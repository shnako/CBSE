package mcom.kernel.processor;

/**
 * This BundleAnnotationProcessor processes a class in a jar file to identify the following:
 * 1. The BundleController class - the class contains mBundleController annotation
 * 2. The BundleControllerInit method - the class contains mBundleControllerInit method annotation
 * 3. Contract methods - the class contains one or more contract
 * @see b_process
 *
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import StateAnnotations.mState;
import StateAnnotations.mStateType;
import mcom.bundle.Contract;
import mcom.bundle.annotations.mController;
import mcom.bundle.annotations.mControllerInit;
import mcom.bundle.annotations.mEntity;
import mcom.bundle.annotations.mEntityContract;
import mcom.bundle.util.bMethod;
import mcom.kernel.exceptions.ImpureBundleException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class BundleAnnotationProcessor {
    private Class bundleController;
    private bMethod bundleControllerInit;
    private Contract[] contracts;
    private mStateType stateType;

    /*
     * @throws: ImpureBundleException
     */
    public BundleAnnotationProcessor(Class oClass) {
        contracts = new Contract[0];
        b_process(oClass);

        if (bundleController == null && bundleControllerInit != null) {
            try {
                throw new ImpureBundleException(oClass.getName() + " has a bundleControllerInit without a bundleController");
            } catch (ImpureBundleException e) {
                e.printStackTrace();
            }
        } else if (bundleController != null && bundleControllerInit == null) {
            try {
                throw new ImpureBundleException(oClass.getName() + " has a bundleController without a bundleControllerInit");
            } catch (ImpureBundleException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * @throws: ImpureBundleException when more than one BundleController or BundleControllerInit is identified
     * @precondition: Object o != null
     */
    private void b_process(Class oClass) {

        Annotation[] oClassAnnotations = oClass.getAnnotations();
        for (Annotation annotation1 : oClassAnnotations) {
            if (annotation1 instanceof mController) {
                if (bundleController == null) {
                    bundleController = oClass;
                } else {
                    try {
                        throw new ImpureBundleException(oClass.getName() + " contains more than one Bundle controller");
                    } catch (ImpureBundleException e) {
                        e.printStackTrace();
                    }
                }

                Method[] methods = oClass.getMethods();
                for (Method m : methods) {
                    Annotation[] mannotations = m.getAnnotations();
                    for (Annotation annotation2 : mannotations) {
                        if (annotation2 instanceof mControllerInit) {
                            if (bundleControllerInit == null) {
                                bundleControllerInit = bMethod.encodeAsbMethod(m, bundleController);
                            } else {
                                try {
                                    throw new ImpureBundleException(oClass.getName() + " Bundle controller has more than one initialiser");
                                } catch (ImpureBundleException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } else if (annotation1 instanceof mEntity) { //class containing contract (method)
                Method[] methods = oClass.getMethods();
                for (Method m : methods) {
                    Annotation[] mannotations = m.getAnnotations();
                    for (Annotation annotation2 : mannotations) {
                        if (annotation2 instanceof mEntityContract) {

                            Contract contract = new Contract();
                            contract.setBundleEntity(oClass);
                            contract.setBundleEntityContract(bMethod.encodeAsbMethod(m, oClass));
                            String description = ((mEntityContract) annotation2).description();
                            contract.setDescription(description);
                            int contractType = ((mEntityContract) annotation2).contractType();
                            contract.setContractType(contractType);
                            contract.setReturnType(m.getReturnType().getName());

                            addContract(contract);
                        }
                    }
                }
            } else if (annotation1 instanceof mState) {
                stateType = ((mState) annotation1).stateType();
            }
        }
    }

    private void addContract(Contract c) {
        Contract[] c_temp = new Contract[contracts.length + 1];
        List<Contract> c_t = new LinkedList<Contract>();

        for (Contract ct : contracts) {
            if (ct != null) {
                c_t.add(ct);
            }
        }

        int i = 0;
        for (Contract ct : c_t) {
            c_temp[i] = ct;
            i = i + 1;
        }

        c_temp[i] = c;
        contracts = c_temp;
    }

    public Class getBundleController() {
        return bundleController;
    }

    public bMethod getBundleControllerInit() {
        return bundleControllerInit;
    }

    public Contract[] getContracts() {
        return contracts;
    }

    public mStateType getStateType() {
        return stateType;
    }
}
