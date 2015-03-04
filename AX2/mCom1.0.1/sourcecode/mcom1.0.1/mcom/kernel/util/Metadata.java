package mcom.kernel.util;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class Metadata {

    private Document doc = null;

    public Metadata() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            docBuilder = docFactory.newDocumentBuilder();


            doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("header");
            doc.appendChild(rootElement);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Document getDocument() {
        if (doc != null) {
            return doc;
        }
        else {
            Metadata meta = new Metadata();
            return meta.doc;
        }
    }

    public void addMetadata(String name, String value) {
        Element el = doc.createElement(name);
        doc.getFirstChild().appendChild(el);
        el.appendChild(doc.createTextNode(value));
    }

    public String toString() {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        }
        catch (Exception e) {

        }
        return "";
    }

}
