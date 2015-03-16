package mcom.bundle;

/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow
 */
public class ContractType {
	public static final int GET = 0;
	public static final int POST = 1;
	
//	public static final int REQUEST = 2;
//	public static final int CONSENT = 3;
//	public static final int ACK = 4;
//	public static final int SEND = 5;
	
	public static final int TERMINATE = -1;
	
	public static String getType(int type){
		String t = "UNKNOWN";
		
		switch(type){
		case 0:
			t = "GET";
			break;
		case 1:
			t = "POST";
			break;
		case -1:
			t = "TERMINATE";
			break;
		}		
		return t;
	}
	
}
