package roxcdi.tool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import roxcdi.parameter.Property;

public class PropertyScanner {

	static public Set<Class<?>> getAllClasses() {
		Reflections reflections = new Reflections("", new SubTypesScanner(false));
		
		Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);
		
		return allClasses ;
	}
	
	static public void getClassPropertyFields(Class<?> clazz, List<Field> propertyFields, boolean includeRoxCDIClasses) {
		
		Field[] fields = clazz.getDeclaredFields() ;
		
		for (Field field : fields) {
			if ( Property.class.isAssignableFrom( field.getType() ) ) {
				
				if ( !includeRoxCDIClasses && field.getDeclaringClass().getName().startsWith("roxcdi") ) continue ;
				
				propertyFields.add(field) ;
			}
		}
		
	}
	
	static public List<Field> scanPropertyFields(boolean includeRoxCDIClasses) {
		
		Set<Class<?>> allClasses = getAllClasses() ;
		
		ArrayList<Field> propertyFields = new ArrayList<>() ;
		
		for (Class<?> clazz : allClasses) {
			getClassPropertyFields(clazz, propertyFields, includeRoxCDIClasses);
		}
		
		return propertyFields ;
	}
	
	static public List<Field> scanPropertyFieldsStatic(boolean includeRoxCDIClasses) {
		
		ArrayList<Field> propertyFields = new ArrayList<>() ;
		
		List<Field> fields = scanPropertyFields(includeRoxCDIClasses) ;
		
		for (Field field : fields) {
			if ( (field.getModifiers() & Modifier.STATIC) != 0 ) {
				propertyFields.add(field) ;
			}
		}
		
		return propertyFields ;
	}

	//////////////////////////////////////////////////////////////////////////////////
	
	static public void printAllPropertyFields(boolean onlyStatic, boolean showField, boolean showValue, boolean includeRoxCDIClasses) {

		System.out.println("===============================================================================");
		System.out.println("SCANNING Property Fields...");
		List<Field> properties = onlyStatic ? scanPropertyFieldsStatic(includeRoxCDIClasses) : scanPropertyFields(includeRoxCDIClasses) ;
		System.out.println("===============================================================================");
		
		System.out.println("Found fields: "+ properties.size());
		
		System.out.println("===============================================================================");
		
		if (onlyStatic) {
			for (Field field : properties) {
				Property prop = null ;
				
				try {
					field.setAccessible(true);
					prop = (Property) field.get(null) ;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
				if ( showField ) {
					System.out.println( field +" >>> "+ ( showValue ? prop : prop.getName() ) );	
				}
				else if (prop != null) {
					System.out.println( ( showValue ? prop : prop.getName() ) );
				}
			}	
		}
		else {
			for (Field field : properties) {
				System.out.println( field );
			}
		}
		
		
		
		System.out.println("===============================================================================");

	}
	
	//////////////////////////////////////////////////////////////////////////////////
	
	static private boolean contains(String[] a, String v, boolean defaultVal) {
		for (int i = 0; i < a.length; i++) {
			String elem = a[i].toLowerCase() ;
			if ( elem.equals(v) ) return true ;
		}
		return defaultVal ;
	}
	
	public static void main(String[] args) {
		
		boolean help = args.length == 0 || contains(args, "-h", false) || contains(args, "--h", false) ;
		
		if ( help ) {
			System.out.println("USAGE:");
			System.out.println();
			System.out.println("  $> java "+ PropertyScanner.class.getName() +" %option");
			System.out.println();
			System.out.println("OPTIONS:");
			System.out.println();
			System.out.println(  " -h           This help.");
			System.out.println(  " -s       Show only static Property Feilds (default).");
			System.out.println(  " -a       Show all Property Feilds (even not static).");
			System.out.println(  " -v       Show Property values (static only).");
			System.out.println(  " -f       Show Property Field.");
			System.out.println(  " -rox       Show Property Field.");
			System.out.println();
			System.exit(0);
		}
		
		boolean onlyStaticFields = contains(args, "-s", true) ;
		boolean allFields = contains(args, "-a", false) ;
		boolean showPropertyValue = contains(args, "-v", false) ;
		boolean showPropertyField = contains(args, "-f", false) ;
		boolean includeRoxCDIClasses = contains(args, "-rox", false) ;
		
		System.out.println("===============================================================================");
		System.out.println( PropertyScanner.class.getName() +":");
		System.out.println();
		System.out.println("  onlyStaticFields: "+ onlyStaticFields);
		System.out.println("  allFields: "+ allFields);
		System.out.println("  showPropertyValue: "+ showPropertyValue);
		System.out.println("  showPropertyField: "+ showPropertyField);
		System.out.println("  includeRoxCDIClasses: "+ includeRoxCDIClasses);
		System.out.println();
		
		if (allFields) onlyStaticFields = false ;
		
		printAllPropertyFields(onlyStaticFields, showPropertyField, showPropertyValue, includeRoxCDIClasses);
		
	}
}
