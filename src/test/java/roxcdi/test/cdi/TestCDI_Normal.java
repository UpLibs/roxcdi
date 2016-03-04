package roxcdi.test.cdi;

import javax.enterprise.inject.Instance;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;

import roxcdi.test.cdi.sys.SampleSys;

public class TestCDI_Normal {

	@Test
	public void testCDI() {
		
		Weld weld = new Weld() ;
		
		WeldContainer weldContainer = weld.initialize() ;
		
		Instance<SampleSys> instance = weldContainer.select(SampleSys.class) ;
		
		SampleSys sampleSys = instance.get() ;
		
		Assert.assertTrue( sampleSys.getCollectionSize() == 20 );
		
		for (int i = 0; i < sampleSys.getCollectionSize() ; i++) {
			int elem = sampleSys.getCollectionElement(i) ;
			
			Assert.assertEquals( 10+i , elem );
		}
		
		weldContainer.shutdown();
		
	}
	
}
