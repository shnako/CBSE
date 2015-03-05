package mcom.kernel.processor;

/**
 * Extracts all bundles (.jar files) in mCom BundleDir
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import mcom.kernel.util.KernelConstants;

import java.io.File;

public class BundleDirProcessor {

    public static File[] loadFilesInBundleDirectory() {
        File folder = new File(KernelConstants.BUNDLEDIR);

        return folder.listFiles();
    }

}
