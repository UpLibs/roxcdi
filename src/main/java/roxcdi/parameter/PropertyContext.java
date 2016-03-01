package roxcdi.parameter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class PropertyContext {

	static final private ThreadLocal<PropertyContext> context = new ThreadLocal<>() ;
	
	static final private ThreadLocal<ArrayDeque<PropertyContext>> contextHistory = new ThreadLocal<>() ;
	
	static public PropertyContext setContext(PropertyContext propertyContext) {
		PropertyContext prev = context.get() ;
		
		if (prev != null) {
			ArrayDeque<PropertyContext> history = contextHistory.get() ;
			if (history == null) {
				history = new ArrayDeque<>() ;
				contextHistory.set(history);
			}
			history.addLast(prev) ;
		}
		
		context.set(propertyContext);
		return prev ;
	}
	
	static public PropertyContext unsetContext(PropertyContext propertyContext) {
		PropertyContext current = context.get() ;
		
		if (current == propertyContext) {
			ArrayDeque<PropertyContext> history = contextHistory.get() ;
			PropertyContext prev = history != null && !history.isEmpty() ? history.pollLast() : null ;
			context.set(prev) ;
		}
		
		return current ;
	}
	
	static public PropertyContext clearContext() {
		PropertyContext prev = context.get() ;
		context.set(null);
		return prev ;
	}
	
	static public PropertyContext getContext() {
		return getContext(SYSTEM);
	}
	
	static public PropertyContext getContext(PropertyContext defaultContext) {
		PropertyContext currentContext = context.get() ;
		return currentContext != null ? currentContext : defaultContext ;
	}
	
	////////////////////////////////////////////////////////////
	
	static final SystemPropertyContext SYSTEM = new SystemPropertyContext() ; 
	
	final static public class SystemPropertyContext extends PropertyContext {
		@Override
		public String getProperty(String name) {
			Properties properties = System.getProperties() ;
			synchronized (properties) {
				Object val = properties.get(name) ;
				return val != null ? val.toString() : null ;
			}
		}
		
		@Override
		public String getEnv(String name) {
			return System.getenv(name) ;
		}
		
		@Override
		public void clear() {
			Properties properties = System.getProperties() ;
			synchronized (properties) {
				properties.clear();	
			}
		}
		
		@Override
		public boolean contains(String key) {
			Properties properties = System.getProperties() ;
			synchronized (properties) {
				return properties.containsKey(key) ;
			}
		}
		
		@Override
		public String get(String key) {
			return getProperty(key) ;
		}
		
		@Override
		public List<String> getKeys() {
			Properties properties = System.getProperties() ;
			
			synchronized (properties) {
				ArrayList<String> keys = new ArrayList<>(properties.size()) ;
				
				for (Object k : properties.keySet()) {
					keys.add(k.toString()) ;
				}
				
				Collections.sort(keys);
				return keys ;	
			}
		}
		
		@Override
		public int getSetKeysSize() {
			Properties properties = System.getProperties() ;
			
			synchronized (properties) {
				return properties.size() ;
			}
		}
		
		@Override
		public String remove(String key) {
			Properties properties = System.getProperties() ;
			
			synchronized (properties) {
				Object prev = properties.remove(key) ;
				return prev != null ? prev.toString() : null ;	
			}
		}
		
		@Override
		public void set(String key, String value) {
			Properties properties = System.getProperties() ;
			
			synchronized (properties) {
				properties.put(key, value) ;
			}
		}
		
		
	}
	
	////////////////////////////////////////////////////////////
	
	private PropertyContext parent ;
	
	final private HashMap<String, String> properties = new HashMap<>() ;
	
	public PropertyContext(Object... props) {
		this(null, props) ;
	}
	
	public PropertyContext(PropertyContext parent, Object... props) {
		this.parent = parent ;
		
		for (int i = 0; i < props.length; i+=2) {
			Object k = props[i] ;
			Object v = props[i+1] ;
			
			if (k == null) continue ;
			
			String kStr ;
			
			if (k instanceof Property) {
				kStr = ((Property) k).getName() ;
			}
			else {
				kStr = k.toString() ;
			}
			
			String vStr ;
			
			if (v instanceof Property) {
				vStr = ((Property) v).get() ;
			}
			else {
				vStr = v.toString() ;
			}
			
			properties.put(kStr, vStr);
		}
	}
	
	public PropertyContext getParent() {
		return parent;
	}
	
	public void setParent(PropertyContext parent) {
		this.parent = parent;
	}
	
	public void clear() {
		synchronized (properties) {
			properties.clear();
		}
	}
	
	public void set(String key, String value) {
		synchronized (properties) {
			properties.put(key, value) ;	
		}
	}
	
	public String remove(String key) {
		synchronized (properties) {
			return properties.remove(key) ;
		}
	}
	
	public String get(String key) {
		synchronized (properties) {
			return properties.get(key) ;
		}
	}
	
	public boolean contains(String key) {
		synchronized (properties) {
			return properties.containsKey(key) ;
		}
	}
	
	public List<String> getKeys() {
		synchronized (properties) {
			ArrayList<String> keys = new ArrayList<>(properties.size()) ;
			
			for (String k : properties.keySet()) {
				keys.add(k) ;
			}
			
			Collections.sort(keys);
			return keys ;
		}
	}
	
	public int getSetKeysSize() {
		synchronized (properties) {
			return properties.size() ;
		}
	}

	public String getProperty(String name) {
		String val = get(name) ;
		if (val != null) return val ;
		
		if (parent != null) return parent.getProperty(name) ;
		
		return SYSTEM.getProperty(name) ;
	}
	
	public String getEnv(String name) {
		String val = get(name) ;
		if (val != null) return val ;
		
		if (parent != null) return parent.getEnv(name) ;
		
		return SYSTEM.getEnv(name) ;
	}
	
}
