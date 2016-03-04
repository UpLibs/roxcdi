package roxcdi.test.cdi.sys;

import javax.inject.Inject;

import org.junit.Assert;

import roxcdi.parameter.cdi.PropertyInjection;

public class SampleSys {

	@Inject
	private SampleCollection collection ;
	
	@Inject
	@PropertyInjection( name = "roxcdi.test.property" , defaultValue = "nothing" , type = String.class )
	private String testProperty ;
	
	public int getCollectionSize() {
		return collection.size() ;
	}
	
	public int getCollectionElement(int idx) {
		return collection.get(idx) ;
	}
	
	public void checkProperty(String expectedValue) {
		Assert.assertEquals(expectedValue, testProperty) ;
	}
	
}
