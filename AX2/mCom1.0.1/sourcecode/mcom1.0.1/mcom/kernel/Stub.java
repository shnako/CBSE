package mcom.kernel;

public interface Stub {
	public boolean deploy();
	public boolean advertise(int bundleId);
	public void remoteLookup();
	public void localLookup();
	public void invoke();
}
