package roxcdi.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.WeakHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import roxcdi.RoxCDI;

final public class WeldProvider extends CDIProvider {
	
	static public final WeldProvider DUMMY = new WeldProvider(null) ;
	
	static public CDI<?> instantiateCDI() {
		try {
			Class<?> weldClass = Class.forName(WELD_CLASS_NAME) ;
			
			Object weld = weldClass.newInstance() ;
			
			Method methodInitialize = weldClass.getMethod("initialize") ;
			
			CDI<?> cdi = (CDI<?>) methodInitialize.invoke(weld) ;
			
			instance(cdi) ;
			
			return cdi ;
		}
		catch (Exception e) {
			e.printStackTrace(); 
			return null ;
		}
	}

	
	static public boolean isWeldContainer(CDI<?> cdi) {
		String className = cdi.getClass().getName() ;
		return className.startsWith(WELD_PACKAGE) ;
	}
	
	static public boolean isProviderProxy(Object obj) {
		Class<? extends Object> clazz = obj.getClass() ;
		
		Class<?> weldProxyClass = getWeldProxySuperclass(clazz);
		
		return weldProxyClass != null ;
	}
	
	static private WeakHashMap<CDI<?>, WeldProvider> instances = new WeakHashMap<>() ;
	static public WeldProvider instance(CDI<?> cdi) {
		if (cdi == null) return DUMMY ;
		
		synchronized (instances) {
			WeldProvider provider = instances.get(cdi) ;
			if (provider == null) instances.put(cdi, provider = new WeldProvider(cdi) ) ;
			return provider ;
		}
	}
	
	/////////////////////////////////////////////////
	
	static private Boolean isWeldPresentAndAllowed ;
	static public boolean isWeldPresentAndAllowed() {
		if (isWeldPresentAndAllowed == null) isWeldPresentAndAllowed = isWeldPresentAndAllowedImplem() ;
		return isWeldPresentAndAllowed ;
	}
	
