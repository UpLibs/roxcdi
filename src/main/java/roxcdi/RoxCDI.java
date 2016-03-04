package roxcdi ;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import roxcdi.parameter.Property;

public class RoxCDI {
	
	private static final String WELD_PROXY_OBJECT_CLASS_NAME = "org.jboss.weld.bean.proxy.ProxyObject";
	private static final String WELD_CLASS_NAME = "org.jboss.weld.environment.se.Weld";
	private static final String WELD_PACKAGE = "org.jboss.weld.";
	
	final static public Property PROPERTY_CDI_INSTANTIATOR = Property.fromPackage("instantiator") ;
	final static public Property PROPERTY_WELD_AUTOLOAD = Property.fromPackage("weld.autoload",Boolean.class).withDefault(true) ;
	
	final static private RoxCDI roxCDI = new RoxCDI() ;
	
	static public <T extends CDI<?>> T getCDI() {
		return roxCDI.get() ;
	}
	
	static public <T extends CDI<?>> T getCDI_IfInitialized() {
		return roxCDI.getIfInitialized();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <U> U getBean(Class<U> subtype) {
		Instance<U> instance = getCDI().select((Class)subtype) ;
		return instance != null ? instance.get() : null ;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <U> U getBean(Class<U> subtype, Annotation... qualifiers) {
		Instance<U> instance = getCDI().select((Class)subtype, qualifiers) ;
		return instance != null ? instance.get() : null ;
	}
	
	@SuppressWarnings({ "unchecked" })
	static public <U> U getBean(Annotation subtype, Annotation qualifiers) {
		Instance<U> instance = (Instance<U>) getCDI().select(subtype, qualifiers) ;
		return instance != null ? instance.get() : null ;
	}
	
	@SuppressWarnings({ "unchecked" })
	static public <U> U getBean(Annotation... qualifiers) {
		Instance<U> instance = (Instance<U>) getCDI().select(qualifiers) ;
		return instance != null ? instance.get() : null ;
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
			Class<?> weldClass = Class.forName(WELD_CLASS_NAME) ;
			
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
			Class<?> weldClass = Class.forName(WELD_CLASS_NAME) ;
			
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
	
	static public boolean ensureConstructed(Object obj) {
		if (obj == null) return false ;
		
		if ( isFromWeldProxy(obj) ) {
			try {
				return ensureConstructed_WeldProxy(obj);
			}
			catch (Exception e) {
				e.printStackTrace();
				return false ;
			}
		}
		else {
			return ensureConstructed_Generic(obj);
		}
		
	}
	
	static private boolean ensureConstructed_Generic(Object obj) {
		obj.toString() ;
		return true ;
	}

	static private boolean ensureConstructed_WeldProxy(Object obj) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Class<? extends Object> clazz = obj.getClass() ;
		
		Class<?> weldProxySuperclass = getWeldProxySuperclass(clazz);
		
		Method method_getHandler = weldProxySuperclass.getMethod("getHandler") ;
		
		Object handler = method_getHandler.invoke(obj) ;
		
		if (handler == null) return false ;
		
		Method method_getInstance = handler.getClass().getMethod("getInstance") ;
		
		Object realObj = method_getInstance.invoke(handler) ;
		
		return realObj != null ;
	}
	
	static private boolean isFromWeldProxy(Object obj) {
		Class<? extends Object> clazz = obj.getClass() ;
		
		Class<?> weldProxyClass = getWeldProxySuperclass(clazz);
		
		return weldProxyClass != null ;
	}

	static private Class<?> getWeldProxySuperclass(Class<? extends Object> clazz) {
		while (true) {
			if (clazz.getName().equals(WELD_PROXY_OBJECT_CLASS_NAME)) {
				return clazz ;
			}
			
			Class<?>[] interfaces = clazz.getInterfaces() ;
			
			for (Class<?> interf : interfaces) {
				if ( isInterfaceOfType(interf, WELD_PROXY_OBJECT_CLASS_NAME) ) {
					return clazz ;
				}
			}
			
			Class<?> superclass = clazz.getSuperclass() ;
			if (superclass == null) break ;
			
			clazz = superclass ;
		}
		return null ;
	}
	
	static private boolean isInterfaceOfType(Class<?> interf, String typeName) {
		
		while (true) {
			if ( interf.getName().equals(typeName) ) return true ;
			
			Class<?> superclass = interf.getSuperclass() ;
			if (superclass == null) break ;
			
			interf = superclass ;
		}
		
		return false ;
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
		
		return className.startsWith(WELD_PACKAGE) ;
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
