package roxcdi ;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import roxcdi.parameter.Property;
import roxcdi.parameter.PropertyContext;
import roxcdi.reflection.AnnotationProxy;

public class RoxCDI {
	
	private static final String WELD_PROXY_OBJECT_CLASS_NAME = "org.jboss.weld.bean.proxy.ProxyObject";
	private static final String WELD_CLASS_NAME = "org.jboss.weld.environment.se.Weld";
	private static final String WELD_PACKAGE = "org.jboss.weld.";
	
	private static final String WELD_REQUEST_CONTEXT_CLASS = "org.jboss.weld.context.RequestContext";
	private static final String WELD_SESSION_CONTEXT_CLASS = "org.jboss.weld.context.SessionContext";
	private static final String WELD_APPLICATION_CONTEXT_CLASS = "org.jboss.weld.context.ApplicationContext";
	private static final String WELD_CONVERSATION_CONTEXT_CLASS = "org.jboss.weld.context.ConversationContext";
	
	private static final String WELD_BOUNDLITERAL_CLASS = "org.jboss.weld.context.bound.BoundLiteral";

	
	private static final String DELTASPIKE_CDICONTAINERLOADER_CLASS_NAME = "org.apache.deltaspike.cdise.api.CdiContainerLoader";
	private static final String DELTASPIKE_CONTEXTCONTROL_CLASS_NAME = "org.apache.deltaspike.cdise.api.ContextControl";
	private static final String DELTASPIKE_BEANPROVIDER_CLASS_NAME = "org.apache.deltaspike.core.api.provider.BeanProvider";
	
	final static public Property PROPERTY_CDI_INSTANTIATOR = Property.fromPackage("instantiator") ;
	final static public Property PROPERTY_WELD_AUTOLOAD = Property.fromPackage("weld.autoload",Boolean.class).withDefault(true) ;
	final static public Property PROPERTY_DELTASPIKE_AUTOLOAD = Property.fromPackage("deltaspike.autoload",Boolean.class).withDefault(true) ;
	
	final static private RoxCDI roxCDI = new RoxCDI() ;
	
	static public <T extends CDI<?>> T getCDI() {
		return roxCDI.get() ;
	}
	
