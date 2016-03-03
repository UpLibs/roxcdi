package roxcdi.parameter;

import roxcdi.reflection.Package;

final public class Property {
	
	static public Property fromPackage(String name) {
		return new Property( Package.getName(1) , name ) ;
	}
	
	static public Property fromPackage(String name, Class<?> type) {
		return new Property( Package.getName(1) , name , type ) ;
	}
	
	final private String name ;
	final private Class<?> type ;
	
	public Property(String fullName) {
		this(fullName, (Class<?>)null) ;
	}
	
	public Property(String fullName, Class<?> type) {
		this.name = fullName;
		this.type = type ;
	}
	
	public Property(String packageName, String name) {
		this(packageName, name, null) ;
	}
	
	public Property(String packageName, String name, Class<?> type) {
		this.name = packageName +"."+ name;
		this.type = type ;
	}
	
	public Class<?> getType() {
		return type;
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
	
	private String envName ;
	public String getEnvName() {
		if (envName == null) {
			envName = toEnvName(name) ;
		}
		return envName ; 
	}
	
	static public String toEnvName(String propertyName) {
		return propertyName.replaceAll("\\.", "_").toUpperCase() ;
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

	///////////////////////////////////////////
	
	private boolean freezed = false ;
	private String freezeValue = null ;
	
	public boolean isFreezed() {
		return freezed;
	}
	
	public Property freeze() {
		return freeze(getContext()) ;
	}
	
	public Property freeze(PropertyContext propertyContext) {
		freezed = true ;
		freezeValue = resolveValue(this.defaultValue, this.scanEnv, propertyContext, true);
		return this ;
	}
	
	public Property unfreeze() {
		freezed = false ;
		freezeValue = null ;
		return this ;
	}
	
	public boolean isValueTypeOk() {
		return isValueTypeOk(get()) ;
	}
	
	public boolean isValueTypeOk(String value) {
		try {
			return checkValueType(value) ;
		}
		catch (Exception e) {
			return false ;
		}
	}
	
	public boolean checkValueType() {
		return checkValueType( get() ) ;
	}
	
	public boolean checkValueType(String value) {
		if (type == null) return true ;
		
		try {

			if (type == String.class) {
				String.valueOf(value);
			}
			else if (type == Integer.class) {
				Integer.parseInt(value) ;
			}
			else if (type == Long.class) {
				Long.parseLong(value) ;
			}
			else if (type == Double.class) {
				Double.parseDouble(value) ;
			}
			else if (type == Boolean.class) {
				Boolean.parseBoolean(value) ;
			}
				
		}
		catch (Exception e) {
			throw new PropertyValueException(name, value, type, e) ;
		}
		
		return true ;
	}
	
	public Object getTyped() {
		return getTyped(getContext()) ;
	}
	
	public Object getTyped(PropertyContext propertyContext) {
		String value = get(propertyContext) ;
		
		if (type == null) return value ;
		
		try {

			if (type == String.class) {
				return String.valueOf(value);
			}
			else if (type == Integer.class) {
				return Integer.parseInt(value) ;
			}
			else if (type == Long.class) {
				return Long.parseLong(value) ;
			}
			else if (type == Double.class) {
				return Double.parseDouble(value) ;
			}
			else if (type == Boolean.class) {
				return Boolean.parseBoolean(value) ;
			}
		
			return value ;
		}
		catch (Exception e) {
			throw new PropertyValueException(name, value, type, e) ;
		}
	}

	
	public String resolveValue(String defaultValue, boolean scanEnv, PropertyContext propertyContext) {
		return resolveValue(defaultValue, scanEnv, propertyContext, false) ;
	}
	
	protected String resolveValue(String defaultValue, boolean scanEnv, PropertyContext propertyContext, boolean ignoreFreezeValue) {
		if (freezed && !ignoreFreezeValue) return freezeValue ;
		
		if (propertyContext == null) propertyContext = PropertyContext.getContext() ;
		
		String val = propertyContext.getProperty(name) ;
		
		if (val == null && scanEnv) {
			val = propertyContext.getEnv(getEnvName()) ;
		}
		
		if (val == null) {
			val = defaultValue ;
		}
		
		checkValueType(val) ;
		
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
		StringBuilder str = new StringBuilder() ;
		
		if (type != null) {
			str.append("[") ;
			str.append( type.getName().replaceFirst("^java\\.lang\\.", "") ) ;
			str.append("]") ;
		}
		
		str.append( getName() ) ;
		str.append("=") ;
		str.append( getTyped() ) ;
		
		return str.toString() ;
	}
	
}
