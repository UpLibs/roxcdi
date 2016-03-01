package roxcdi.parameter;

import roxcdi.reflection.Package;

final public class Property {
	
	static public Property fromPackage(String name) {
		return new Property( Package.getName(1) , name ) ;
	}
	
	final private String name ;

	public Property(String fullName) {
		this.name = fullName;
	}
	
	public Property(String packageName, String name) {
		this.name = packageName +"."+ name;
	}
	
	private String defaultValue ;
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public Property withDefault(Object defaultValue) {
		this.defaultValue = defaultValue != null ? defaultValue.toString() : null ;
		return this ;
	}
	
	public boolean scanEnv = true ;
	
	public boolean isScanEnvEnabled() {
		return scanEnv;
	}
	
	public Property withScanEnv(boolean enableScanEnv) {
		this.scanEnv = enableScanEnv ;
		return this ;
	}
	
	public String getName() {
		return name;
	}
	
	private PropertyContext defaultContext ;
	
	public PropertyContext getDefaultContext() {
		return defaultContext;
	}
	
	public Property withDefaultContext(PropertyContext defaultContext) {
		this.defaultContext = defaultContext ;
		return this ;
	}
	
	public PropertyContext getContext() {
		return PropertyContext.getContext(defaultContext) ;
	}
	
	public String get() {
		return get(getContext()) ;
	}
	
	public String get(PropertyContext propertyContext) {
		return resolveValue(this.defaultValue, this.scanEnv, propertyContext);
	}
	
	public String get(String defaultValue) {
		return get(defaultValue, getContext()) ;
	}
	
	public String get(String defaultValue, PropertyContext propertyContext) {
		return resolveValue(defaultValue, this.scanEnv, propertyContext);
	}
	
	public String resolveValue(String defaultValue, boolean scanEnv, PropertyContext propertyContext) {
		if (propertyContext == null) propertyContext = PropertyContext.getContext() ;
		
		String val = propertyContext.getProperty(name) ;
		
		if (val == null && scanEnv) {
			val = propertyContext.getEnv(name) ;
		}
		
		if (val == null) {
			val = defaultValue ;
		}
		
		return val;
	}

	///////////////////////////////////////////

	public boolean isDefined() {
		return isDefined(getContext());
	}
	
	public boolean isDefined(PropertyContext propertyContext) {
		return resolveValue(null, this.scanEnv, propertyContext) != null ;
	}
	
	public boolean isNotDefined() {
		return isNotDefined(getContext()) ;
	}
	
	public boolean isNotDefined(PropertyContext propertyContext) {
		return !isDefined() ;
	}
	
	public boolean isDefinedAndNotEmpty() {
		return isDefinedAndNotEmpty(getContext());
	}
	
	public boolean isDefinedAndNotEmpty(PropertyContext propertyContext) {
		String val = resolveValue(null, this.scanEnv, propertyContext) ;
		return val != null && !val.isEmpty() ;
	}
	
	public String getString() {
		return getString(getContext()) ;
	}
	
	public String getString(PropertyContext propertyContext) {
		return get(propertyContext) ;
	}
	
	public Boolean getBoolean() {
		return getBoolean(getContext());
	}
	
	public Boolean getBoolean(PropertyContext propertyContext) {
		String val = get(propertyContext) ;
		if (val == null) return false ;
		
		if (val.isEmpty() || val.equals("false") || val.equals("null") || val.equals("undef")) return false ;
		
		return true ;
	}
	
	public Integer getInteger() {
		return getInteger(getContext()) ;
	}
	
	public Integer getInteger(PropertyContext propertyContext) {
		String val = get(propertyContext) ;
		return val != null ? Integer.parseInt(val) : null ;
	}
	
	public Long getLong() {
		return getLong(getContext()) ;
	}
	
	public Long getLong(PropertyContext propertyContext) {
		String val = get(propertyContext) ;
		return val != null ? Long.parseLong(val) : null ;
	}
	
	public Float getFloat() {
		return getFloat(getContext()) ;
	}
	
	public Float getFloat(PropertyContext propertyContext) {
		String val = get(propertyContext) ;
		return val != null ? Float.parseFloat(val) : null ;
	}
	
	public Double getDouble() {
		return getDouble(getContext()) ;
	}
	
	public Double getDouble(PropertyContext propertyContext) {
		String val = get(propertyContext) ;
		return val != null ? Double.parseDouble(val) : null ;
	}
	
	///////////////////////////////////////
	
	@Override
	public String toString() {
		return getName()+"="+get() ;
	}
	
}
