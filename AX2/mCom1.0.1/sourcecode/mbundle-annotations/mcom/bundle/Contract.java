package mcom.bundle;

/**
 * A Contract specifies a service that is provided by a Bundle and consist of the following:
 * 1. bundleController - A class in the jar file that contains a BundleControllerInit method
 * 2. bundleControllerInit - A method in bundleController class that initialises variables and calls functions required for the contract to execute appropriately
 * 3. bundleEntity - A class in the jar file that contains a BundleEntityContract method.
 * 4. bundleEntityContract - A method in bundleEntity class that defines a contract
 * 5. parameters[] - A list of parameters for bundleEntityContract
 * 
 * @Author Inah Omoronyia
 */
import java.io.Serializable;
import java.io.StringWriter;

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

import mcom.bundle.util.bMethod;
import mcom.bundle.util.bParameter;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SuppressWarnings("rawtypes")
public class Contract implements Serializable{
	
	private static final long serialVersionUID = 897661373381998917L;
	
	private Class bundleEntity; //class describing a contract	
	private bMethod bundleEntityContract; //method describing a contract and its parameters
	
	private String description;
	private int contractType = -1;
	
	private String returnType;
	
	public Contract(){

	}
	
	public Class getBundleEntity() {
		return bundleEntity;
	}
	
	public void setBundleEntity(Class bundleEntity) {
		this.bundleEntity = bundleEntity;
	}

	public bMethod getBundleEntityContract() {
		return bundleEntityContract;
	}

	public void setBundleEntityContract(bMethod bundleEntityContract) {
		this.bundleEntityContract = bundleEntityContract;
	}
	
	public int getContractType() {
		return contractType;
	}

	public void setContractType(int contractType) {
		this.contractType = contractType;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String desc){
		this.description = desc;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
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
			Element xcontract = doc.createElement("Contract");
			doc.appendChild(xcontract);
			
			Element xbundleEntity = doc.createElement("BundleEntity");
			xbundleEntity.appendChild(doc.createTextNode(bundleEntity.getName()));
			xcontract.appendChild(xbundleEntity);
			
			Element xbundleEntityContract = doc.createElement("BundleEntityContract");
			xbundleEntityContract.appendChild(doc.createTextNode(bundleEntityContract.getMethodName()));
			xcontract.appendChild(xbundleEntityContract);

			Element xcontractType = doc.createElement("ContractType");
			xcontractType.appendChild(doc.createTextNode(ContractType.getType(contractType)));			
			xcontract.appendChild(xcontractType);
			
			Element xdescription = doc.createElement("Description");
			xdescription.appendChild(doc.createTextNode(description));
			xcontract.appendChild(xdescription);
			
			Element xparameters= doc.createElement("Parameters");
			xcontract.appendChild(xparameters);
			for(bParameter p: bundleEntityContract.getbParameters()){
				Element xparameter= doc.createElement("Parameter");
				xparameters.appendChild(xparameter);
				
				Element xname = doc.createElement("Name");
				xname.appendChild(doc.createTextNode(p.getClassName()));
				xparameter.appendChild(xname);
				
				Element xvalue = doc.createElement("Value");
				xvalue.appendChild(doc.createTextNode(p.getValue().toString()));
				xparameter.appendChild(xvalue);			
			}
			
			Element xreturnType = doc.createElement("ReturnType");
			xreturnType.appendChild(doc.createTextNode(returnType));						
			xcontract.appendChild(xreturnType);									
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
		
		return doc;
	}	
	
	public void encodeasxml(Document doc, Element xcontracts){
		Element xcontract= doc.createElement("Contract");
		xcontracts.appendChild(xcontract);
		
		Element xbundleEntity = doc.createElement("BundleEntity");
		xbundleEntity.appendChild(doc.createTextNode(bundleEntity.getName()));
		xcontract.appendChild(xbundleEntity);
		
		Element xbundleEntityContract = doc.createElement("BundleEntityContract");
		xbundleEntityContract.appendChild(doc.createTextNode(bundleEntityContract.getMethodName()));
		xcontract.appendChild(xbundleEntityContract);

		Element xcontractType = doc.createElement("ContractType");
		xcontractType.appendChild(doc.createTextNode(ContractType.getType(contractType)));			
		xcontract.appendChild(xcontractType);
		
		Element xdescription = doc.createElement("Description");
		xdescription.appendChild(doc.createTextNode(description));
		xcontract.appendChild(xdescription);
		
		Element xparameters= doc.createElement("Parameters");
		xcontract.appendChild(xparameters);
		for(bParameter p: bundleEntityContract.getbParameters()){
			Element xparameter= doc.createElement("Parameter");
			xparameters.appendChild(xparameter);
			
			Element xname = doc.createElement("Name");
			xname.appendChild(doc.createTextNode(p.getClassName()));
			xparameter.appendChild(xname);
			
			Element xvalue = doc.createElement("Value");
			if(p.getValue() != null){
				xvalue.appendChild(doc.createTextNode(p.getValue().toString()));				
			}
			else{
				xvalue.appendChild(doc.createTextNode("null"));
			}
			xparameter.appendChild(xvalue);			
		}
		
		Element xreturnType = doc.createElement("ReturnType");
		if(returnType == null){
			xreturnType.appendChild(doc.createTextNode("VOID"));						
		}
		else{
			xreturnType.appendChild(doc.createTextNode(returnType.getClass().getName()));						
		}
		xcontract.appendChild(xreturnType);
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
