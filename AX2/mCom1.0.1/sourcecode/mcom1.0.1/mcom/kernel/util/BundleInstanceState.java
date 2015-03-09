package mcom.kernel.util;

import java.io.Serializable;
import java.util.HashMap;

public class BundleInstanceState implements Serializable {
    private int bundleId;
    private int callCount;
    private HashMap<String, Serializable> customBundleData;

    public BundleInstanceState() {
        customBundleData = new HashMap<String, Serializable>();
    }

    public BundleInstanceState(int bundleId, int callCount) {
        this();
        this.bundleId = bundleId;
        this.callCount = callCount;
    }

    public BundleInstanceState(int bundleId, int callCount, HashMap<String, Serializable> customBundleData) {
        this();
        this.bundleId = bundleId;
        this.callCount = callCount;
        this.customBundleData = customBundleData;
    }

    public int getBundleId() {
        return bundleId;
    }

    public void setBundleId(int bundleId) {
        this.bundleId = bundleId;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    public int incrementCallCount() {
        return ++this.callCount;
    }

    public HashMap<String, Serializable> getCustomBundleData() {
        return customBundleData;
    }

    public void setCustomBundleData(HashMap<String, Serializable> customBundleData) {
        this.customBundleData = customBundleData;
    }

    public void addCustomBundleData(String key, Serializable value) {
        customBundleData.put(key, value);
    }
}
