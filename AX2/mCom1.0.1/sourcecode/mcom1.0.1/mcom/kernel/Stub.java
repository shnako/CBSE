package mcom.kernel;

public interface Stub {
	public boolean deploy();
	public void undeploy(int bundleId);
	public boolean advertise(int bundleId, boolean isAdvertised);
	public void remoteLookup();
	public void localLookup();
	public void invoke();
}
