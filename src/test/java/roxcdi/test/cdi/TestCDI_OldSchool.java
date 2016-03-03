package roxcdi.test.cdi;

import org.junit.Assert;
import org.junit.Test;

import roxcdi.RoxCDI;
import roxcdi.test.cdi.sys.OldSchoolSys;

public class TestCDI_OldSchool {

	@Test
	public void testCDI() {
		
		OldSchoolSys oldSchoolSys = new OldSchoolSys() ;
		
		Assert.assertTrue( oldSchoolSys.getCollectionSize() == 20 );
		
		for (int i = 0; i < oldSchoolSys.getCollectionSize() ; i++) {
			int elem = oldSchoolSys.getCollectionElement(i) ;
			
			Assert.assertEquals( 10+i , elem );
		}
		
		Assert.assertTrue( RoxCDI.shutdownCDI() );
		
	}
	
}
