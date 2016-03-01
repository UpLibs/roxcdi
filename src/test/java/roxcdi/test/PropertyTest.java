package roxcdi.test;

import org.junit.Assert;
import org.junit.Test;

import roxcdi.parameter.Property;

public class PropertyTest {

	@Test
	public void testBasic() {
		
		System.setProperty("roxcdi.test.foo", "123") ;
		
		Property property = Property.fromPackage("foo") ;
		
		Assert.assertEquals( property.getName() ,"roxcdi.test.foo" );
		
		Assert.assertEquals( property.get() ,"123" );
		
		Assert.assertTrue( property.getInteger() == 123 );
		Assert.assertTrue( property.getLong() == 123L );
		Assert.assertTrue( property.getDouble() == 123D );
		Assert.assertTrue( property.getBoolean() );
		
	}
	
	@Test
	public void testDefaultValue() {
		
		Property property = Property.fromPackage("fooNotSet").withDefault("456") ;
		
		Assert.assertFalse( property.isDefined() );
		Assert.assertTrue( property.isNotDefined() );
		
		Assert.assertEquals( property.get() ,"456" );
		
		Assert.assertTrue( property.getInteger() == 456 );
		Assert.assertTrue( property.getLong() == 456L );
		Assert.assertTrue( property.getDouble() == 456D );
		Assert.assertTrue( property.getBoolean() );
		
	}
	
}
