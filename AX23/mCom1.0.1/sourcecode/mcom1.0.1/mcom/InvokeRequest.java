package mcom;

import javafx.util.Pair;

import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class InvokeRequest {
    private String bundleHostIp;
    private int bundleHostPort;
    private int bundleId;
    private String contractName;

    public InvokeRequest() {
        this.parameters = new ArrayList<Pair<Class, String>>();
    }

    private ArrayList<Pair<Class, String>> parameters;

    public void setBundleHostIp(String bundleHostIp) {
        this.bundleHostIp = bundleHostIp;
    }

    public void setBundleHostPort(int bundleHostPort) {
        this.bundleHostPort = bundleHostPort;
    }

    public void setBundleId(int bundleId) {
        this.bundleId = bundleId;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public void addParameter(Pair<Class, String> parameter) {
        this.parameters.add(parameter);
    }

    public String getBundleHostIp() {

        return bundleHostIp;
    }

    public int getBundleHostPort() {
        return bundleHostPort;
    }

    public int getBundleId() {
        return bundleId;
    }

    public String getContractName() {
        return contractName;
    }

    public ArrayList<Pair<Class, String>> getParameterPairs() {
        return parameters;
    }

    public String[] getParameters() {
        ArrayList<String> result = new ArrayList<String>(parameters.size());
        for (Pair pair : parameters) {
            result.add("" + pair.getValue());
        }

        return result.toArray(new String[result.size()]);
    }

    public Pair<Class, String> getParameterByIndex(int index) {
        return parameters.get(index);
    }
}
