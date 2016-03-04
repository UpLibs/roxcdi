package roxcdi.test.cdi;

import org.junit.Assert;
import org.junit.Test;

import roxcdi.RoxCDI;
import roxcdi.parameter.PropertyContext;
import roxcdi.test.cdi.sys.SampleSys;

public class TestCDI_PropertyContext2 {

	@Test
	public void testCDI() {
		
		String propValue = "hello" ;
		
		System.setProperty("roxcdi.test.property", propValue) ;
		
		String propValueOverwrite = "HELLO!!!" ;
		
		PropertyContext propertyContext = new PropertyContext("roxcdi.test.property", propValueOverwrite);
		
		SampleSys sampleSys1 = RoxCDI.getBean(SampleSys.class, propertyContext) ;
		
		SampleSys sampleSys2 = RoxCDI.getBean(SampleSys.class) ;
		
		// Ensure that is not an ApplicationContext bean:
		Assert.assertTrue( sampleSys1 != sampleSys2 );
		
		sampleSys1.checkProperty(propValueOverwrite);
		sampleSys2.checkProperty(propValue);
		
		RoxCDI.shutdownCDI() ;
		
	}
	
}
