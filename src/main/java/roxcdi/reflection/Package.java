package roxcdi.reflection;

import java.util.HashMap;

public class Package {

	static public String getName() {
		return getName(1) ;
	}
	
	static public String getName(int ignoreCallersLeves) {
		Throwable throwable = new Throwable() ;
		StackTraceElement[] stackTrace = throwable.getStackTrace() ;
		
		int idx = 1+ignoreCallersLeves ;
		if (idx >= stackTrace.length) idx = stackTrace.length-1 ;
		
		StackTraceElement stackTraceElement = stackTrace[idx] ;
		
		String className = stackTraceElement.getClassName() ;
		
		String packageName = extractClassPackageName(className);
		return packageName ;
	}

	static private final HashMap<String, String> extractClassPackageName_Cache = new HashMap<>() ;
	
	public static String extractClassPackageName(String className) {
		String packageName ;
		
		synchronized (extractClassPackageName_Cache) {
			packageName = extractClassPackageName_Cache.get(className) ;
		}
		
		if (packageName != null) return packageName ;
		
		packageName = className.indexOf(".") < 0 ? "" : className.replaceFirst("\\.[^\\.]+$", "") ;
		
		synchronized (extractClassPackageName_Cache) {
			extractClassPackageName_Cache.put(className, packageName) ;
		}
		
		return packageName;
	}
	
}
