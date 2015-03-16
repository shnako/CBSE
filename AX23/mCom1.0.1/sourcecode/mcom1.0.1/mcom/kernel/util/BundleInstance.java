package mcom.kernel.util;

import StateAnnotations.mStateType;

import java.io.*;

public class BundleInstance {
    private mStateType stateType;
    private Object instance;
    private BundleInstanceState bundleInstanceState;

    public BundleInstance(mStateType stateType) throws IllegalStateException {
        if (stateType == mStateType.STATELESS) {
            throw new IllegalStateException("You cannot instantiate a stateless bundle!");
        }

        this.stateType = stateType;
    }

    public BundleInstance(int bundleId, mStateType stateType, Object instance) throws IllegalStateException {
        this(stateType);
        this.instance = instance;
        this.bundleInstanceState = new BundleInstanceState(bundleId, 0);
    }

    public int getBundleId() {
        return bundleInstanceState.getBundleId();
    }

    public mStateType getStateType() {
        return stateType;
    }

    public Object getInstance() {
        System.out.println(stateType + " bundle used " + bundleInstanceState.incrementCallCount() + " times");
        if (stateType == mStateType.PERSISTANT) {
            persist();
        }
        return instance;
    }

    public void persist() throws IllegalStateException {
        if (stateType != mStateType.PERSISTANT) {
            throw new IllegalStateException("Cannot persist a " + stateType + " bundle. It must be " + mStateType.PERSISTANT + "!");
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(getBundlePersistentFilePath(bundleInstanceState.getBundleId()));
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            try {
                out.writeObject(bundleInstanceState);
            } finally {
                out.close();
                fileOut.close();
            }
        } catch (Exception ex) {
            System.err.println("Could not persist instance: " + ex.getMessage());
        }
    }

    public void removeStateFromDisk() throws IllegalStateException {
        if (stateType != mStateType.PERSISTANT) {
            throw new IllegalStateException("Cannot remove from disk a " + stateType + " bundle. It must be " + mStateType.PERSISTANT + "!");
        }

        try {
            File file = new File(getBundlePersistentFilePath(bundleInstanceState.getBundleId()));
            if (!file.delete()) {
                throw new IOException("could not delete file!");
            }
        } catch (Exception ex) {
            System.err.println("Could not remove instance " + getBundleId() + " from disk: " + ex.getMessage());
        }
    }

    public static BundleInstance loadPersistentInstance(int bundleId, mStateType stateType, Object instance) {
        File instanceFile = new File(getBundlePersistentFilePath(bundleId));
        if (!instanceFile.exists()) {
            return null;
        }

        BundleInstance bundleInstance = new BundleInstance(bundleId, stateType, instance);
        try {
            FileInputStream fileIn = new FileInputStream(instanceFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            try {
                bundleInstance.bundleInstanceState = (BundleInstanceState) in.readObject();
            } finally {
                in.close();
                fileIn.close();
            }
        } catch (Exception ex) {
            System.err.println("Instance file exists (" + instanceFile.getAbsolutePath() + ") but it could not be parsed.");
            return null;
        }

        return bundleInstance;
    }

    private static String getBundlePersistentFilePath(int bundleId) {
        return KernelConstants.BUNDLEPERSISTENTDIR + "/" + bundleId + KernelConstants.BUNDLEPERSISTENTFILEEXT;
    }
}