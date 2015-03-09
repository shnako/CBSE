package org.gla.mcom.init;

public aspect Security {
	pointcut methodCallPointcut():
//		call(void mcom.kernel.impl.StubImpl.sendRemoteInvocation(..));
		call(void mcom.kernel.impl.StubImpl.invoke());
	before() : methodCallPointcut(){
		System.out.println("Initiating Security Check");
		
		// retrive bundleId, parameters etc		
		// sendRemoteInvocation(bundleId, contractName, parameters, header);
		
		//perform security check
		
		// if(valid){proceed}else{return null;}
		}
	}