	static private boolean isWeldPresentAndAllowedImplem() {
		try {
			Class<?> weldClass = Class.forName(WELD_CLASS_NAME) ;
			
			if (weldClass != null) {
				try {
					Boolean allow = RoxCDI.PROPERTY_WELD_AUTOLOAD.getBoolean();
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
	
	/////////////////////////////////////////////////
	
	private static final String WELD_PROXY_OBJECT_CLASS_NAME = "org.jboss.weld.bean.proxy.ProxyObject";
	private static final String WELD_CLASS_NAME = "org.jboss.weld.environment.se.Weld";
	private static final String WELD_PACKAGE = "org.jboss.weld.";
	
	private static final String WELD_REQUEST_CONTEXT_CLASS = "org.jboss.weld.context.RequestContext";
	private static final String WELD_SESSION_CONTEXT_CLASS = "org.jboss.weld.context.SessionContext";
	private static final String WELD_APPLICATION_CONTEXT_CLASS = "org.jboss.weld.context.ApplicationContext";
	private static final String WELD_CONVERSATION_CONTEXT_CLASS = "org.jboss.weld.context.ConversationContext";
	
	private static final String WELD_BOUNDLITERAL_CLASS = "org.jboss.weld.context.bound.BoundLiteral";

	private WeldProvider(CDI<?> cdi) {
		super(cdi);
	}

	private HashMap<Class<?>, Class<?>> weldContextClass = new HashMap<>() ;
	private Class<?> getWeldContextClass(Class<? extends Annotation> contextType) {
		Class<?> contextClass ;
		synchronized (weldContextClass) {
			contextClass = weldContextClass.get(contextType) ;
			
			if (contextClass == null) {
				try {
					if ( contextType == RequestScoped.class ) {
						contextClass = Class.forName(WELD_REQUEST_CONTEXT_CLASS) ;
					}
					else if ( contextType == ApplicationScoped.class ) {
						contextClass = Class.forName(WELD_APPLICATION_CONTEXT_CLASS) ;
					}
					else if ( contextType == SessionScoped.class ) {
						contextClass = Class.forName(WELD_SESSION_CONTEXT_CLASS) ;
					}
					else if ( contextType == ConversationScoped.class ) {
						contextClass = Class.forName(WELD_CONVERSATION_CONTEXT_CLASS) ;
					}
					else {
						throw new UnsupportedOperationException("Can't find context class for type: "+ contextType) ;
					}
				}
				catch (ClassNotFoundException e) {
					throw new IllegalStateException(e) ;
				}
				
				if (contextClass != null) weldContextClass.put(contextType, contextClass) ;
			}
			
			return contextClass ;
		}
		

	}

	private Annotation weldBoundLiteralAnnotation ;
	private Annotation getWeldBoundLiteralAnnotation() {
		if (weldBoundLiteralAnnotation == null) {
			try {
				Class< ? > boundLiteralClass = Class.forName( WELD_BOUNDLITERAL_CLASS );
				Field field = boundLiteralClass.getField( "INSTANCE" );
				Annotation boundLiteral = (Annotation) field.get(null);
				
				weldBoundLiteralAnnotation = boundLiteral ;
			}
			catch ( NoSuchFieldException | ClassNotFoundException | SecurityException | IllegalAccessException e) {
				throw new IllegalStateException(e) ;
			}
		}
		
		return weldBoundLiteralAnnotation ;
	}
	
	private Object getWeldContext(CDI<?> cdi, Class<?> contextClass) {
		Annotation boundLiteral = getWeldBoundLiteralAnnotation();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Instance<Object> context = cdi.select((Class)contextClass, boundLiteral) ;
		
		return context != null ? context.get() : null ;		
	}
	
	private HashMap<Class<?>, Method> weldContextMethodIsAlive = new HashMap<>() ;
	private Method getWeldContextMethodIsAlive(Class<?> contextClass) {
		synchronized (weldContextMethodIsAlive) {
			Method method = weldContextMethodIsAlive.get(contextClass) ;
			
			if (method == null) {
				try {
					method = contextClass.getMethod("isActive") ;
					weldContextMethodIsAlive.put(contextClass, method) ;
				}
				catch (Exception e) {
					throw new IllegalStateException(e) ;
				}
			}
			
			return method ;
		}
	}
	
	private HashMap<Class<?>, Method> weldContextMethodActivate = new HashMap<>() ;
	private Method getWeldContextMethodActivate(Class<?> contextClass) {
		synchronized (weldContextMethodActivate) {
			Method method = weldContextMethodActivate.get(contextClass) ;
			
			if (method == null) {
				try {
					method = contextClass.getMethod("activate") ;
					weldContextMethodActivate.put(contextClass, method) ;
				}
				catch (Exception e) {
					throw new IllegalStateException(e) ;
				}
			}
			
			return method ;
		}
	}
	
	private HashMap<Class<?>, Method> weldContextMethodDeactivate = new HashMap<>() ;
	private Method getWeldContextMethodDeactivate(Class<?> contextClass) {
		synchronized (weldContextMethodDeactivate) {
			Method method = weldContextMethodDeactivate.get(contextClass) ;
			
			if (method == null) {
				try {
					method = contextClass.getMethod("deactivate") ;
					weldContextMethodDeactivate.put(contextClass, method) ;
				}
				catch (Exception e) {
					throw new IllegalStateException(e) ;
				}
			}
			
			return method ;
		}
	}
	
	private HashMap<Class<?>, Method> weldContextMethodInvalidate = new HashMap<>() ;
	private Method getWeldContextMethodInvalidate(Class<?> contextClass) {
		synchronized (weldContextMethodInvalidate) {
			Method method = weldContextMethodInvalidate.get(contextClass) ;
			
			if (method == null) {
				try {
					method = contextClass.getMethod("invalidate") ;
					weldContextMethodInvalidate.put(contextClass, method) ;
				}
				catch (Exception e) {
					throw new IllegalStateException(e) ;
				}
			}
			
			return method ;
		}
	}
	
	////////////////////////////////////////////////////////
	
	@Override
	public void startContexts() {
		startContext(ApplicationScoped.class);
		startContext(RequestScoped.class);
	}

	public void startContext( Class<? extends Annotation> contextType ) {
		try {
			Class<?> contextClass = getWeldContextClass(contextType);
			Object context = getWeldContext(cdi, contextClass) ;
			
			Method methodIsAlive = getWeldContextMethodIsAlive(contextClass) ;
			Boolean alive = (Boolean) methodIsAlive.invoke(context) ;
			
			if (!alive) {
				Method methodActivate = getWeldContextMethodActivate(contextClass);
				methodActivate.invoke(context) ;
			}
		}
		catch ( SecurityException | InvocationTargetException | IllegalAccessException e) {
			throw new IllegalStateException(e) ;
		}
	}
	
	public void stopContext( Class<? extends Annotation> contextType ) {

		try {
			Class<?> contextClass = getWeldContextClass(contextType);
			Object context = getWeldContext(cdi, contextClass) ;
			
			Method methodIsAlive = getWeldContextMethodIsAlive(contextClass) ;
			Boolean alive = (Boolean) methodIsAlive.invoke(context) ;

			if (alive) {
				Method methodInvalidate = getWeldContextMethodInvalidate(contextClass);
				methodInvalidate.invoke(context) ;
				
				Method methodDeactivate = getWeldContextMethodDeactivate(contextClass);
				methodDeactivate.invoke(context) ;
			}
		}
		catch (SecurityException | InvocationTargetException | IllegalAccessException e) {
			throw new IllegalStateException(e) ;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean ensureConstructed(Object obj) {
		if ( isProviderProxy(obj) ) {
			try {
				return ensureConstructed_WeldProxy(obj) ;
			}
			catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				return false ;
			}
		}
		else {
			obj.toString();
			return true ;
		}
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
	
	///////////////////////////////////////////////////////////////////////////////
	
	public boolean shutdown() {
		
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



}
