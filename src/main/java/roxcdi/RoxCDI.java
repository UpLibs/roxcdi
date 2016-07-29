package roxcdi ;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import roxcdi.parameter.Property;
import roxcdi.parameter.PropertyContext;
import roxcdi.provider.DeltaSpikeProvider;
import roxcdi.provider.GenericProvider;
import roxcdi.provider.WeldProvider;
import roxcdi.reflection.AnnotationProxy;

final public class RoxCDI {

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
	
	static public enum InstantiatedCDIMethod {
		ALREADY_PRESENT,
		INSTANTIATOR_CLASS,
		DELTASPIKE,
		WELD,
	}
	
	private InstantiatedCDIMethod instantiatedCDI = InstantiatedCDIMethod.ALREADY_PRESENT ;
	
	public InstantiatedCDIMethod getInstantiatedCDIMethod() {
		return instantiatedCDI;
	}
	
	private CDI<?> instantiateCDI() {
		
		String instantiatorClassName = PROPERTY_CDI_INSTANTIATOR.get() ;
		
		if (instantiatorClassName != null && !instantiatorClassName.isEmpty()) {
			
			try {
				CDI<?> cdi = instantiateCDIWithClass(instantiatorClassName);
				
				instantiatedCDI = InstantiatedCDIMethod.INSTANTIATOR_CLASS ;
				
				return cdi ;
			}
			catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException("Can't automatically instantiate CDI. Can't find class set at property: "+ PROPERTY_CDI_INSTANTIATOR.getName() +"="+ instantiatorClassName, e);
			}
			catch (InstantiationException | IllegalAccessException e) {
				throw new UnsupportedOperationException("Can't automatically instantiate CDI. Error calling instantiator set by property: "+ PROPERTY_CDI_INSTANTIATOR.getName() +"="+ instantiatorClassName, e);
			}
			
		}
		else if ( DeltaSpikeProvider.isDeltaSpikePresentAndAllowed() ) {
			CDI<?> cdi = DeltaSpikeProvider.instantiateCDI() ;
			instantiatedCDI = InstantiatedCDIMethod.DELTASPIKE;
			return cdi ;
		}
		else if ( WeldProvider.isWeldPresentAndAllowed() ) {
			CDI<?> cdi = WeldProvider.instantiateCDI();
			instantiatedCDI = InstantiatedCDIMethod.WELD;
			return cdi ;
		}
		
		throw new UnsupportedOperationException("Can't automatically instantiate CDI. Shoud instantiate CDI before call any RoxCDI method! You also can autoload Weld (if present in classpath) using property <"+ PROPERTY_WELD_AUTOLOAD + "> or use RoxCDIInstantiator implementation at property <"+ PROPERTY_CDI_INSTANTIATOR +">");
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
		
		if ( WeldProvider.isProviderProxy(obj) ) {
			try {
				return WeldProvider.DUMMY.ensureConstructed(obj) ;
			}
			catch (Exception e) {
				e.printStackTrace();
				return false ;
			}
		}
		else {
			return GenericProvider.DUMMY.ensureConstructed(obj) ;
		}
		
	}
		
	/////////////////////////////////////////////////////
	
	static public boolean shutdownCDI() {
		CDI<?> cdi = getCDI_IfInitialized() ;
		return shutdown(cdi) ;
	}
	
	static public boolean shutdown(CDI<?> cdi) {
		if (cdi == null) return false ;
	
		if ( DeltaSpikeProvider.isDeltaSpikeContainer(cdi) ) {
			return DeltaSpikeProvider.instance(cdi).shutdown() ;
		}
		else if ( WeldProvider.isWeldContainer(cdi) ) {
			return WeldProvider.instance(cdi).shutdown() ;
		}
		else {
			return GenericProvider.instance(cdi).shutdown() ;
		}
	}

	//////////////////////////////////////////////////////////////////
	
	static public void startContext( Class<? extends Annotation> contextType ) { 
		
		CDI<?> cdi = getCDI() ;
		
		if ( DeltaSpikeProvider.isDeltaSpikeContainer(cdi) ) {
			DeltaSpikeProvider.instance(cdi).startContext(contextType);
		}
		else if ( WeldProvider.isWeldContainer(cdi) ) { 
			WeldProvider.instance(cdi).startContext(contextType);
		}
		else if ( DeltaSpikeProvider.isDeltaSpikePresentAndAllowed() ) {
			DeltaSpikeProvider.instance(cdi).startContext(contextType);
		}
		
	}
	
	static public void stopContext( Class<? extends Annotation> contextType ) {

		CDI<?> cdi = getCDI() ;
		
		if ( DeltaSpikeProvider.isDeltaSpikeContainer(cdi) ) {
			DeltaSpikeProvider.instance(cdi).stopContext(contextType);
		}
		else if ( WeldProvider.isWeldContainer(cdi) ) { 
			WeldProvider.instance(cdi).stopContext(contextType);
		}
		else if ( DeltaSpikeProvider.isDeltaSpikePresentAndAllowed() ) {
			DeltaSpikeProvider.instance(cdi).stopContext(contextType);
		}
		
	}
		
}
