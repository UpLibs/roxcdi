package roxcdi.parameter;

public class PropertyValueException extends IllegalArgumentException {
	private static final long serialVersionUID = 4734036992777816448L;
	
	private String invalidValue ;
	private Class<?> type ;
	
	public PropertyValueException(String propertyName, String invalidValue, Class<?> type) {
		super("["+propertyName+"] Invalid value for type "+ typeName(type) +": "+ invalidValue) ;
		this.invalidValue = invalidValue ;
		this.type = type ;
	}
	
	public PropertyValueException(String propertyName, String invalidValue, Class<?> type, Throwable cause) {
		super("["+propertyName+"] Invalid value for type "+ typeName(type) +": "+ invalidValue, cause) ;
		this.invalidValue = invalidValue ;
		this.type = type ;
	}
	
	static String typeName(Class<?> type) {
		return type.getName().replaceFirst("^java\\.lang\\.", "");
	}

	public String getInvalidValue() {
		return invalidValue;
	}
	
	public Class<?> getType() {
		return type;
	}
	
}
