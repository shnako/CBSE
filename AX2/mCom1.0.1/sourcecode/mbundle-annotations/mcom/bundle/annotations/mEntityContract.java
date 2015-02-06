package mcom.bundle.annotations;
/**
 *  @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface mEntityContract {
	String description() default "EMPTY";
	int contractType();
}
