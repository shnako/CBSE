package mcom.kernel.util;
/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import javafx.util.Pair;
import mcom.InvokeRequest;
import mcom.bundle.Contract;
import mcom.bundle.annotations.mControllerInit;
import mcom.bundle.annotations.mEntityContract;
import mcom.bundle.util.bMethod;
import mcom.kernel.processor.BundleClassLoader;
import mcom.kernel.processor.BundleDescriptor;
import mcom.kernel.processor.BundleDirProcessor;
import mcom.kernel.processor.BundleJarProcessor;
import mcom.wire.util.IPResolver;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class KernelUtil {

    public static void storeBundleDescriptor(BundleDescriptor bd) {
        Document doc = bd.encodeasxml();

        // write the content into mcom BundleDescDir
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            //StreamResult result = new StreamResult(System.out);
            StreamResult result = new StreamResult(new File(KernelConstants.BUNDLEDESCRIPTORDIR + "/" + bd.getBundleName().split(".jar")[0] + ".xml"));
            transformer.transform(source, result);

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static Document decodeTextToXml(String dtText) {
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            //doc = dBuilder.parse(dtText);
            doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(dtText.getBytes("utf-8"))));
            doc.getDocumentElement().normalize();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    public static int retrieveBundleId(Document doc) {
        String bundleId = doc.getElementsByTagName("BundleId").item(0).getTextContent();

        return new Integer(bundleId.trim());
    }

    public static ArrayList<String> retrieveContractNames(Document doc) {
        ArrayList<String> cnames = new ArrayList<String>();
        System.out.println(getBDString(doc));

        NodeList nList = doc.getElementsByTagName("Contract");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                //TODO: BUG FIX - prevent multiple contracts with the same contractName in a bundle (See:BundleAnnotationProcessor)
                String sBundleEntityContract = eElement.getElementsByTagName("BundleEntityContract").item(0).getTextContent();
                cnames.add(sBundleEntityContract);
            }
        }

        return cnames;
    }

    public static HashMap<Object, Object> retrieveParameters(Document doc, String contractName) {
        HashMap<Object, Object> parameters = new HashMap<Object, Object>();

        NodeList nList = doc.getElementsByTagName("Contract");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                //TODO: BUG FIX - prevent multiple contracts with the same contractName in a bundle (See:BundleAnnotationProcessor)
                String sBundleEntityContract = eElement.getElementsByTagName("BundleEntityContract").item(0).getTextContent();

                if (sBundleEntityContract.equals(contractName)) {
                    NodeList npList = eElement.getElementsByTagName("Parameter");
                    for (int temp1 = 0; temp1 < npList.getLength(); temp1++) {
                        Node npNode = npList.item(temp1);

                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element epElement = (Element) npNode;
                            String pName = epElement.getElementsByTagName("Name").item(0).getTextContent();
                            String pValue = epElement.getElementsByTagName("Value").item(0).getTextContent();

                            parameters.put(pName, pValue);
                        }
                    }
                    break;
                }
            }
        }
        return parameters;
    }

    @SuppressWarnings("rawtypes")
    public static BundleDescriptor loadBundleDescriptor(String bundleId) {
        BundleDescriptor bd = null;
        File folder = new File(KernelConstants.BUNDLEDESCRIPTORDIR);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                //InputStream is = checkForUtf8BOMAndDiscardIfAny(new FileInputStream(file));
                InputStream is = new FileInputStream(file);
                Reader reader = new InputStreamReader(is, "UTF-8");
                InputSource source = new InputSource(reader);
                Document doc = dBuilder.parse(source);

                doc.getDocumentElement().normalize();


                String BundleId = doc.getElementsByTagName("BundleId").item(0).getTextContent();
                boolean found = false;

                if (BundleId.equals(bundleId)) {
                    found = true;
                    bd = new BundleDescriptor();

                    String bundleName = doc.getElementsByTagName("BundleName").item(0).getTextContent();
                    bd.setBundleName(bundleName);

                    bd.setBundleId(new Integer(BundleId));

                    String hostAddress = doc.getElementsByTagName("HostAddress").item(0).getTextContent();
                    InetAddress inaddress = IPResolver.getAddress(hostAddress);
                    bd.setAddress(inaddress);

                    String hostPort = doc.getElementsByTagName("HostPort").item(0).getTextContent();
                    bd.setPort(new Integer(hostPort));

                    String sbundleController = doc.getElementsByTagName("BundleController").item(0).getTextContent();
                    Class bundleController = getmClass(sbundleController);
                    bd.setBundleController(bundleController);

                    String sbundleControllerInit = doc.getElementsByTagName("BundleControllerInit").item(0).getTextContent();
                    bMethod bundleControllerInit = getbControllerInit(bundleController, sbundleControllerInit);
                    bd.setBundleControllerInit(bundleControllerInit);

                    NodeList nList = doc.getElementsByTagName("Contracts");
                    for (int temp = 0; temp < nList.getLength(); temp++) {

                        Node nNode = nList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;
                            String sbundleEntity = eElement.getElementsByTagName("BundleEntity").item(0).getTextContent();
                            Class bundleEntity = getmClass(sbundleEntity);

                            ArrayList<Contract> contracts = getbBundleEntityContracts(bundleEntity);
                            for (Contract contract : contracts) {
                                bd.addContract(contract);
                            }
                        }
                    }
                }

                if (found) {
                    break;
                }

            } catch (IOException i) {
                i.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXParseException e) {
                //
            } catch (SAXException e) {
                e.printStackTrace();
            }

        }
        return bd;
    }

    @SuppressWarnings("rawtypes")
    public static BundleDescriptor[] loadBundleDescriptors() {
        BundleDescriptor[] bds = new BundleDescriptor[0];
        File folder = new File(KernelConstants.BUNDLEDESCRIPTORDIR);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                //InputStream is = checkForUtf8BOMAndDiscardIfAny(new FileInputStream(file));
                InputStream is = new FileInputStream(file);
                Reader reader = new InputStreamReader(is, "UTF-8");
                InputSource source = new InputSource(reader);
                Document doc = dBuilder.parse(source);

                doc.getDocumentElement().normalize();

                BundleDescriptor bd = new BundleDescriptor();

                String bundleName = doc.getElementsByTagName("BundleName").item(0).getTextContent();
                bd.setBundleName(bundleName);

                String BundleId = doc.getElementsByTagName("BundleId").item(0).getTextContent();
                bd.setBundleId(new Integer(BundleId));

                String hostAddress = doc.getElementsByTagName("HostAddress").item(0).getTextContent();
                InetAddress inaddress = IPResolver.getAddress(hostAddress);
                bd.setAddress(inaddress);

                String hostPort = doc.getElementsByTagName("HostPort").item(0).getTextContent();
                bd.setPort(new Integer(hostPort));

                String sbundleController = doc.getElementsByTagName("BundleController").item(0).getTextContent();
                Class bundleController = getmClass(sbundleController);
                bd.setBundleController(bundleController);

                String sbundleControllerInit = doc.getElementsByTagName("BundleControllerInit").item(0).getTextContent();
                bMethod bundleControllerInit = getbControllerInit(bundleController, sbundleControllerInit);
                bd.setBundleControllerInit(bundleControllerInit);

                NodeList nList = doc.getElementsByTagName("Contracts");
                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String sbundleEntity = eElement.getElementsByTagName("BundleEntity").item(0).getTextContent();
                        Class bundleEntity = getmClass(sbundleEntity);

                        ArrayList<Contract> contracts = getbBundleEntityContracts(bundleEntity);
                        for (Contract contract : contracts) {
                            bd.addContract(contract);
                        }
                    }
                }

                BundleDescriptor[] p_temp = new BundleDescriptor[bds.length + 1];
                List<BundleDescriptor> p_t = new LinkedList<BundleDescriptor>();

                for (BundleDescriptor pt : bds) {
                    if (pt != null) {
                        p_t.add(pt);
                    }
                }

                int i = 0;
                for (BundleDescriptor pt : p_t) {
                    p_temp[i] = pt;
                    i = i + 1;
                }

                p_temp[i] = bd;
                bds = p_temp;

            } catch (IOException i) {
                i.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXParseException e) {
                //
            } catch (SAXException e) {
                e.printStackTrace();
            }

        }
        return bds;
    }


    @SuppressWarnings("rawtypes")
    private static Class getmClass(String sClass) {
        Class mClass = null;
        File[] mBundles = BundleDirProcessor.loadFilesInBundleDirectory();//list of mBundles (files) in BundleDir

        for (File mBundle : mBundles) {
            String[] mBundleClassFiles = new BundleJarProcessor(mBundle).getClassFiles();    //list of classes in mBundle

            boolean found = false;
            //create custom class loader
            try {
                URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                BundleClassLoader mBundleClassLoader = new BundleClassLoader(loader.getURLs());
                String path = mBundle.getAbsolutePath();
                path = "jar:file://" + path + "!/";
                mBundleClassLoader.addURL(new URL(path));

                for (String cf : mBundleClassFiles) {
                    Class oClass = Class.forName(cf, true, mBundleClassLoader);
                    if (oClass.getName().equals(sClass)) {
                        mClass = oClass;
                        found = true;
                    }
                    break;
                }
                if (found) {
                    break;
                }
            } catch (ClassNotFoundException e) {
            } catch (NoClassDefFoundError e) {
                //handle carefully
            } catch (UnsatisfiedLinkError e) {
                //handle carefully
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        return mClass;
    }

    @SuppressWarnings("rawtypes")
    private static bMethod getbControllerInit(Class oClass, String sMethod) {
        bMethod bundleControllerInit = null;

        boolean found = false;
        //create custom class loader
        try {
            Method[] methods = oClass.getMethods();
            for (Method m : methods) {
                Annotation[] mannotations = m.getAnnotations();
                for (Annotation annotation2 : mannotations) {
                    if (annotation2 instanceof mControllerInit) {
                        if (m.getName().equals(sMethod)) {
                            bundleControllerInit = bMethod.encodeAsbMethod(m, oClass);
                            found = true;
                            break;
                        }
                    }
                }
                if (found) {
                    break;
                }
            }
        } catch (NoClassDefFoundError e) {
            //handle carefully
        } catch (UnsatisfiedLinkError e) {
            //handle carefully
        } finally {
        }

        return bundleControllerInit;
    }

    @SuppressWarnings("rawtypes")
    private static ArrayList<Contract> getbBundleEntityContracts(Class oClass) {
        ArrayList<Contract> contracts = new ArrayList<Contract>();

        //create custom class loader
        try {
            Method[] methods = oClass.getMethods();
            for (Method m : methods) {
                Annotation[] mannotations = m.getAnnotations();
                for (Annotation annotation2 : mannotations) {
                    if (annotation2 instanceof mEntityContract) {
                        Contract contract = new Contract();
                        contract.setBundleEntity(oClass);
                        contract.setBundleEntityContract(bMethod.encodeAsbMethod(m, oClass));
                        String description = ((mEntityContract) annotation2).description();
                        contract.setDescription(description);
                        int contractType = ((mEntityContract) annotation2).contractType();
                        contract.setContractType(contractType);
                        contract.setReturnType(m.getReturnType().getName());

                        contracts.add(contract);
                    }
                }
            }
        } catch (NoClassDefFoundError e) {
            //handle carefully
        } catch (UnsatisfiedLinkError e) {
            //handle carefully
        } finally {
        }

        return contracts;
    }

    @SuppressWarnings("rawtypes")
    public static Document encodeRemoteCallAsxml(String bhip, int bhport, int bid, String cName, HashMap<String, String> parameters) {
        Document doc = null;

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("InvokeRequest");
            doc.appendChild(rootElement);

            Element bhhostIp = doc.createElement("BundleHostIP");
            bhhostIp.appendChild(doc.createTextNode(bhip));
            rootElement.appendChild(bhhostIp);

            Element bhhostPort = doc.createElement("BundleHostPort");
            bhhostPort.appendChild(doc.createTextNode("" + bhport));
            rootElement.appendChild(bhhostPort);

            Element bundleId = doc.createElement("BundleId");
            bundleId.appendChild(doc.createTextNode("" + bid));
            rootElement.appendChild(bundleId);

            Element contract = doc.createElement("Contract");
            rootElement.appendChild(contract);

            Element contractName = doc.createElement("ContractName");
            contractName.appendChild(doc.createTextNode("" + cName));
            contract.appendChild(contractName);

            Element xparameters = doc.createElement("Parameters");
            Iterator it = parameters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String pName = (String) pairs.getKey();
                String pValue = (String) pairs.getValue();

                Element xparameter = doc.createElement("Parameter");

                Element xname = doc.createElement("Type");
                xname.appendChild(doc.createTextNode(pName));
                xparameter.appendChild(xname);

                Element xvalue = doc.createElement("Value");
                xvalue.appendChild(doc.createTextNode(pValue));
                xparameter.appendChild(xvalue);

                xparameters.appendChild(xparameter);
            }
            contract.appendChild(xparameters);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return doc;
    }

    public static InvokeRequest decodeInvokeRequest(Document invokeDoc) throws ClassNotFoundException {
        InvokeRequest result = new InvokeRequest();

        invokeDoc.getDocumentElement().normalize();

        result.setBundleHostIp(invokeDoc.getElementsByTagName("BundleHostIP").item(0).getTextContent());
        result.setBundleHostPort(Integer.parseInt(invokeDoc.getElementsByTagName("BundleHostPort").item(0).getTextContent()));
        result.setBundleId(Integer.parseInt(invokeDoc.getElementsByTagName("BundleId").item(0).getTextContent()));

        Node contractNode = invokeDoc.getElementsByTagName("Contract").item(0);

        result.setContractName(contractNode.getChildNodes().item(0).getTextContent());

        NodeList parametersNodeList = contractNode.getChildNodes().item(1).getChildNodes();

        for (int i = 0; i < parametersNodeList.getLength(); i++) {
            NodeList parameterNodeList = parametersNodeList.item(i).getChildNodes();
            result.addParameter(new Pair<Class, String>(Class.forName(parameterNodeList.item(0).getTextContent()), parameterNodeList.item(1).getTextContent()));
        }

        return result;
    }

    public static String getBDString(Document doc) {
        String output = "";

        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            output = writer.getBuffer().toString().replaceAll("\n|\r", "");

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static String getMetadataAndBDString (String BDString, Metadata meta) {
        return "<container>" + meta.toString() + BDString + "</container>";
    }

    public static Metadata getMetadataFromString (String str) {
        Metadata meta = new Metadata();
        try
        {
            Document document = KernelUtil.decodeTextToXml(str.trim());
            Node importedNode = meta.getDocument().importNode(document.getElementsByTagName("header").item(0), true);

            if (importedNode.hasChildNodes()) {
                meta.getDocument().getFirstChild().appendChild(importedNode.getFirstChild());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return meta;
    }

    public static String stripMetadataFromString (String str) {
        String res;
        try {
            res = str.split("</header>")[1].split("</container>")[0];
            return res;
        }
        catch (Exception e) {
            return str;
        }
    }


    public static String prettyPrint(final String xml) {

        if (StringUtils.isBlank(xml)) {
            throw new RuntimeException("xml was null or blank in prettyPrint()");
        }

        final StringWriter sw;

        try {
            final org.dom4j.io.OutputFormat format = org.dom4j.io.OutputFormat.createPrettyPrint();
            final org.dom4j.Document document = DocumentHelper.parseText(xml);
            sw = new StringWriter();
            final org.dom4j.io.XMLWriter writer = new org.dom4j.io.XMLWriter(sw, format);
            writer.write(document);
        } catch (Exception e) {
            throw new RuntimeException("Error pretty printing xml:\n" + xml, e);
        }
        return sw.toString();
    }
//	private static InputStream checkForUtf8BOMAndDiscardIfAny(InputStream inputStream) throws IOException {
//	    PushbackInputStream pushbackInputStream = new PushbackInputStream(new BufferedInputStream(inputStream), 3);
//	    byte[] bom = new byte[3];
//	    if (pushbackInputStream.read(bom) != -1) {
//	        if (!(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)) {
//	            pushbackInputStream.unread(bom);
//	        }
//	    }
//	    return pushbackInputStream; 
//	 }
}
