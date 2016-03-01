package roxcdi.test;

import org.junit.Assert;
import org.junit.Test;

import roxcdi.parameter.Property;
import roxcdi.parameter.PropertyContext;

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

	@Test
	public void testContext() {
		
		System.setProperty("roxcdi.test.foo", "123") ;
		
		Property property = Property.fromPackage("foo") ;
		
		Assert.assertEquals( property.getName() ,"roxcdi.test.foo" );
		
		Assert.assertEquals( property.get() ,"123" );
		
		PropertyContext propertyContext = new PropertyContext() ;
		propertyContext.set( property.getName() , "1111");
		
		PropertyContext.setContext(propertyContext) ;
		Assert.assertEquals( property.get() ,"1111" );
		PropertyContext.unsetContext(propertyContext) ;
		
		Assert.assertEquals( property.get() ,"123" );
		
		
		PropertyContext propertyContext2 = new PropertyContext() ;
		propertyContext2.set( property.getName() , "2222");
		
		PropertyContext.setContext(propertyContext) ;
		Assert.assertEquals( property.get() ,"1111" );
			PropertyContext.setContext(propertyContext2) ;
			Assert.assertEquals( property.get() ,"2222" );
			PropertyContext.unsetContext(propertyContext2) ;
		Assert.assertEquals( property.get() ,"1111" );
		PropertyContext.unsetContext(propertyContext) ;
		Assert.assertEquals( property.get() ,"123" );
		
		PropertyContext propertyContext3 = new PropertyContext() ;
		propertyContext3.set( property.getName() , "3333");
		
		property.withDefaultContext(propertyContext3);
		Assert.assertEquals( property.get() ,"3333" );
		PropertyContext.setContext(propertyContext) ;
		Assert.assertEquals( property.get() ,"1111" );
		PropertyContext.unsetContext(propertyContext) ;
		Assert.assertEquals( property.get() ,"3333" );		
	}
	

	@Test
	public void testContext2() {
		
		System.setProperty("roxcdi.test.foo", "123") ;
		System.setProperty("roxcdi.test.bar", "456") ;
		
		Property property = Property.fromPackage("foo") ;
		Property property2 = Property.fromPackage("bar") ;
		
		Assert.assertEquals( property.getName() ,"roxcdi.test.foo" );
		
		Assert.assertEquals( property.get() ,"123" );
		
		PropertyContext propertyContext = new PropertyContext(property , "1111") ;
		
		PropertyContext.setContext(propertyContext) ;
		Assert.assertEquals( property.get() ,"1111" );
		PropertyContext.unsetContext(propertyContext) ;
		
		PropertyContext propertyContext2 = new PropertyContext(property , property2) ;
		
		PropertyContext.setContext(propertyContext2) ;
		Assert.assertEquals( property.get() ,"456" );
		PropertyContext.unsetContext(propertyContext2) ;

	}
	
}
