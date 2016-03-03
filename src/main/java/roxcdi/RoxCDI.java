package roxcdi ;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import roxcdi.parameter.Property;

public class RoxCDI {
	
	final static public Property PROPERTY_CDI_INSTANTIATOR = Property.fromPackage("instantiator") ;
	final static public Property PROPERTY_WELD_AUTOLOAD = Property.fromPackage("weld.autoload",Boolean.class).withDefault(true) ;
	
	final static private RoxCDI roxCDI = new RoxCDI() ;
	
	static public <T extends CDI<?>> T getCDI() {
		return roxCDI.get() ;
	}
	
	static public <T extends CDI<?>> T getCDI_IfInitialized() {
		return roxCDI.getIfInitialized();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public <U> Instance<U> select(Class<U> subtype) {
		return getCDI().select((Class)subtype) ;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public <U> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
		return getCDI().select((Class)subtype, qualifiers) ;
	}
	
	@SuppressWarnings("unchecked")
	static public <T> Instance<T> select(Annotation subtype, Annotation qualifiers) {
		return (Instance<T>) getCDI().select(subtype, qualifiers) ;
	}
	
	@SuppressWarnings("unchecked")
	static public <T> Instance<T> select(Annotation... qualifiers) {
		return (Instance<T>) getCDI().select(qualifiers) ;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private RoxCDI() {
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CDI<?>> T get() {
		
		CDI<?> cdi = null ;
		
		try {
			cdi = CDI.current() ;
		}
		catch (IllegalStateException e) {}
		
		if (cdi == null) {
			cdi = getCDIInstantiated(true) ;	
		}
		
		if (cdi != null) {
			return (T)cdi ; 
		}
		else {
			return null ;	
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CDI<?>> T getIfInitialized() {
		
		CDI<?> cdi = null ;
		
		try {
			cdi = CDI.current() ;
		}
		catch (IllegalStateException e) {}
		
		if (cdi == null) {
			cdi = getCDIInstantiated(false) ;	
		}
		
		if (cdi != null) {
			return (T)cdi ; 
		}
		else {
			return null ;	
		}
	}
	
	private CDI<?> cdiInstantiated = null ;
	
	private CDI<?> getCDIInstantiated(boolean autoCreate) {
		if (cdiInstantiated == null && autoCreate) {
			synchronized (this) {
				if (cdiInstantiated != null) return cdiInstantiated ;
				cdiInstantiated = instantiateCDI() ;
			}
		}
		return cdiInstantiated ;
	}
	
	private CDI<?> instantiateCDI() {
		
		String instantiatorClassName = PROPERTY_CDI_INSTANTIATOR.get() ;
		
		if (instantiatorClassName != null && !instantiatorClassName.isEmpty()) {
			
			try {
				CDI<?> cdi = instantiateCDIWithClass(instantiatorClassName);
				return cdi ;
			}
			catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException("Can't automatically instantiate CDI. Can't find class set at property: "+ PROPERTY_CDI_INSTANTIATOR.getName() +"="+ instantiatorClassName, e);
			}
			catch (InstantiationException | IllegalAccessException e) {
				throw new UnsupportedOperationException("Can't automatically instantiate CDI. Error calling instantiator set by property: "+ PROPERTY_CDI_INSTANTIATOR.getName() +"="+ instantiatorClassName, e);
			}
			
		}
		else if ( isWeldPresentAndAllowed() ) {
			CDI<?> cdi = instantiateWeld() ;
			return cdi ;
		}
		
		throw new UnsupportedOperationException("Can't automatically instantiate CDI. Shoud instantiate CDI before call any RoxCDI method, or use RoxCDIInstantiator implementation at property: "+ PROPERTY_CDI_INSTANTIATOR.getName());
	}

	private CDI<?> instantiateWeld() {
		try {
			Class<?> weldClass = Class.forName("org.jboss.weld.environment.se.Weld") ;
			
			Object weld = weldClass.newInstance() ;
			
			Method methodInitialize = weldClass.getMethod("initialize") ;
			
			CDI<?> cdi = (CDI<?>) methodInitialize.invoke(weld) ;
			return cdi ;
		}
		catch (Exception e) {
			e.printStackTrace(); 
			return null ;
		}
	}

	private boolean isWeldPresentAndAllowed() {
		try {
			Class<?> weldClass = Class.forName("org.jboss.weld.environment.se.Weld") ;
			
			if (weldClass != null) {
				try {
					Boolean allow = PROPERTY_WELD_AUTOLOAD.getBoolean();
					return allow != null && allow ;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				return false ;
			}
			
			return false ;
		}
		catch (Exception e) {
			return false ;
		}
	}

	@SuppressWarnings("unchecked")
	private CDI<?> instantiateCDIWithClass(String instantiatorClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<RoxCDIInstantiator> instantiatorClass = (Class<RoxCDIInstantiator>) Class.forName(instantiatorClassName, true, RoxCDI.class.getClassLoader()) ;
		
		RoxCDIInstantiator instantiator = instantiatorClass.newInstance() ;
		
		return instantiator.instantiateCDI() ;
	}
	
	/////////////////////////////////////////////////////
	
	static public boolean shutdownCDI() {
		CDI<?> cdi = getCDI_IfInitialized() ;
		return shutdown(cdi) ;
	}
	
	static public boolean shutdown(CDI<?> cdi) {
		if (cdi == null) return false ;
	
		if ( isWeldContainer(cdi) ) {
			return shutdownWeld(cdi) ;
		}
		else {
			return shutdownGeneric(cdi) ;
		}
		
	}
	
	private static boolean shutdownWeld(CDI<?> cdi) {
		
		try {
			Object ret = callMethod(cdi, "shutdown") ;
			if ( ret == Boolean.TRUE ) return true ;
			if ( ret instanceof Exception ) throw (Exception)ret ;
			
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			
			return false ;
		}
		
	}

	private static boolean isWeldContainer(CDI<?> cdi) {
		String className = cdi.getClass().getName() ;
		
		return className.startsWith("org.jboss.weld.") ;
	}
	
	private static boolean shutdownGeneric(CDI<?> cdi) {

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

	@SuppressWarnings("rawtypes")
	private static Object callMethod(CDI<?> cdi, String methodName) {
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
