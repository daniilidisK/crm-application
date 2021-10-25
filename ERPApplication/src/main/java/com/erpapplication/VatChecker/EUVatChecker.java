package com.erpapplication.VatChecker;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.BiFunction;

public class EUVatChecker {

    private static final Document BASE_DOCUMENT_TEMPLATE;

    private static final String ENDPOINT = "https://ec.europa.eu/taxation_customs/vies/services/checkVatService";
    private static final XPathExpression VALID_ELEMENT_MATCHER;
    private static final XPathExpression NAME_ELEMENT_MATCHER;
    private static final XPathExpression ADDRESS_ELEMENT_MATCHER;
    private static final XPathExpression SOAP_FAULT_MATCHER;
    private static final XPathExpression SOAP_FAULT_CODE_MATCHER;
    private static final XPathExpression SOAP_FAULT_STRING_MATCHER;

    static {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            VALID_ELEMENT_MATCHER = xPath.compile("//*[local-name()='checkVatResponse']/*[local-name()='valid']");
            NAME_ELEMENT_MATCHER = xPath.compile("//*[local-name()='checkVatResponse']/*[local-name()='name']");
            ADDRESS_ELEMENT_MATCHER = xPath.compile("//*[local-name()='checkVatResponse']/*[local-name()='address']");

            SOAP_FAULT_MATCHER = xPath.compile("//*[local-name()='Fault']");
            SOAP_FAULT_CODE_MATCHER = xPath.compile("//*[local-name()='Fault']/*[local-name()='faultcode']");
            SOAP_FAULT_STRING_MATCHER = xPath.compile("//*[local-name()='Fault']/*[local-name()='faultstring']");

            String soapCallTemplate = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soapenv:Header/>" +
                    "<soapenv:Body>" +
                    "<checkVat xmlns=\"urn:ec.europa.eu:taxud:vies:services:checkVat:types\">" +
                    "<countryCode></countryCode><vatNumber></vatNumber>" +
                    "</checkVat>" +
                    "</soapenv:Body>" +
                    "</soapenv:Envelope>";

            BASE_DOCUMENT_TEMPLATE = toDocument(new StringReader(soapCallTemplate));

        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String prepareTemplate(String countryCode, String vatNumber) {
        Document doc = copyDocument();
        doc.getElementsByTagName("countryCode").item(0).setTextContent(countryCode);
        doc.getElementsByTagName("vatNumber").item(0).setTextContent(vatNumber);
        return fromDocument(doc);
    }

    private static Document copyDocument() {
        try {
            Transformer tx = getTransformer();
            DOMSource source = new DOMSource(EUVatChecker.BASE_DOCUMENT_TEMPLATE);
            DOMResult result = new DOMResult();
            tx.transform(source, result);
            return (Document) result.getNode();
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory tf = TransformerFactory.newInstance();
        setAttribute(tf, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setAttribute(tf, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setAttribute(tf, XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        return tf.newTransformer();
    }

    private static void setAttribute(TransformerFactory tf, String key, Object value) {
        try {
            tf.setAttribute(key, value);
        } catch (IllegalArgumentException ignored) {}
    }

    private static Document toDocument(Reader reader) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);

            setFeature(dbFactory, "http://apache.org/xml/features/disallow-doctype-decl", true);
            setFeature(dbFactory,"http://xml.org/sax/features/external-general-entities", false);
            setFeature(dbFactory,"http://xml.org/sax/features/external-parameter-entities", false);
            setFeature(dbFactory,"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setXIncludeAware(false);
            dbFactory.setExpandEntityReferences(false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(new InputSource(reader));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setFeature(DocumentBuilderFactory dbFactory, String feature, boolean value) {
        try {
            dbFactory.setFeature(feature, value);
        } catch (ParserConfigurationException ignored) {}
    }

    private static String fromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            Transformer transformer = getTransformer();
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            return sw.toString();
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    private static InputStream doCall(String endpointUrl, String document) {
        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(document.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            return conn.getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static EUVatCheckResponse doCheck(String countryCode, String vatNumber) {
        return doCheck(countryCode, vatNumber, EUVatChecker::doCall);
    }

    public static EUVatCheckResponse doCheck(String countryCode, String vatNumber, BiFunction<String, String, InputStream> documentFetcher) {
        Objects.requireNonNull(countryCode, "countryCode cannot be null");
        Objects.requireNonNull(vatNumber, "vatNumber cannot be null");
        try {
            String body = prepareTemplate(countryCode, vatNumber);
            try (InputStream is = documentFetcher.apply(ENDPOINT, body); Reader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Document result = toDocument(isr);
                Node validNode = (Node) VALID_ELEMENT_MATCHER.evaluate(result, XPathConstants.NODE);
                Node faultNode = (Node) SOAP_FAULT_MATCHER.evaluate(result, XPathConstants.NODE);
                if (validNode != null) {
                    Node nameNode = (Node) NAME_ELEMENT_MATCHER.evaluate(result, XPathConstants.NODE);
                    Node addressNode = (Node) ADDRESS_ELEMENT_MATCHER.evaluate(result, XPathConstants.NODE);
                    return new EUVatCheckResponse("true".equals(textNode(validNode)), textNode(nameNode), textNode(addressNode), false, null);
                } else if (faultNode != null) {
                    Node faultCode = (Node) SOAP_FAULT_CODE_MATCHER.evaluate(result, XPathConstants.NODE);
                    Node faultString = (Node) SOAP_FAULT_STRING_MATCHER.evaluate(result, XPathConstants.NODE);
                    return new EUVatCheckResponse(false, null, null, true, new EUVatCheckResponse.Fault(textNode(faultCode), textNode(faultString)));
                } else {
                    return new EUVatCheckResponse(false, null, null, false, null);
                }
            }
        } catch (IOException | XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String textNode(Node node) {
        return node != null ? node.getTextContent() : null;
    }
}
