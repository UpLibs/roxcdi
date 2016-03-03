package roxcdi.test.cdi.sys;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SampleSys {

	@Inject
	private SampleCollection collection ;
	
	public int getCollectionSize() {
		return collection.size() ;
	}
	
	public int getCollectionElement(int idx) {
		return collection.get(idx) ;
	}
	
	
}
