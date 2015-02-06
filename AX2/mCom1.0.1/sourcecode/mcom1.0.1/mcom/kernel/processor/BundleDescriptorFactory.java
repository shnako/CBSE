package mcom.kernel.processor;

/**
 * BundleDescriptorBuilder generates a BundleDescriptor for each bundle in BundleDir
 * This is achieved based on the following two steps:
 * STEP1. Generate set of mComProcessors for each mBundle:
 * 		(a) For each mBundle, generate a list of mBundleAnnotationProcessors. 
 * 		(b) The # of mBundleAnnotationProcessors = # of classes in a mBundle
 * 
 * STEP2. Validate each set of mComProcessors: 
 * 		- A set is valid if the following conditions are satisfied:
 * 		  (a) it contains at most one bundleController
 * 		  (b) it contains at least one contract * 
 * 		- If a set is valid, define a BundleDescriptor
 * 		- Each mBundle has only one deployment descriptor
 * 
 * @Author Inah Omoronyia
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import mcom.bundle.Contract;
import mcom.bundle.util.bMethod;
import mcom.init.Initialiser;
import mcom.kernel.util.KernelUtil;
import mcom.wire.impl.ReceiverImpl;

public class BundleDescriptorFactory {

	@SuppressWarnings("rawtypes")
	public static void buildBundleDescriptors() throws MalformedURLException{
				
		File[] mBundles = BundleDirProcessor.loadFilesInBundleDirectory();//list of mBundles (files) in BundleDir
		
		//STEP 1
		//ArrayList<BundleDescriptor> bundleDescriptors = new ArrayList<BundleDescriptor>();	
		File [] invalidBundles = new File[0];

		for(File mBundle:mBundles){ 
			System.out.println("Deploying bundle: "+mBundle.getName()+" ...");			
			
			String [] mBundleClassFiles = new BundleJarProcessor(mBundle).getClassFiles();	//list of classes in mBundle					
						
			//create custom class loader
			URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
			BundleClassLoader mBundleClassLoader = new BundleClassLoader(loader.getURLs());
			String path = mBundle.getAbsolutePath();
			path = "jar:file://"+ path+"!/";
			mBundleClassLoader.addURL(new URL(path));		
//						
			ArrayList<BundleAnnotationProcessor> mBundleProcessors = new ArrayList<BundleAnnotationProcessor>(); //
			
			for(String cf: mBundleClassFiles){				
				try {
					
					Class oClass = Class.forName(cf, true, mBundleClassLoader);
					BundleAnnotationProcessor mbap = new BundleAnnotationProcessor(oClass);
					mBundleProcessors.add(mbap);	
				}
				catch (ClassNotFoundException e) {}
				catch(NoClassDefFoundError e) {
					//handle carefully
				}
				catch(UnsatisfiedLinkError e) {
					//handle carefully
				}
				finally{}
			}
			
			//STEP 2
			boolean isValid = true;
			
			Class bundleController = null;
			bMethod bundleControllerInit = null;
			Contract [] contracts = new Contract[0];
			
			for(BundleAnnotationProcessor bap:mBundleProcessors){
				if(bap.getBundleController() != null){
					if(bundleController == null){
						bundleController = bap.getBundleController();
					}
					else{
						isValid = false;
					}
				}
				
				if(bap.getBundleControllerInit() != null){
					if(bundleControllerInit == null){
						bundleControllerInit = bap.getBundleControllerInit();
					}
					else{
						isValid = false;
					}
				}				
				
				if(bap.getContracts().length >0){
					//add contract to contracts
					for(Contract contract1: bap.getContracts()){
						Contract [] c_temp = new Contract[contracts.length+1];
						List<Contract> c_t = new LinkedList<Contract>();
						
						for(Contract ct: contracts){
							if(ct !=null){
								c_t.add(ct);
							}
						}
						
						int i = 0;
						for(Contract ct: c_t){
							c_temp[i] = ct;
							i = i +1;
						}
						
						c_temp[i] = contract1;
						contracts = c_temp;	
					}					
				}
			}
			
			if(isValid){
				//create BundleDescriptor, add to bundleDescriptors
				BundleDescriptor bd = new BundleDescriptor();
				bd.setBundleName(mBundle.getName());
				
				Random rn = new Random();
				int bundleId = rn.nextInt(100); //TODO: Non-synchronised bundleId: refactor to avoid replication across platform
				bd.setBundleId(bundleId);
				
				if(Initialiser.local_address !=null){
					bd.setAddress(Initialiser.local_address);					
				}
				if(ReceiverImpl.listenSocket !=null){
					bd.setPort(ReceiverImpl.listenSocket.getLocalPort());					
				}
				bd.setBundleController(bundleController);
				bd.setBundleControllerInit(bundleControllerInit);
				bd.setContract(contracts);
				
				KernelUtil.storeBundleDescriptor(bd);
				//bundleDescriptors.add(bd);
			}
			else{
				//add mBundle to invalidBundles
				File [] c_temp = new File[invalidBundles.length+1];
				List<File> c_t = new LinkedList<File>();
				
				for(File ct: invalidBundles){
					if(ct !=null){
						c_t.add(ct);
					}
				}
				
				int i = 0;
				for(File ct: c_t){
					c_temp[i] = ct;
					i = i +1;
				}
				
				c_temp[i] = mBundle;
				invalidBundles = c_temp;					
			}					
		}
		
		if(invalidBundles.length >0){
			System.err.println("Invalid bundles");
			for(File f: invalidBundles){
				System.err.println(f.getName());
			}	
		}		
	}
	
	
}
