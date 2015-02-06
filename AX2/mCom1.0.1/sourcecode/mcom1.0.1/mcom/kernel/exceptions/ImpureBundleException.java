package mcom.kernel.exceptions;

/**
 * ImpureBundleException is thrown when a jar file exhibit any of the following xteristics:
 * 1. contains more than one BundleController
 * 2. contains more than one BundleControllerInit
 * 3. contains a BundleController without BundleControllerInit
 * 4. contains a BundleControllerInit without BundleController 
 * 
 * @author Inah Omoronyia
 */
public class ImpureBundleException extends Exception{
		
	private static final long serialVersionUID = 1L;

	public ImpureBundleException() { 
		super(); 
	}
	
	public ImpureBundleException(String message) {
		super(message); 
	}
	
	public ImpureBundleException(String message, Throwable cause) {
		super(message, cause); 
	}
	
	public ImpureBundleException(Throwable cause) { 
		super(cause); 
	}
}
