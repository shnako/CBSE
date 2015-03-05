package mcom.wire.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;

// AX3 State implementation.
public final class ClientConnectionManager {
    private static final String CONNECTIONS_FILE = "connections.xml";
    private static final String NODE_ROOT = "PersistentConnections", NODE_CONNECTION = "Connection",
            NODE_HOST_ADDRESS = "HostAddress", NODE_SERVER_CONNECTION_ID = "ServerConnectionId";
    private static ClientConnectionManager clientConnectionManager;
    private HashMap<String, ClientConnectionDetails> connections;

    public static ClientConnectionManager getClientConnectionManager() {
        if (clientConnectionManager == null) {
            clientConnectionManager = new ClientConnectionManager();
        }

        return clientConnectionManager;
    }

    private ClientConnectionManager() {
        loadPersistentConnectionsOrCreateFile();
    }

    public ClientConnectionDetails getConnection(String host) {
        return connections.get(host);
    }

    public void addConnection(String host, ConnectionType connectionType, int connectionId) {
        connections.put(host, new ClientConnectionDetails(connectionType, connectionId));

        if (connectionType == ConnectionType.PERSISTENT) {
            persistConnection(host, connectionId);
        }
    }

    private static void persistConnection(String host, int serverConnectionId) {
        File file = new File(CONNECTIONS_FILE);
        if (file.exists()) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(file);

                Element eHostAddress = doc.createElement(NODE_HOST_ADDRESS);
                eHostAddress.appendChild(doc.createTextNode(host));

                Element eServerConnectionId = doc.createElement(NODE_SERVER_CONNECTION_ID);
                eServerConnectionId.appendChild(doc.createTextNode("" + serverConnectionId));

                Element eConnection = doc.createElement(NODE_CONNECTION);
                eConnection.appendChild(eHostAddress);
                eConnection.appendChild(eServerConnectionId);

                doc.getFirstChild().appendChild(eConnection);

                // Write to file.
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
            } catch (Exception ex) {
                System.err.println("Could not persist connection: " + ex.getMessage());
            }
        } else {
            System.err.println("Connections file " + CONNECTIONS_FILE + " not found!");
        }
    }

    private void loadPersistentConnectionsOrCreateFile() {
        connections = new HashMap<String, ClientConnectionDetails>();

        File file = new File(CONNECTIONS_FILE);
        if (file.exists()) {
            // Load the connections.
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(file);

                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName(NODE_CONNECTION);

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;

                        String hostAddress = element.getElementsByTagName(NODE_HOST_ADDRESS).item(0).getTextContent();
                        String serverConnectionId = element.getElementsByTagName(NODE_SERVER_CONNECTION_ID).item(0).getTextContent();

                        connections.put(hostAddress, new ClientConnectionDetails(ConnectionType.PERSISTENT, Integer.parseInt(serverConnectionId)));
                    }
                }

                System.out.println("Loaded " + nodeList.getLength() + " persistent connections from " + CONNECTIONS_FILE);
            } catch (Exception ex) {
                System.err.println("Could not load persistent connections: " + ex.getMessage());
            }
        } else {
            // Create the XML file.
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                // Root.
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement(NODE_ROOT);
                doc.appendChild(rootElement);

                // Write to file.
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);

                System.out.println("Created persistent connection file " + CONNECTIONS_FILE);
            } catch (Exception ex) {
                System.out.println("Could not create " + CONNECTIONS_FILE + ": " + ex.getMessage());
            }
        }
    }
}
