package roxcdi.test.cdi;

import javax.enterprise.inject.Instance;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;

import roxcdi.RoxCDI;
import roxcdi.parameter.PropertyContext;
import roxcdi.test.cdi.sys.SampleSys;

public class TestCDI_Normal {

	@Test
	public void testCDI() {
		
		String propValue = "hello" ;
		
		System.setProperty("roxcdi.test.property", propValue) ;
		
		Weld weld = new Weld() ;
		
		WeldContainer weldContainer = weld.initialize() ;
		
		String propValueOverwrite = "HELLO!!!" ;
		
		PropertyContext propertyContext = new PropertyContext("roxcdi.test.property", propValueOverwrite);
		PropertyContext.setContext(propertyContext) ;
		
		Instance<SampleSys> instance = weldContainer.select(SampleSys.class) ;
		
		SampleSys sampleSys = instance.get() ;
		
		RoxCDI.ensureConstructed(sampleSys) ;
		
		PropertyContext.unsetContext(propertyContext);
		
		Assert.assertTrue( sampleSys.getCollectionSize() == 20 );
		
		for (int i = 0; i < sampleSys.getCollectionSize() ; i++) {
			int elem = sampleSys.getCollectionElement(i) ;
			
			Assert.assertEquals( 10+i , elem );
		}
		
		sampleSys.checkProperty(propValueOverwrite);
		
		weldContainer.shutdown();
		
	}
	
}
