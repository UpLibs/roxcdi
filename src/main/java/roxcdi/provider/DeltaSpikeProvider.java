package roxcdi.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

import javax.enterprise.inject.spi.CDI;

import roxcdi.RoxCDI;

final public class DeltaSpikeProvider extends CDIProvider {
	
	static public final DeltaSpikeProvider DUMMY = new DeltaSpikeProvider(null) ;

	static public boolean isDeltaSpikeContainer(CDI<?> cdi) {
		synchronized (instantiatedCDIs) {
			return instantiatedCDIs.containsKey(cdi) ;
		}
	}
	
	static final private WeakHashMap<CDI<?>, DeltaSpikeProvider> instantiatedCDIs = new WeakHashMap<>() ;
	
	static public CDI<?> instantiateCDI() {
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
			
			CDI<Object> cdi = CDI.current() ;
			if (cdi == null) return null ;
			
			DeltaSpikeProvider provider = instance(cdi) ;
			
			synchronized (instantiatedCDIs) {
				instantiatedCDIs.put(cdi, provider) ;	
			}
			
			provider.startContexts();
			
			return cdi ;
		}
		catch (Exception e) {
			e.printStackTrace(); 
			return null ;
		}
	}
	

	static private WeakHashMap<CDI<?>, DeltaSpikeProvider> instances = new WeakHashMap<>() ;
	static public DeltaSpikeProvider instance(CDI<?> cdi) {
		if (cdi == null) return DUMMY ;
		
		synchronized (instances) {
			DeltaSpikeProvider provider = instances.get(cdi) ;
			if (provider == null) instances.put(cdi, provider = new DeltaSpikeProvider(cdi) ) ;
			return provider ;
		}
	}
	
	/////////////////////////////////////////////////////////////

	static private Boolean isDeltaSpikePresentAndAllowed ;
	static public boolean isDeltaSpikePresentAndAllowed() {
		if (isDeltaSpikePresentAndAllowed == null) isDeltaSpikePresentAndAllowed = isDeltaSpikePresentAndAllowedImplem() ; 
		return isDeltaSpikePresentAndAllowed ;
	}

	static private boolean isDeltaSpikePresentAndAllowedImplem() {
		try {
			Class<?> dsClass = Class.forName(DELTASPIKE_CDICONTAINERLOADER_CLASS_NAME) ;
			
			if (dsClass != null) {
				try {
					Boolean allow = RoxCDI.PROPERTY_DELTASPIKE_AUTOLOAD.getBoolean();
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

	/////////////////////////////////////////////////////////////
	
	private static final String DELTASPIKE_CDICONTAINERLOADER_CLASS_NAME = "org.apache.deltaspike.cdise.api.CdiContainerLoader";
	private static final String DELTASPIKE_CONTEXTCONTROL_CLASS_NAME = "org.apache.deltaspike.cdise.api.ContextControl";
	private static final String DELTASPIKE_BEANPROVIDER_CLASS_NAME = "org.apache.deltaspike.core.api.provider.BeanProvider";
	
	public DeltaSpikeProvider(CDI<?> cdi) {
		super(cdi);
	}

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
				methodDeltaSpikeContextControlStartContext = contextControl.getClass().getMethod("startContext", Class.class) ;
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
				methodDeltaSpikeContextControlStopContext = contextControl.getClass().getMethod("stopContext", Class.class) ;
			}
			catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
				throw new IllegalStateException(e) ;
			}
		}
		return methodDeltaSpikeContextControlStopContext ;
	}
	
	public void startContexts() {
		
		try {
			Object contextControl = getDeltaSpikeContextControl() ;
			Method methodStartContexts = getMethodDeltaSpikeContextControlStartContexts();
			
			methodStartContexts.invoke(contextControl) ;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e) ;
		}
		
	}
	
	public void startContext( Class<? extends Annotation> contextType ) {
		
		try {
			Object contextControl = getDeltaSpikeContextControl() ;
			Method methodStartContext = getMethodDeltaSpikeContextControlStartContext();
			methodStartContext.invoke(contextControl, contextType) ;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e) ;
		}
		
	}
		
	public void stopContext( Class<? extends Annotation> contextType ) {
		
		try {
			Object contextControl = getDeltaSpikeContextControl() ;
			Method methodStartContext = getMethodDeltaSpikeContextControlStopContext();
			methodStartContext.invoke(contextControl, contextType) ;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e) ;
		}
		
	}
	
	private WeldProvider weldProvider ;
	private WeldProvider getWeldProvider() {
		if (weldProvider == null) weldProvider = WeldProvider.instance(cdi) ;
		return weldProvider ;
	}
	
	private GenericProvider genericProvider ;
	private GenericProvider getGenericProvider() {
		if (genericProvider == null) genericProvider = GenericProvider.instance(cdi) ;
		return genericProvider ;
	}
	
	@Override
	public boolean ensureConstructed(Object obj) {
		if ( WeldProvider.isProviderProxy(obj) ) {
			return getWeldProvider().ensureConstructed(obj) ;
		}
		else {
			return getGenericProvider().ensureConstructed(obj) ;
		}
	}
	
	public boolean shutdown() {
		if ( WeldProvider.isWeldContainer(cdi) ) {
			return getWeldProvider().shutdown() ;
		}
		else {
			return getGenericProvider().shutdown() ;
		}
	}
	
	

}
