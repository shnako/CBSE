package mcom.kernel.util;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
//test only
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

    public static String getStringFromDoc(org.w3c.dom.Document doc)    {
        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        return lsSerializer.writeToString(doc);
    }

}
