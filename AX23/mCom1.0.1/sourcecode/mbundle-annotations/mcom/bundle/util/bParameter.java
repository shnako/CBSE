package mcom.bundle.util;

import java.io.Serializable;
import java.lang.reflect.Parameter;

public class bParameter implements Serializable{
	private static final long serialVersionUID = 7737086120258757092L;
	
	private String className;
	private Object value;		

	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public static bParameter encodeAsbParameter(Parameter parameter){
		bParameter bp = new bParameter();
		bp.setClassName(parameter.getType().getName());
		
		return bp;
	}
	
	public boolean sameAs(bParameter parameter){
		boolean same = true;
		if(!this.className.equals(parameter.getClassName())){
			same = false;
		}
		//else if(this.value != parameter.getValue()){
		//	same = false;
		//}
		
		return same;
	}
}	
