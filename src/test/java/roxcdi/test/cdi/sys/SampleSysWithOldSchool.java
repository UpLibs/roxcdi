package roxcdi.test.cdi.sys;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class SampleSysWithOldSchool {

	@Inject
	private SampleCollection collection ;
	
	public SampleSysWithOldSchool() {
	}
	
	private OldSchoolSys oldSchoolSys ;
	
	@PostConstruct
	public void init() {
		oldSchoolSys = new OldSchoolSys() ;
	}
	
	public SampleCollection getCollection() {
		return collection;
	}
	
	public int getSubCollectionSize() {
		return oldSchoolSys.getCollectionSize() ;
	}
	
	public int getSubCollectionElement(int idx) {
		return oldSchoolSys.getCollectionElement(idx) ;
	}
	
}
