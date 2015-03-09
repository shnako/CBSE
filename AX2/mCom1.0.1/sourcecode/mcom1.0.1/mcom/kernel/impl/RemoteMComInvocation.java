package mcom.kernel.impl;

import mcom.InvokeRequest;
import mcom.bundle.util.bMethod;
import mcom.kernel.processor.BundleDescriptor;
import mcom.kernel.util.BundleInstance;
import mcom.kernel.util.KernelUtil;
import mcom.kernel.util.StateManager;
import org.w3c.dom.Document;

import java.lang.reflect.Method;
import java.util.Arrays;

public class RemoteMComInvocation {

    /**
     * Task 1
     * This task is essential to complete the invoke command.
     * This method takes as parameter a text encoding for the remote invocation. You should uncomment the first line
     * of this function to study its content by using eclipse on debug mode.
     * <p/>
     * To complete this task, you will need to carry out the following:
     * 1) Get the object class from the BundleDescriptor. The object class is the bundle controller (i.e. a class annotated with @mController).
     * 2) Identify the right method to call and associated parameters by comparing methods in object class with encoded contract in inv_doc.
     * 3) When the right method is identified, call the method on an instance of the class and store in the returned object result.
     * <p/>
     * You may find page 33 (invoking methods) of lecture slides on reflective and adaptive components useful to achieve this task.
     * You may also want to investigate how class loader has been used in MCom to generate BundleDescriptors
     */
    public static Object executeRemoteCall(Document inv_doc) {
        //System.out.println(KernelUtil.prettyPrint(KernelUtil.getBDString(inv_doc))); //uncomment to study encoded call content

        Object result = null;
        Object instanceToInvoke;

        InvokeRequest invokeRequest;
        try {
            invokeRequest = KernelUtil.decodeInvokeRequest(inv_doc);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        BundleDescriptor bd = KernelUtil.loadBundleDescriptor(invokeRequest.getBundleId());
        Class bundleControllerClass = bd.getBundleController();

        bMethod requestedMethod = bd.getContracts()[0].getBundleEntityContract();
        String requestedMethodName = bd.getContracts()[0].getBundleEntityContract().getMethodName();

        Class[] requestedPars = new Class[requestedMethod.getbParameters().length];
        for (int i = 0; i < requestedPars.length; i++) {
            try {
                requestedPars[i] = Class.forName(requestedMethod.getbParameters()[i].getClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        Method methodToInvoke = null;

        for (Method method : bundleControllerClass.getMethods()) {
            if (method.getName().equals(requestedMethodName) && Arrays.equals(method.getParameterTypes(), requestedPars)) {
                methodToInvoke = method;
                break;
            }
        }

        BundleInstance bundleInstance = StateManager.getStateManager().getInstance(invokeRequest.getBundleId());

        try {
            if (bundleInstance != null) {
                instanceToInvoke = bundleInstance.getInstance();
            } else {
                instanceToInvoke = bundleControllerClass.newInstance();
            }

            //noinspection ConstantConditions
            result = methodToInvoke.invoke(instanceToInvoke, invokeRequest.getParameters());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}
