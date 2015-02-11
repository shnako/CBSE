package mcom.kernel.impl;

import org.w3c.dom.Document;

import mcom.kernel.processor.BundleDescriptor;
import mcom.kernel.util.KernelUtil;

public class RemoteMComInvocation {

	/**
	 * Task 1
	 * This task is essential to complete the invoke command. 
	 * This method takes as parameter a text encoding for the remote invocation. You should uncomment the first line 
	 * of this function to study its content by using eclipse on debug mode.
	 * 
	 * To complete this task, you will need to carry out the following:
	 * 1) Get the object class from the BundleDescriptor. The object class is the bundle controller (i.e. a class annotated with @mController).
	 * 2) Identify the right method to call and associated parameters by comparing methods in object class with encoded contract in inv_doc.
	 * 3) When the right method is identified, call the method on an instance of the class and store in the returned object result.
	 * 
	 * You may find page 33 (invoking methods) of lecture slides on reflective and adaptive components useful to achieve this task.
	 * You may also want to investigate how class loader has been used in MCom to generate BundleDescriptors
	 * 
	 * @param inv_doc
	 * @return
	 */
	public static Object executeRemoteCall(Document inv_doc){
		//System.out.println(KernelUtil.prettyPrint(KernelUtil.getBDString(inv_doc))); //uncomment to study encoded call content		

		Object result = null; 
			
		//BundleDescriptor 
		String bundleId = inv_doc.getElementsByTagName("BundleId").item(0).getTextContent();
		BundleDescriptor bd = KernelUtil.loadBundleDescriptor(bundleId);

		
		
		return result;
	}
	

}
