/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.wsdl.wsdl11.builder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.w3c.dom.Document;

public class XsdBasedSoap11Wsdl4jDefinitionBuilderTest extends XMLTestCase {

    private XsdBasedSoap11Wsdl4jDefinitionBuilder builder;

    private Transformer transformer;

    private DocumentBuilder documentBuilder;

    protected void setUp() throws Exception {
        builder = new XsdBasedSoap11Wsdl4jDefinitionBuilder();
        builder.setLocationUri("http://localhost:8080/");
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testNonExistingSchema() throws Exception {
        try {
            builder.setSchema(new ClassPathResource("bla"));
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testSingle() throws Exception {
        builder.setSchema(new ClassPathResource("single.xsd", getClass()));
        builder.setPortTypeName("Order");
        builder.setTargetNamespace("http://www.springframework.org/spring-ws/single/definitions");
        builder.afterPropertiesSet();

        buildAll();

        Wsdl11Definition definition = builder.getDefinition();
        DOMResult domResult = new DOMResult();
        transformer.transform(definition.getSource(), domResult);

        Document result = (Document) domResult.getNode();
        Document expected = documentBuilder.parse(getClass().getResourceAsStream("single-inline.wsdl"));
        assertXMLEqual("Invalid WSDL built", expected, result);
    }

    public void testSingleImport() throws Exception {
        builder.setSchema(new ClassPathResource("single.xsd", getClass()));
        builder.setSchemaLocation("single.xsd");
        builder.setPortTypeName("Order");
        builder.setTargetNamespace("http://www.springframework.org/spring-ws/single/definitions");
        builder.afterPropertiesSet();

        buildAll();

        Wsdl11Definition definition = builder.getDefinition();
        DOMResult domResult = new DOMResult();
        transformer.transform(definition.getSource(), domResult);

        Document result = (Document) domResult.getNode();
        Document expected = documentBuilder.parse(getClass().getResourceAsStream("single-import.wsdl"));
        assertXMLEqual("Invalid WSDL built", expected, result);
    }

    public void testFollowIncluding() throws Exception {
        builder.setSchema(new ClassPathResource("including.xsd", getClass()));
        builder.setPortTypeName("Order");
        builder.setTargetNamespace("http://www.springframework.org/spring-ws/include/definitions");
        builder.setFollowIncludeImport(true);
        builder.afterPropertiesSet();

        buildAll();

        Wsdl11Definition definition = builder.getDefinition();
        DOMResult domResult = new DOMResult();
        transformer.transform(definition.getSource(), domResult);

        Document result = (Document) domResult.getNode();
        Document expected = documentBuilder.parse(getClass().getResourceAsStream("include-inline.wsdl"));
        assertXMLEqual("Invalid WSDL built", expected, result);
    }

    public void testFollowImport() throws Exception {
        builder.setSchema(new ClassPathResource("importing.xsd", getClass()));
        builder.setPortTypeName("Order");
        builder.setTargetNamespace("http://www.springframework.org/spring-ws/import/definitions");
        builder.setFollowIncludeImport(true);
        builder.afterPropertiesSet();

        buildAll();

        Wsdl11Definition definition = builder.getDefinition();
        DOMResult domResult = new DOMResult();
        transformer.transform(definition.getSource(), domResult);

        Document result = (Document) domResult.getNode();
        Document expected = documentBuilder.parse(getClass().getResourceAsStream("import-inline.wsdl"));
        assertXMLEqual("Invalid WSDL built", expected, result);
    }

    public void testAirline() throws Exception {
        builder.setSchema(new ClassPathResource("airline.xsd", getClass()));
        builder.setPortTypeName("Airline");
        builder.setTargetNamespace("http://www.springframework.org/spring-ws/samples/airline/definitions");
        builder.afterPropertiesSet();
        buildAll();
        Wsdl11Definition definition = builder.getDefinition();
        DOMResult domResult = new DOMResult();
        transformer.transform(definition.getSource(), domResult);

        Document result = (Document) domResult.getNode();
        Document expected = documentBuilder.parse(getClass().getResourceAsStream("airline.wsdl"));
        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual("Invalid WSDL built", expected, result);
    }

    private void buildAll() {
        builder.buildDefinition();
        builder.buildImports();
        builder.buildTypes();
        builder.buildMessages();
        builder.buildPortTypes();
        builder.buildBindings();
        builder.buildServices();
    }


}