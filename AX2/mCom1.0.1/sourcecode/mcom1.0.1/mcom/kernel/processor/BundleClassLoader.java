package mcom.kernel.processor;
/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import java.net.URL;
import java.net.URLClassLoader;

public class BundleClassLoader extends URLClassLoader{

	public URLClassLoader urlClassLoader;	
	/**
     * @param urls, to carry forward the existing classpath.
     */
    public BundleClassLoader(URL[] urls) {
        super(urls);
    }
     
    @Override
    /**
     * add classpath to the loader.
     */
    public void addURL(URL url) {
        super.addURL(url);
    }	
}
