package roxcdi.test;

import org.junit.Assert;
import org.junit.Test;

import roxcdi.reflection.Package;

public class PackageTest {
	
	@Test
	public void testBasic() {
		
		Assert.assertEquals( "roxcdi.test" , Package.getName() );
		
	}

}