	static public <T extends CDI<?>> T getCDI_IfInitialized() {
		return roxCDI.getIfInitialized();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	static public <U> U getBean(Class<U> subtype, PropertyContext propertyContext) {
		PropertyContext.setContext(propertyContext) ;
		
		try {
			U bean = getBean(subtype) ;
			ensureConstructed(bean) ;
			return bean ;	
		}
		finally {
			PropertyContext.unsetContext(propertyContext) ;
		}
	}
	
	static public <U> U getBean(Class<U> subtype, PropertyContext propertyContext, Class<? extends Annotation> qualifier) {
		return getBean(subtype, propertyContext, AnnotationProxy.asAnnotation(qualifier)) ;
	}
	
	@SafeVarargs
	static public <U> U getBean(Class<U> subtype, PropertyContext propertyContext, Class<? extends Annotation>... qualifiers) {
		return getBean(subtype, propertyContext, AnnotationProxy.asAnnotations(qualifiers)) ;
	}
	
	static public <U> U getBean(Class<U> subtype, PropertyContext propertyContext, Annotation... qualifiers) {
		PropertyContext.setContext(propertyContext) ;
		
		try {
			U bean = getBean(subtype, qualifiers) ;
			ensureConstructed(bean) ;
			return bean ;	
		}
		finally {
			PropertyContext.unsetContext(propertyContext) ;
		}
	}
	
	static public <U> U getBean(Annotation subtype, PropertyContext propertyContext, Annotation qualifiers) {
		PropertyContext.setContext(propertyContext) ;
		
		try {
			U bean = getBean(subtype, qualifiers) ;
			ensureConstructed(bean) ;
			return bean ;	
		}
		finally {
			PropertyContext.unsetContext(propertyContext) ;
		}
	}
	
	static public <U> U getBean(PropertyContext propertyContext, Annotation qualifiers) {
		PropertyContext.setContext(propertyContext) ;
		
		try {
			U bean = getBean(qualifiers) ;
			ensureConstructed(bean) ;
			return bean ;	
		}
		finally {
			PropertyContext.unsetContext(propertyContext) ;
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <U> U getBean(Class<U> subtype) {
		Instance<U> instance = getCDI().select((Class)subtype) ;
		return instance != null ? instance.get() : null ;
	}

	static public <U> U getBean(Class<U> subtype, Class<? extends Annotation> qualifier) {
		return getBean(subtype, AnnotationProxy.asAnnotation(qualifier)) ;
	}

	@SuppressWarnings({ "unchecked" })
	static public <U> U getBean(Class<U> subtype, Class<? extends Annotation>... qualifiers) {
		return getBean(subtype, AnnotationProxy.asAnnotations(qualifiers)) ;
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
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	
	static public enum InstantiatedCDI {
		ALREADY_PRESENT,
		INSTANTIATOR_CLASS,
		DELTASPIKE,
		WELD,
	}
	
	private InstantiatedCDI instantiatedCDI = InstantiatedCDI.ALREADY_PRESENT ;
	
	public InstantiatedCDI getInstantiatedCDI() {
		return instantiatedCDI;
	}
	
	private CDI<?> instantiateCDI() {
		
		String instantiatorClassName = PROPERTY_CDI_INSTANTIATOR.get() ;
		
		if (instantiatorClassName != null && !instantiatorClassName.isEmpty()) {
			
			try {
				CDI<?> cdi = instantiateCDIWithClass(instantiatorClassName);
				
				instantiatedCDI = InstantiatedCDI.INSTANTIATOR_CLASS ;
				
				return cdi ;
			}
			catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException("Can't automatically instantiate CDI. Can't find class set at property: "+ PROPERTY_CDI_INSTANTIATOR.getName() +"="+ instantiatorClassName, e);
			}
			catch (InstantiationException | IllegalAccessException e) {
				throw new UnsupportedOperationException("Can't automatically instantiate CDI. Error calling instantiator set by property: "+ PROPERTY_CDI_INSTANTIATOR.getName() +"="+ instantiatorClassName, e);
			}
			
		}
		else if ( isDeltaSpikePresentAndAllowed() ) {
			CDI<?> cdi = instantiateDeltaSpike() ;
			
			instantiatedCDI = InstantiatedCDI.DELTASPIKE;
			
			return cdi ;
		}
		else if ( isWeldPresentAndAllowed() ) {
			CDI<?> cdi = instantiateWeld() ;
			
			instantiatedCDI = InstantiatedCDI.WELD;
			
			return cdi ;
		}
		
		throw new UnsupportedOperationException("Can't automatically instantiate CDI. Shoud instantiate CDI before call any RoxCDI method! You also can autoload Weld (if present in classpath) using property <"+ PROPERTY_WELD_AUTOLOAD + "> or use RoxCDIInstantiator implementation at property <"+ PROPERTY_CDI_INSTANTIATOR +">");
	}
	
	/////////////////////////////////////////////////
	
	private WeakReference<CDI<?>> deltaspikeInstantiatedCDIRef = null ;
	
	private CDI<?> instantiateDeltaSpike() {
		try {
			Class<?> dsClass = Class.forName(DELTASPIKE_CDICONTAINERLOADER_CLASS_NAME) ;
			
			try {
				Method methodGetCdiContainer = dsClass.getMethod("getCdiContainer") ;
				
				Object cdiContainer = methodGetCdiContainer.invoke(null) ;
				
				Method methodBoot = cdiContainer.getClass().getMethod("boot") ;
				
				methodBoot.invoke(cdiContainer) ;	
			}
			catch (IllegalStateException e) {
				if ( !e.getMessage().startsWith("WELD-ENV") ) {
					throw new IllegalStateException("Can't boot DeltaSpike.", e) ;
				}
			}
			
			startDeltaSpikeContexts();
			
			CDI<Object> cdi = CDI.current() ;
			if (cdi == null) return null ;
			
			deltaspikeInstantiatedCDIRef = new WeakReference<CDI<?>>(cdi) ;
			
			return cdi ;
		}
		catch (Exception e) {
			e.printStackTrace(); 
			return null ;
		}
	}

	private Boolean isDeltaSpikePresentAndAllowed ;
	private boolean isDeltaSpikePresentAndAllowed() {
		if (isDeltaSpikePresentAndAllowed == null) isDeltaSpikePresentAndAllowed = isDeltaSpikePresentAndAllowedImplem() ; 
		return isDeltaSpikePresentAndAllowed ;
	}

	private boolean isDeltaSpikePresentAndAllowedImplem() {
		try {
			Class<?> dsClass = Class.forName(DELTASPIKE_CDICONTAINERLOADER_CLASS_NAME) ;
			
			if (dsClass != null) {
				try {
					Boolean allow = PROPERTY_DELTASPIKE_AUTOLOAD.getBoolean();
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

	private Boolean isWeldPresentAndAllowed ;
	private boolean isWeldPresentAndAllowed() {
		if (isWeldPresentAndAllowed == null) isWeldPresentAndAllowed = isWeldPresentAndAllowedImplem() ;
		return isWeldPresentAndAllowed ;
	}
	
	private boolean isWeldPresentAndAllowedImplem() {
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

	/////////////////////////////////////////////////////
	
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
	
		if ( roxCDI.isDeltaSpikeContainer(cdi)) {
			return roxCDI.shutdownDeltaSpike(cdi) ;
		}
		else if ( roxCDI.isWeldContainer(cdi) ) {
			return roxCDI.shutdownWeld(cdi) ;
		}
		else {
			return roxCDI.shutdownGeneric(cdi) ;
		}
		
	}
	
	private boolean shutdownDeltaSpike(CDI<?> cdi) {
		if ( roxCDI.isWeldContainer(cdi) ) {
			return shutdownWeld(cdi) ;
		}
		else {
			return shutdownGeneric(cdi) ;
		}
	}
	
	private boolean shutdownWeld(CDI<?> cdi) {
		
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

	private boolean isDeltaSpikeContainer(CDI<?> cdi) {
		WeakReference<CDI<?>> ref = deltaspikeInstantiatedCDIRef ;
		CDI<?> deltaSpikeCDI = ref != null ? ref.get() : null ;
		return cdi == deltaSpikeCDI ;
	}
	
	private boolean isWeldContainer(CDI<?> cdi) {
		String className = cdi.getClass().getName() ;
		return className.startsWith(WELD_PACKAGE) ;
	}
	
	private boolean shutdownGeneric(CDI<?> cdi) {

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
	
	//////////////////////////////////////////////////////////////////
	
	static public void startContext( Class<? extends Annotation> contextType ) { 
		
		CDI<?> cdi = getCDI() ;
		
		if ( roxCDI.isDeltaSpikeContainer(cdi) ) {
			roxCDI.startContextDeltaSpike(cdi, contextType) ;
		}
		else if ( roxCDI.isWeldContainer(cdi) ) {
			roxCDI.startContextWeld(cdi, contextType);
		}
		else if ( roxCDI.isDeltaSpikePresentAndAllowed() ) {
			roxCDI.startContextDeltaSpike(cdi, contextType) ;
		}
		
	}
	
	static public void stopContext( Class<? extends Annotation> contextType ) {
		CDI<?> cdi = getCDI() ;
		
		if ( roxCDI.isDeltaSpikeContainer(cdi) ) {
			roxCDI.stopContextDeltaSpike(cdi, contextType) ;
		}
		else if ( roxCDI.isWeldContainer(cdi) ) {
			roxCDI.stopContextWeld(cdi, contextType);
		}
		else if ( roxCDI.isDeltaSpikePresentAndAllowed() ) {
			roxCDI.stopContextDeltaSpike(cdi, contextType) ;
		}
		
	}
	
	//////////////////////////////////////////////////////////////////

	private Method deltaSpikeMethodGetContextualReference ;
	private Method getDeltaSpikeMethodGetContextualReference() {
		if (deltaSpikeMethodGetContextualReference == null) {
			try {
				Class<?> classbeanProvider = Class.forName(DELTASPIKE_BEANPROVIDER_CLASS_NAME) ;
				deltaSpikeMethodGetContextualReference = classbeanProvider.getMethod("getContextualReference", Class.class, Annotation[].class) ;
			}
			catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalArgumentException e) {
				throw new IllegalStateException(e) ;
			}	
		}
		return deltaSpikeMethodGetContextualReference ;
	}
	
	private Class<?> deltaSpikeClassContextControl ;
	private Class<?> getDeltaSpikeClassContextControl() {
		if (deltaSpikeClassContextControl == null) {
			try {
				deltaSpikeClassContextControl = Class.forName(DELTASPIKE_CONTEXTCONTROL_CLASS_NAME) ;
			}
			catch (ClassNotFoundException e) {
				throw new IllegalStateException(e) ;
			}
		}
		return deltaSpikeClassContextControl ;
	}
	
	static final private Annotation[] dummyAnnotationArray = new Annotation[0] ;

	private Object getDeltaSpikeContextControl() {
		
		try {
			Method methodGetContextualReference = getDeltaSpikeMethodGetContextualReference() ;
			Class<?> classContextControl = getDeltaSpikeClassContextControl();
			
			Object contextControl = methodGetContextualReference.invoke(null, classContextControl, dummyAnnotationArray) ;
			
			return contextControl ;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e) ;
		}
	}
	
	private Method methodDeltaSpikeContextControlStartContexts ;
	private Method methodDeltaSpikeContextControlStartContext ;
	private Method methodDeltaSpikeContextControlStopContext ;
	
	private Method getMethodDeltaSpikeContextControlStartContexts() {
		if (methodDeltaSpikeContextControlStartContexts == null) {
			try {
				Object contextControl = getDeltaSpikeContextControl() ;
				methodDeltaSpikeContextControlStartContexts = contextControl.getClass().getMethod("startContexts") ;
			}
			catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
				throw new IllegalStateException(e) ;
			} 
		}
		return methodDeltaSpikeContextControlStartContexts ;
	}
	
	private Method getMethodDeltaSpikeContextControlStartContext() {
		if (methodDeltaSpikeContextControlStartContext == null) {
			try {
				Object contextControl = getDeltaSpikeContextControl() ;
				methodDeltaSpikeContextControlStartContext = contextControl.getClass().getMethod("startContext") ;
			}
			catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
				throw new IllegalStateException(e) ;
			}
		}
		return methodDeltaSpikeContextControlStartContext ;
	}
	
	private Method getMethodDeltaSpikeContextControlStopContext() {
		if (methodDeltaSpikeContextControlStopContext == null) {
			try {
				Object contextControl = getDeltaSpikeContextControl() ;
				methodDeltaSpikeContextControlStopContext = contextControl.getClass().getMethod("stopContext") ;
			}
			catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
				throw new IllegalStateException(e) ;
			}
		}
		return methodDeltaSpikeContextControlStopContext ;
	}
	
	private void startDeltaSpikeContexts() {
		
		try {
			Object contextControl = getDeltaSpikeContextControl() ;
			Method methodStartContexts = getMethodDeltaSpikeContextControlStartContexts();
			
			methodStartContexts.invoke(contextControl) ;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e) ;
		}
		
	}
	
	private void startContextDeltaSpike( CDI<?> cdi, Class<? extends Annotation> contextType ) {
		
		try {
			Object contextControl = getDeltaSpikeContextControl() ;
			Method methodStartContext = getMethodDeltaSpikeContextControlStartContext();
			methodStartContext.invoke(contextControl, contextType) ;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e) ;
		}
		
	}
		
	private void stopContextDeltaSpike( CDI<?> cdi, Class<? extends Annotation> contextType ) {
		
		try {
			Object contextControl = getDeltaSpikeContextControl() ;
			Method methodStartContext = getMethodDeltaSpikeContextControlStopContext();
			methodStartContext.invoke(contextControl, contextType) ;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e) ;
		}
		
	}
	
	//////////////////////////////////////////////////////////////////	
	
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

	private void startContextWeld( CDI<?> cdi, Class<? extends Annotation> contextType ) {
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
	
	private void stopContextWeld( CDI<?> cdi, Class<? extends Annotation> contextType ) {

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
	

	
}
