package roxcdi.test.cdi;

import javax.enterprise.inject.Instance;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;

import roxcdi.test.cdi.sys.SampleSysWithOldSchool;

public class TestCDI_NormalAndOldSchool {

	@Test
	public void testCDI() {
		
		Weld weld = new Weld() ;
		
		WeldContainer weldContainer = weld.initialize() ;
		
		Instance<SampleSysWithOldSchool> instance = weldContainer.select(SampleSysWithOldSchool.class) ;
		
		SampleSysWithOldSchool sampleSysWithOldSchool = instance.get() ;
		
		Assert.assertTrue( sampleSysWithOldSchool.getSubCollectionSize() == 20 );
		
		for (int i = 0; i < sampleSysWithOldSchool.getSubCollectionSize() ; i++) {
			int elem = sampleSysWithOldSchool.getSubCollectionElement(i) ;
			
			Assert.assertEquals( 10+i , elem );
		}
		
		
	}
	
}
