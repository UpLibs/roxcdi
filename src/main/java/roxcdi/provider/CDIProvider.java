package roxcdi.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.enterprise.inject.spi.CDI;

abstract public class CDIProvider {

	final protected CDI<?> cdi ;

	public CDIProvider(CDI<?> cdi) {
		this.cdi = cdi;
	}
	
	public CDI<?> getCDI() {
		return cdi;
	}

	////////////////////////////////////////////////////////////////////
	
	abstract public void startContexts() ;
	
	abstract public void startContext( Class<? extends Annotation> contextType ) ;
	
	abstract public void stopContext( Class<? extends Annotation> contextType ) ;
	
	
	private final HashMap<String, Class<?>> classCache = new HashMap<>() ;
	
	@SuppressWarnings({ "unchecked" })
	private Class<?> getClass(String className) {
		try {
			Class<?> clazz = classCache.get(className) ;
			if (clazz != null) return clazz ;
			
			clazz = Class.forName(className) ;
			
			classCache.put(className, clazz) ;
			return clazz ;
		} catch (Exception e) {
			return null ;
		}
	}
	
	protected boolean startContextByClassName(String className) {
		@SuppressWarnings("unchecked")
		Class<? extends Annotation> clazz = (Class<? extends Annotation>) getClass(JAVAX_ENTERPRISE_CONTEXT_APPLICATION_SCOPED) ;
		if (clazz == null) return false ;
		
		startContext(clazz) ;
		return true ;
	}
	
	protected boolean stopContextByClassName(String className) {
		@SuppressWarnings("unchecked")
		Class<? extends Annotation> clazz = (Class<? extends Annotation>) getClass(JAVAX_ENTERPRISE_CONTEXT_APPLICATION_SCOPED) ;
		if (clazz == null) return false ;
		
		stopContext(clazz) ;
		return true ;
	}
	
	private static final String JAVAX_ENTERPRISE_CONTEXT_CONVERSATION_SCOPED = "javax.enterprise.context.ConversationScoped";
	private static final String JAVAX_ENTERPRISE_CONTEXT_SESSION_SCOPED = "javax.enterprise.context.SessionScoped";
	private static final String JAVAX_ENTERPRISE_CONTEXT_REQUEST_SCOPED = "javax.enterprise.context.RequestScoped";
	private static final String JAVAX_ENTERPRISE_CONTEXT_APPLICATION_SCOPED = "javax.enterprise.context.ApplicationScoped";
	
	public void startContextApplicationScoped() {
		startContextByClassName(JAVAX_ENTERPRISE_CONTEXT_APPLICATION_SCOPED) ;
	}
	
	public void stopContextApplicationScoped() {
		stopContextByClassName(JAVAX_ENTERPRISE_CONTEXT_APPLICATION_SCOPED) ;
	}
	
	public void startContextRequestScoped() {
		startContextByClassName(JAVAX_ENTERPRISE_CONTEXT_REQUEST_SCOPED) ;
	}
	
	public void stopContextRequestScoped() {
		stopContextByClassName(JAVAX_ENTERPRISE_CONTEXT_REQUEST_SCOPED) ;
	}
	
	public void startContextSessionScoped() {
		startContextByClassName(JAVAX_ENTERPRISE_CONTEXT_SESSION_SCOPED) ;
	}
	
	public void stopContextSessionScoped() {
		stopContextByClassName(JAVAX_ENTERPRISE_CONTEXT_SESSION_SCOPED) ;
	}
	
	public void startContextConversationScoped() {
		startContextByClassName(JAVAX_ENTERPRISE_CONTEXT_CONVERSATION_SCOPED) ;
	}
	
	public void stopContextConversationScoped() {
		stopContextByClassName(JAVAX_ENTERPRISE_CONTEXT_CONVERSATION_SCOPED) ;
	}
		
	abstract public boolean ensureConstructed(Object obj) ;
	
	abstract public boolean shutdown() ;
	
	////////////////////////////////////////////////////////////////////

	@SuppressWarnings("rawtypes")
	protected static Object callMethod(CDI<?> cdi, String methodName) {
		Class<? extends CDI> clazz = cdi.getClass() ;
		
		Method method = null ;
		try {
			method = clazz.getMethod(methodName) ;
		}
		catch (NoSuchMethodException e) {}
		
		if (method != null) {
			try {
				method.invoke(cdi) ;
				return Boolean.TRUE ;
			}
			catch (Exception e) {
				return e ;
			}
		}
		
		return Boolean.FALSE ;
	}

	
}
