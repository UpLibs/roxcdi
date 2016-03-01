package roxcdi.parameter;

public class Parameter {

	final private String name ;
	final private Property propery ;
	
	public Parameter(String name, Property propery) {
		this.name = name;
		this.propery = propery;
	}

	public String getName() {
		return name;
	}

	public Property getPropery() {
		return propery;
	}
	
	@Override
	public String toString() {
		return name+"="+propery ;
	}
	
}
