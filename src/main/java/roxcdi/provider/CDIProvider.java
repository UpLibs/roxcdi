package roxcdi.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.CDI;

abstract public class CDIProvider {

	final protected CDI<?> cdi ;

	public CDIProvider(CDI<?> cdi) {
		this.cdi = cdi;
	}
	
	public CDI<?> getCDI() {
		return cdi;
	}

	////////////////////////////////////////////////////////////////////
	
	abstract public void startContexts() ;
	
	abstract public void startContext( Class<? extends Annotation> contextType ) ;
	
	abstract public void stopContext( Class<? extends Annotation> contextType ) ;

	abstract public boolean ensureConstructed(Object obj) ;
	
	abstract public boolean shutdown() ;
	
	////////////////////////////////////////////////////////////////////

	@SuppressWarnings("rawtypes")
	protected static Object callMethod(CDI<?> cdi, String methodName) {
		Class<? extends CDI> clazz = cdi.getClass() ;
		
		Method method = null ;
		try {
			method = clazz.getMethod(methodName) ;
		}
		catch (NoSuchMethodException e) {}
		
		if (method != null) {
			try {
				method.invoke(cdi) ;
				return Boolean.TRUE ;
			}
			catch (Exception e) {
				return e ;
			}
		}
		
		return Boolean.FALSE ;
	}

	
}
