package mcom.kernel.processor;

/**
 * Extracts all bundles (.jar files) in mCom BundleDir
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import java.io.File;

import mcom.kernel.util.KernelConstants;

public class BundleDirProcessor {

	public static File[] loadFilesInBundleDirectory(){
		File folder = new File(KernelConstants.BUNDLEDIR);
		File[] listOfFiles = folder.listFiles();
		
		return listOfFiles;
	}

}
