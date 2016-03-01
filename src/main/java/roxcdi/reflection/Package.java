package roxcdi.reflection;

import java.util.HashMap;

public class Package {

	static public String getName() {
		return getName(0) ;
	}
	
	static public String getName(int ignoreCallerLeves) {
		Throwable throwable = new Throwable() ;
		StackTraceElement[] stackTrace = throwable.getStackTrace() ;
		
		int idx = 1+ignoreCallerLeves ;
		if (idx >= stackTrace.length) idx = stackTrace.length-1 ;
		
		StackTraceElement stackTraceElement = stackTrace[idx] ;
		
		String className = stackTraceElement.getClassName() ;
		
		String packageName = extractClassNamePackage(className);
		return packageName ;
	}

	static private final HashMap<String, String> extractClassNamePackageCache = new HashMap<>() ;
	
	private static String extractClassNamePackage(String className) {
		String packageName ;
		
		synchronized (extractClassNamePackageCache) {
			packageName = extractClassNamePackageCache.get(className) ;
		}
		
		if (packageName != null) return packageName ;
		
		packageName = className.indexOf(".") < 0 ? "" : className.replaceFirst("\\.[^\\.]+$", "") ;
		
		synchronized (extractClassNamePackageCache) {
			extractClassNamePackageCache.put(className, packageName) ;
		}
		
		return packageName;
	}
	
	public static void main(String[] args) {
		
		System.out.println( getName() );
		
	}
	
}
