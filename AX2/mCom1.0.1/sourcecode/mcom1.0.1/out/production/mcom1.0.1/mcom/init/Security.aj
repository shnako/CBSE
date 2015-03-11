package org.gla.mcom.init;

import java.util.HashSet;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class Security {

	private HashSet<String> authorisedUsers;
	
		@Pointcut("call(* mcom.wire.impl.ReceiverImpl.authoriseUser(..))")
		public void addUser(){}
	
		
		@Pointcut("call(* mcom.kernel.impl.StubImpl.sendRemoteInvocation(..))")
		public void securityCheck() { }
		
		
		@Around("addUser()")
		public void upgradeUser(ProceedingJoinPoint pjp) throws Throwable {
			System.out.println("Upgrading user");
			Object[] args = pjp.getArgs();
			for(Object arg: args){
				authorisedUsers.add((String)arg);
			}		
		}
		
		
		@Around("securityCheck()")
		public Object profile(ProceedingJoinPoint pjp) throws Throwable {
		Object[] args = pjp.getArgs();
		boolean hasAccess = false;
		for(Object arg : args){
			if (authorisedUsers.contains(arg)){
				hasAccess = true;
			}
			
		}
		if(hasAccess){
			System.out.println("Can Proceed ");
			pjp.proceed();
		}
		else
			System.out.println("Cannot proceed, you do not have access");
			return null;
			}
		}