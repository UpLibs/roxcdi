package roxcdi.test.cdi.sys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.enterprise.inject.Instance;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class TestWeldClassLoaderBeansXML {

	static final private Charset CHARSET_LATIN1 = Charset.forName("iso-8859-1") ;
	
	static private URL urlOfFakeBeansXML ;
	synchronized static private URL getURLOfFakeBeansXML() throws IOException {
		if (urlOfFakeBeansXML == null) {
			urlOfFakeBeansXML = createURLOfFakeBeansXML() ;
		}
		return urlOfFakeBeansXML ;
	}
	
	static private URL createURLOfFakeBeansXML() throws IOException {
		String content = getContentOfFakeBeansXML() ;
		
		System.out.println(content);
		
		File tempDirectory = File.createTempFile("weld-beans-", "dir") ;
		tempDirectory.delete() ;
		tempDirectory.mkdir() ;
		
		File fileMetaInf = new File(tempDirectory,"META-INF") ;
		fileMetaInf.mkdirs() ;
		
		File fileBeans = new File(fileMetaInf,"beans.xml") ;
		
		FileOutputStream fout = new FileOutputStream(fileBeans) ;
		
		try {
			fout.write( content.getBytes(CHARSET_LATIN1) );
		}
		finally {
			try {
				fout.close();
			} catch (Exception e) {}
		}
		
		return fileBeans.toURI().toURL() ;
	}

	
	private static String getContentOfFakeBeansXML() {
		StringBuilder str = new StringBuilder() ;
		
		str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n") ;
		str.append("<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" ") ;
		str.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ") ;
		str.append("xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd\" ") ;
		str.append("bean-discovery-mode=\"all\">\n") ;
		str.append("</beans>") ;
		
		return str.toString() ;
	}


	static public class MyClassLoader extends ClassLoader {

		public MyClassLoader() {
			super();
		}

		public MyClassLoader(ClassLoader parent) {
			super(parent);
		}
		
		@Override
		protected URL findResource(String name) {
			URL url = super.findResource(name);
			
			if (url == null && name.endsWith("META-INF/beans.xml")) {
				try {
					url = getURLOfFakeBeansXML() ;
					System.out.println(url);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return url ;
		}
		
		@Override
		protected Enumeration<URL> findResources(String name) throws IOException {
			Enumeration<URL> resources = super.findResources(name);
			
			ArrayList<URL> list = new ArrayList<>() ;
			
			while ( resources.hasMoreElements() ) {
				list.add( resources.nextElement() ) ;
			}
			
			if (list.isEmpty() && name.endsWith("META-INF/beans.xml")) {
				list.add( getURLOfFakeBeansXML() ) ;
			}
			
			System.out.println("find res>> "+ name +">> "+ list);
			
			return Collections.enumeration(list) ;
		}
		
	}

	public static void main(String[] args) {

		MyClassLoader myClassLoader = new MyClassLoader( TestWeldClassLoaderBeansXML.class.getClassLoader() ) ;
		
		Weld weld = new Weld() ;
		
		weld.setClassLoader(myClassLoader) ;
		
		WeldContainer weldContainer = weld.initialize() ;
		
		Instance<SampleSys> instance = weldContainer.select(SampleSys.class) ;
		
		SampleSys sampleSys = instance.get() ;
		
		System.out.println(sampleSys);
		
		System.out.println("-----------------------------------------");
				
	}
	
}
