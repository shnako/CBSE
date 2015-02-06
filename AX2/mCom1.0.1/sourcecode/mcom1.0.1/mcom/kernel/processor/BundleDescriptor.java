package mcom.kernel.processor;
/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import java.io.Serializable;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import mcom.bundle.Contract;
import mcom.bundle.util.bMethod;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SuppressWarnings("rawtypes")
public class BundleDescriptor implements Serializable {

	private static final long serialVersionUID = 7184766154972304231L;

	private String      bundleName;
	private int			bundleId;
	private int         port;
	private InetAddress address;
	
	private Class 		bundleController = null;
	private bMethod 	bundleControllerInit = null;
	private Contract [] contracts;
	
	public BundleDescriptor() {
		contracts = new Contract[0];
	}
		
	public Class getBundleController() {
		return bundleController;
	}

	public void setBundleController(Class bundleController) {
		this.bundleController = bundleController;
	}

	public bMethod getBundleControllerInit() {
		return bundleControllerInit;
	}

	public void setBundleControllerInit(bMethod bundleControllerInit) {
		
		this.bundleControllerInit = bundleControllerInit;
	}

	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}
	
	public String getBundleName() {
		return bundleName;
	}

	public Contract [] getContracts(){
		return contracts;
	}
	
	public void setContract(Contract [] contracts){
		this.contracts = contracts;
	}
	
	public void addContract(Contract c){
		Contract [] p_temp = new Contract[contracts.length +1];
		List<Contract> p_t = new LinkedList<Contract>();
		
		for(Contract pt: contracts){
			if(pt !=null){
				p_t.add(pt);
			}
		}
		
		int i = 0;
		for(Contract pt: p_t){
			p_temp[i] = pt;
			i = i +1;
		}
		
		p_temp[i] = c;
		contracts = p_temp;
	}	
	
	public InetAddress getAddress() {
		return address;
	}
	
	public void setAddress(InetAddress serviceAddress) {
		this.address = serviceAddress;
	}
	
	protected String getAddressAsString() {
		if(getAddress() != null){
			return getAddress().getHostAddress();			
		}
		return null;
	}
		
	public int getPort() {
		return port;
	}
	
	public void setPort(int servicePort) {
		this.port = servicePort;
	}

	public int getBundleId() {
		return bundleId;
	}

	public void setBundleId(int bundleId) {
		this.bundleId = bundleId;
	}

	protected String getPortAsString() {
		return ""+getPort();
	}

	public String toString() {
		Document doc = encodeasxml();
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
		return prettyPrint(output);
	}
	
	public  Document encodeasxml(){
		Document doc = null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("BundleDescriptor");
			doc.appendChild(rootElement);
			
			Element xbundleName = doc.createElement("BundleName");
			xbundleName.appendChild(doc.createTextNode(getBundleName()));
			rootElement.appendChild(xbundleName);
			
			Element xbundleId = doc.createElement("BundleId");
			xbundleId.appendChild(doc.createTextNode(""+getBundleId()));
			rootElement.appendChild(xbundleId);
			
			Element xhostAddress = doc.createElement("HostAddress");
			if(getAddressAsString()!=null){
				xhostAddress.appendChild(doc.createTextNode(getAddressAsString()));				
			}
			else{
				xhostAddress.appendChild(doc.createTextNode("null"));
			}
			rootElement.appendChild(xhostAddress);
			
			Element xhostPort = doc.createElement("HostPort");
			xhostPort.appendChild(doc.createTextNode(getPortAsString()));
			rootElement.appendChild(xhostPort);
			
			Element xbundleController = doc.createElement("BundleController");
			xbundleController.appendChild(doc.createTextNode(bundleController.getName()));
			rootElement.appendChild(xbundleController);
			
			Element xbundleControllerInit = doc.createElement("BundleControllerInit");
			xbundleControllerInit.appendChild(doc.createTextNode(bundleControllerInit.getMethodName()));
			rootElement.appendChild(xbundleControllerInit);
			
			Element xcontracts= doc.createElement("Contracts");
			rootElement.appendChild(xcontracts);
			for(Contract c: contracts){
				c.encodeasxml(doc, xcontracts);
			}
						
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
		
		return doc;
	}	
	
	private String prettyPrint(final String xml){  

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
	    }
	    catch (Exception e) {
	        throw new RuntimeException("Error pretty printing xml:\n" + xml, e);
	    }
	    return sw.toString();
	}
}
