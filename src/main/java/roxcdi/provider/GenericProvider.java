package roxcdi.provider;

import java.lang.annotation.Annotation;
import java.util.WeakHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;

final public class GenericProvider extends CDIProvider {
	
	static public final GenericProvider DUMMY = new GenericProvider(null) ;
	
	static private WeakHashMap<CDI<?>, GenericProvider> instances = new WeakHashMap<>() ;
	static public GenericProvider instance(CDI<?> cdi) {
		if (cdi == null) return DUMMY ;
		
		synchronized (instances) {
			GenericProvider provider = instances.get(cdi) ;
			if (provider == null) instances.put(cdi, provider = new GenericProvider(cdi) ) ;
			return provider ;
		}
	}
	
	/////////////////////////////////////////////////////////////////
	
	public GenericProvider(CDI<?> cdi) {
		super(cdi);
	}

	@Override
	public void startContexts() {
		startContext(ApplicationScoped.class);
		startContext(RequestScoped.class);
	}

	@Override
	public void startContext(Class<? extends Annotation> contextType) {
		throw new UnsupportedOperationException("startContext: "+ contextType) ;
	}

	@Override
	public void stopContext(Class<? extends Annotation> contextType) {
		throw new UnsupportedOperationException("stopContext: "+ contextType) ;
	}

	@Override
	public boolean ensureConstructed(Object obj) {
		obj.toString();
		return true;
	}
	
	@Override
	public boolean shutdown() {

		try {
			Exception error1 = null ;
			Exception error2 = null ;
			
			{
				Object ret = callMethod(cdi, "shutdown") ;
				if ( ret == Boolean.TRUE ) return true ;
				if ( ret instanceof Exception ) error1 = (Exception) ret ;
			}
			
			{
				Object ret = callMethod(cdi, "close") ;
				if ( ret == Boolean.TRUE ) return true ;
				if ( ret instanceof Exception ) error2 = (Exception) ret ;
			}
			
			if (error1 != null)	throw error1 ;
			if (error2 != null)	throw error2 ;
			
			return false ;
		}
		catch (Exception e) {
			e.printStackTrace();
			
			return false ;
		}
		
	}
	
}
