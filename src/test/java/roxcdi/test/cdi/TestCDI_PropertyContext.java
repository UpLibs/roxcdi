package roxcdi.test.cdi;

import javax.enterprise.inject.Instance;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;

import roxcdi.RoxCDI;
import roxcdi.parameter.PropertyContext;
import roxcdi.test.cdi.sys.SampleSys;

public class TestCDI_PropertyContext {

	@Test
	public void testCDI() {
		
		String propValue = "hello" ;
		
		System.setProperty("roxcdi.test.property", propValue) ;
		
		Weld weld = new Weld() ;
		
		WeldContainer weldContainer = weld.initialize() ;
		
		String propValueOverwrite = "HELLO!!!" ;
		
		PropertyContext propertyContext = new PropertyContext("roxcdi.test.property", propValueOverwrite);
		PropertyContext.setContext(propertyContext) ;
		
		Instance<SampleSys> instance1 = weldContainer.select(SampleSys.class) ;
		SampleSys sampleSys1 = instance1.get() ;
		RoxCDI.ensureConstructed(sampleSys1) ;
		
		PropertyContext.unsetContext(propertyContext);
		
		Instance<SampleSys> instance2 = weldContainer.select(SampleSys.class) ;
		SampleSys sampleSys2 = instance2.get() ;
		RoxCDI.ensureConstructed(sampleSys2) ;
		
		// Ensure that is not an ApplicationContext bean:
		Assert.assertTrue( sampleSys1 != sampleSys2 );
		
		sampleSys1.checkProperty(propValueOverwrite);
		sampleSys2.checkProperty(propValue);
		
		weldContainer.shutdown();
		
	}
	
}
