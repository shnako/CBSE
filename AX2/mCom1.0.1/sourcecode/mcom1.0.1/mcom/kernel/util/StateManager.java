package mcom.kernel.util;

import StateAnnotations.mStateType;

import java.util.HashMap;

public final class StateManager {
    private static StateManager stateManager;
    private HashMap<Integer, BundleInstance> statefulInstances;


    private StateManager() {
        statefulInstances = new HashMap<Integer, BundleInstance>();
    }

    public static StateManager getStateManager() {
        if (stateManager == null) {
            stateManager = new StateManager();
        }
        return stateManager;
    }

    public BundleInstance getInstance(int bundleId) {
        return statefulInstances.get(bundleId);
    }

    public void addInstance(BundleInstance bundleInstance) {
        if (getInstance(bundleInstance.getBundleId()) == null) {
            statefulInstances.put(bundleInstance.getBundleId(), bundleInstance);
        }
    }

    public static void loadOrCreateBundleInstance(int bundleId, Class bundleController, mStateType stateType) {
        try {
            // Try to get the instance from memory.
            BundleInstance bundleInstance = StateManager.getStateManager().getInstance(bundleId);
            // Try to get the instance state from disk.
            if (bundleInstance == null && stateType == mStateType.PERSISTANT) {
                bundleInstance = BundleInstance.loadPersistentInstance(bundleId, stateType, bundleController.newInstance());
                if (bundleInstance != null) {
                    StateManager.getStateManager().addInstance(bundleInstance);
                }
                else {
                    Object instance = bundleController.newInstance();
                    bundleInstance = new BundleInstance(bundleId, stateType, instance);
                    bundleInstance.persist();
                    StateManager.getStateManager().addInstance(bundleInstance);
                }
            }
            // Try to create a new stateful instance.
            if (bundleInstance == null && stateType == mStateType.STATEFUL) {
                Object instance = bundleController.newInstance();
                bundleInstance = new BundleInstance(bundleId, stateType, instance);
                StateManager.getStateManager().addInstance(bundleInstance);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void removeInstanceFromMemory(int bundleId) {
        statefulInstances.remove(bundleId);
    }

    public void removeInstanceFromMemoryAndDisk(int bundleId) {
        BundleInstance bundleInstance = statefulInstances.get(bundleId);
        if (bundleInstance == null) return;

        if (bundleInstance.getStateType().equals(mStateType.PERSISTANT)) {
            bundleInstance.removeStateFromDisk();
        }

        removeInstanceFromMemory(bundleId);
    }
}
