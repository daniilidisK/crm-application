package com.erpapplication.VatChecker;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;

public class GRVatChecker {

    public static Document BASE_DOCUMENT_TEMPLATE;

    private static final String ENDPOINT = "https://ec.europa.eu/taxation_customs/tin/services/checkTinService";

    private static final XPathExpression VALID_ELEMENT_MATCHER;
    private static final XPathExpression VALID_STRUCTURE_MATCHER;
    private static final XPathExpression VALID_SYNTAX_MATCHER;

    static {
        String soapCallTemplate = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<checkTin xmlns=\"urn:ec.europa.eu:taxud:tin:services:checkTin:types\">" +
                "<countryCode></countryCode><tinNumber></tinNumber>" +
                "</checkTin>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";

        BASE_DOCUMENT_TEMPLATE = Utils.toDocument(new StringReader(soapCallTemplate));
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            VALID_ELEMENT_MATCHER = xPath.compile("//*[local-name()='checkTinResponse']");
            VALID_STRUCTURE_MATCHER = xPath.compile("//*[local-name()='checkTinResponse']/*[local-name()='validStructure']");
            VALID_SYNTAX_MATCHER = xPath.compile("//*[local-name()='checkTinResponse']/*[local-name()='validSyntax']");
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    public static GRVatCheckResponse doCheck(String tinNr) {
        return doCheck(tinNr, Utils::doCall);
    }

    public static GRVatCheckResponse doCheck(String tinNumber, BiFunction<String, String, InputStream> documentFetcher) {
        Objects.requireNonNull(tinNumber, "tinNumber cannot be null");
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("countryCode", "EL");
            params.put("tinNumber", tinNumber);
            String body = Utils.prepareTemplate(BASE_DOCUMENT_TEMPLATE, params);
            try (InputStream is = documentFetcher.apply(ENDPOINT, body); Reader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Document result = Utils.toDocument(isr);
                Node validNode = (Node) VALID_ELEMENT_MATCHER.evaluate(result, XPathConstants.NODE);
                Node faultNode = (Node) Utils.SOAP_FAULT_MATCHER.evaluate(result, XPathConstants.NODE);
                if (validNode != null) {
                    Node validStructure = (Node) VALID_STRUCTURE_MATCHER.evaluate(result, XPathConstants.NODE);
                    Node validSyntax = (Node) VALID_SYNTAX_MATCHER.evaluate(result, XPathConstants.NODE);
                    return new GRVatCheckResponse("true".equals(Utils.textNode(validSyntax)), "true".equals(Utils.textNode(validStructure)), false, null);
                } else if (faultNode != null) {
                    Node faultCode = (Node) Utils.SOAP_FAULT_CODE_MATCHER.evaluate(result, XPathConstants.NODE);
                    Node faultString = (Node) Utils.SOAP_FAULT_STRING_MATCHER.evaluate(result, XPathConstants.NODE);
                    return new GRVatCheckResponse(false, false, true, new GRVatCheckResponse.Fault(Utils.textNode(faultCode), Utils.textNode(faultString)));
                } else {
                    return new GRVatCheckResponse(false, false, true, null);
                }
            }
        } catch (IOException | XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }
}