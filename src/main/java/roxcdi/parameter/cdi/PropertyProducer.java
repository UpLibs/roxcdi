package roxcdi.parameter.cdi;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;

import roxcdi.parameter.Property;

@Dependent
public class PropertyProducer {

    private PropertyInjection getPropertyInject( InjectionPoint ip ) {
        Annotated annotated = ip.getAnnotated();
        return annotated.getAnnotation( PropertyInjection.class );
    }
    
    @Produces
    @PropertyInjection
    protected String injectString( final InjectionPoint ip ) {
    	Property property = resolveProperty(ip);
    	
    	return property.getString() ;
    }

    @Produces
    @PropertyInjection
    protected Integer injectInteger( final InjectionPoint ip ) {
    	Property property = resolveProperty(ip);
    	
    	return property.getInteger() ;
    }
    
    @Produces
    @PropertyInjection
    protected Long injectLong( final InjectionPoint ip ) {
    	Property property = resolveProperty(ip);
    	
    	return property.getLong() ;
    }
    
    @Produces
    @PropertyInjection
    protected Boolean injectBoolean( final InjectionPoint ip ) {
    	Property property = resolveProperty(ip);
    	
    	return property.getBoolean() ;
    }
    
    @Produces
    @PropertyInjection
    protected Float injectFloat( final InjectionPoint ip ) {
    	Property property = resolveProperty(ip);
    	
    	return property.getFloat() ;
    }
    
	private Property resolveProperty(final InjectionPoint ip) {
		PropertyInjection propertyInject = getPropertyInject( ip );
        
    	String propName = resolveString( propertyInject.name() ) ;
    	String defaultValue = resolveString( propertyInject.defaultValue() ) ;
    	Class<?> type = propertyInject.type() ;
    	if (type == PropertyInjection.class) type = null ;
    	
    	Property property = new Property(propName, type).withDefault(defaultValue) ;
		return property;
	}
	
    static private String resolveString(String str) {
    	return str == PropertyInjection.NULL ? null : str ;
    }
    
}
