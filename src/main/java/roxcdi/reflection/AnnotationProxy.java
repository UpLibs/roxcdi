package roxcdi.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AnnotationProxy {

	@SuppressWarnings("unchecked")
	static public Annotation[] asAnnotations(Class<? extends Annotation>... annotationsClasses) {
		Annotation[] a = new Annotation[ annotationsClasses.length ] ;
		
		for (int i = 0; i < a.length; i++) {
			a[i] = asAnnotation( annotationsClasses[i] ) ;
		}
		
		return a ;
	}
	
	@SuppressWarnings("unchecked")
	static public <T extends Annotation> T asAnnotation(final Class<T> annotationClass) {
		
		T instance = (T) Proxy.newProxyInstance(AnnotationProxy.class.getClassLoader(), new Class<?>[] {annotationClass}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				
				if ( method.getName().equals("annotationType") ) {
					return annotationClass ; 
				}
				else if ( method.getName().equals("hashCode") ) {
					return proxy.hashCode() ; 
				}
				else if ( method.getName().equals("equals") ) {
					return proxy.equals(args[0]) ; 
				}
				else if ( method.getName().equals("toString") ) {
					return annotationClass.getName() ; 
				}
				
				throw new UnsupportedOperationException("Can't handler Annotation method: "+ method) ;
			}
		}) ;
    	
		return instance ;
	}
	
}
