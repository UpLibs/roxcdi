package roxcdi.parameter;

import roxcdi.reflection.Package;

final public class Property {
	
	static public Property fromPackage(String name) {
		return new Property( Package.getName(1) , name ) ;
	}
	
	final private String name ;

	public Property(String name) {
		this.name = name;
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
	
	public String get() {
		return resolveValue(this.defaultValue, this.scanEnv);
	}
	
	public String get(String defaultValue) {
		return resolveValue(defaultValue, this.scanEnv);
	}
	
	public String resolveValue(String defaultValue, boolean scanEnv) {
		String val = System.getProperty(name) ;
		
		if (val == null && scanEnv) {
			val = System.getenv(name) ;
		}
		
		if (val == null) {
			val = defaultValue ;
		}
		
		return val;
	}

	///////////////////////////////////////////

	public boolean isDefined() {
		return resolveValue(null, this.scanEnv) != null ;
	}
	
	public boolean isNotDefined() {
		return !isDefined() ;
	}
	
	public boolean isDefinedAndNotEmpty() {
		String val = resolveValue(null, this.scanEnv) ;
		return val != null && !val.isEmpty() ;
	}
	
	public String getString() {
		return get() ;
	}
	
	public Boolean getBoolean() {
		String val = get() ;
		if (val == null) return false ;
		
		if (val.isEmpty() || val.equals("false") || val.equals("null") || val.equals("undef")) return false ;
		
		return true ;
	}
	
	public Integer getInteger() {
		String val = get() ;
		return val != null ? Integer.parseInt(val) : null ;
	}
	
	public Long getLong() {
		String val = get() ;
		return val != null ? Long.parseLong(val) : null ;
	}
	
	public Float getFloat() {
		String val = get() ;
		return val != null ? Float.parseFloat(val) : null ;
	}
	
	public Double getDouble() {
		String val = get() ;
		return val != null ? Double.parseDouble(val) : null ;
	}
	
	///////////////////////////////////////
	
	@Override
	public String toString() {
		return getName()+"="+get() ;
	}
	
}
