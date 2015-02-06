package mcom.kernel.util;
/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import mcom.bundle.Contract;
import mcom.bundle.annotations.mControllerInit;
import mcom.bundle.annotations.mEntityContract;
import mcom.bundle.util.bMethod;
import mcom.kernel.processor.BundleClassLoader;
import mcom.kernel.processor.BundleDescriptor;
import mcom.kernel.processor.BundleDirProcessor;
import mcom.kernel.processor.BundleJarProcessor;
import mcom.wire.util.IPResolver;

public class KernelUtil {
	
	public static void storeBundleDescriptor(BundleDescriptor bd){
		Document doc = bd.encodeasxml();

		// write the content into mcom BundleDescDir
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);			
			//StreamResult result = new StreamResult(System.out);			
			StreamResult result = new StreamResult(new File(KernelConstants.BUNDLEDESCRIPTORDIR+"/"+bd.getBundleName().split(".jar")[0]+".xml"));
			transformer.transform(source, result);
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static BundleDescriptor[] loadBundleDescriptors() {
		BundleDescriptor [] bds = new BundleDescriptor[0];		
		File folder = new File(KernelConstants.BUNDLEDESCRIPTORDIR);
		File[] listOfFiles = folder.listFiles();

		for(File file: listOfFiles){
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
				bMethod bundleControllerInit = getbControllerInit(bundleController,sbundleControllerInit);
				bd.setBundleControllerInit(bundleControllerInit);
				
				NodeList nList = doc.getElementsByTagName("Contracts");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					
					Node nNode = nList.item(temp);			 
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						String sbundleEntity = eElement.getElementsByTagName("BundleEntity").item(0).getTextContent();
						Class bundleEntity = getmClass(sbundleEntity);
						
						ArrayList<Contract> contracts = getbBundleEntityContracts(bundleEntity);
						for(Contract contract:contracts){
							bd.addContract(contract);
						}
					}
			 	}
				
				BundleDescriptor [] p_temp = new BundleDescriptor[bds.length +1];
				List<BundleDescriptor> p_t = new LinkedList<BundleDescriptor>();
				
				for(BundleDescriptor pt: bds){
					if(pt !=null){
						p_t.add(pt);
					}
				}
				
				int i = 0;
				for(BundleDescriptor pt: p_t){
					p_temp[i] = pt;
					i = i +1;
				}
				
				p_temp[i] = bd;
				bds = p_temp;
				
			} 
			catch (IOException i) {
				i.printStackTrace();
			} 
			catch (ParserConfigurationException e) {
				e.printStackTrace();
			} 
			catch(SAXParseException e){
				//
			}
			catch (SAXException e) {
				e.printStackTrace();
			}	
			
		}
		return bds;
	}
	
	
	@SuppressWarnings("rawtypes")
	private static Class getmClass(String sClass){
		Class mClass = null;
		File[] mBundles = BundleDirProcessor.loadFilesInBundleDirectory();//list of mBundles (files) in BundleDir
			
		for(File mBundle:mBundles){ 				
			String [] mBundleClassFiles = new BundleJarProcessor(mBundle).getClassFiles();	//list of classes in mBundle					
			
			boolean found = false;
			//create custom class loader
			try{
				URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
				BundleClassLoader mBundleClassLoader = new BundleClassLoader(loader.getURLs());
				String path = mBundle.getAbsolutePath();
				path = "jar:file://"+ path+"!/";
				mBundleClassLoader.addURL(new URL(path));		
							
				for(String cf: mBundleClassFiles){	
					Class oClass = Class.forName(cf, true, mBundleClassLoader);
					if(oClass.getName().equals(sClass)){
						mClass = oClass;
						found = true;
					}
					break;					
				}	
				if(found){
					break;
				}
			}
			catch (ClassNotFoundException e) {}
			catch(NoClassDefFoundError e) {
				//handle carefully
			}
			catch(UnsatisfiedLinkError e) {
				//handle carefully
			} 
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			finally{}			
		}
		
		return mClass;		
	}
		
	@SuppressWarnings("rawtypes")
	private static bMethod getbControllerInit(Class oClass, String sMethod){
		bMethod bundleControllerInit = null;
		
		boolean found = false;
		//create custom class loader
		try{				
			Method[] methods = oClass.getMethods();
			for(Method m:methods){
				Annotation[] mannotations = m.getAnnotations();
				for(Annotation annotation2: mannotations){
					if(annotation2 instanceof mControllerInit){
						if(m.getName().equals(sMethod)){
							bundleControllerInit = bMethod.encodeAsbMethod(m, oClass);
							found = true;
							break;
						}								
					}
				}
				if(found){
					break;
				}
			}	
		}
		catch(NoClassDefFoundError e) {
			//handle carefully
		}
		catch(UnsatisfiedLinkError e) {
			//handle carefully
		}			
		finally{}
		
		return bundleControllerInit;		
	}
	
	@SuppressWarnings("rawtypes")
	private static ArrayList<Contract> getbBundleEntityContracts(Class oClass){
		ArrayList<Contract> contracts = new ArrayList<Contract>();
		
		//create custom class loader
		try{
			Method[] methods = oClass.getMethods();
			for(Method m:methods){
				Annotation[] mannotations = m.getAnnotations();
				for(Annotation annotation2: mannotations){
					if(annotation2 instanceof mEntityContract){
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
		}
		catch(NoClassDefFoundError e) {
			//handle carefully
		}
		catch(UnsatisfiedLinkError e) {
			//handle carefully
		}			
		finally{}
		
		return contracts;		
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
