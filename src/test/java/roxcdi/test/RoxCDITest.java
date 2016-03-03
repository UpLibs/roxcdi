package roxcdi.test;

import javax.enterprise.inject.spi.CDI;

import org.junit.Assert;
import org.junit.Test;

import roxcdi.RoxCDI;

public class RoxCDITest {

	@Test
	public void testBasic() {
	
		CDI<?> cdi = RoxCDI.getCDI() ;
		
		Assert.assertNotNull( cdi );
		
	}
	
}
