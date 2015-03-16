package mcom.bundle.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class bMethod implements Serializable{
	private static final long serialVersionUID = -3207106659318449241L;
	
	private String className;
	private String methodName;
	private bParameter [] bparameters = new bParameter[0];
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public bParameter [] getbParameters() {
		return bparameters;
	}
	public void setbParameters(bParameter [] bparameters) {
		this.bparameters = bparameters;
	}
	
	public void updatebParameter(bParameter oldb, bParameter newb){
		bParameter [] p_temp = new bParameter[bparameters.length];
		List<bParameter> p_t = new LinkedList<bParameter>();
		
		for(bParameter pt: bparameters){
			if(pt !=null){
				p_t.add(pt);
			}
		}
		
		int i = 0;
		for(bParameter pt: p_t){
			if(pt.getClassName().equals(oldb.getClassName())){
				p_temp[i] = newb;	
			}
			else{
				p_temp[i] = pt;
			}			
			i = i +1;
		}
		
		bparameters = p_temp;	
	}
	
	public void addbParameter(bParameter p){
		bParameter [] p_temp = new bParameter[bparameters.length +1];
		List<bParameter> p_t = new LinkedList<bParameter>();
		
		for(bParameter pt: bparameters){
			if(pt !=null){
				p_t.add(pt);
			}
		}
		
		int i = 0;
		for(bParameter pt: p_t){
			p_temp[i] = pt;
			i = i +1;
		}
		
		p_temp[i] = p;
		bparameters = p_temp;
	}	
	
	@SuppressWarnings("rawtypes")
	public static bMethod encodeAsbMethod(Method method, Class mclass){
		bMethod bmethod = new bMethod();
		bmethod.setMethodName(method.getName());
		bmethod.setClassName(mclass.getName());
		
		for(Parameter p:method.getParameters()){
			bParameter bp = bParameter.encodeAsbParameter(p);
			bmethod.addbParameter(bp);
		}
		return bmethod;
	}
	
	public boolean sameas(bMethod method){
		boolean same = true;
		if(!this.className.equals(method.getClassName())){
			same = false;
		}
		else if(!this.methodName.equals(method.getMethodName())){
			same = false;
		}
		ArrayList<Boolean> pcheck = new ArrayList<Boolean>();
		for(bParameter p: bparameters){
			boolean ts = false;
			for(bParameter p1: method.getbParameters()){
				boolean ps = p.sameAs(p1);
				if(ps){
					ts = true;
					break;
				}
			}
			pcheck.add(ts);
		}
		
		for(boolean b: pcheck){
			if(!b){
				same = false;
				break;
			}
		}
		
		return same;
	}
}
