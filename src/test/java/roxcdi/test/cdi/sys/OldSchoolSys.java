package roxcdi.test.cdi.sys;

import roxcdi.RoxCDI;

public class OldSchoolSys {

	private SampleSys sampleSys ;
	
	public OldSchoolSys() {
		
		this.sampleSys = RoxCDI.select(SampleSys.class).get() ;
		
	}
	
	public int getCollectionSize() {
		return sampleSys.getCollectionSize();
	}

	public int getCollectionElement(int idx) {
		return sampleSys.getCollectionElement(idx);
	}

	public static void main(String[] args) {
		
		OldSchoolSys simpleSys = new OldSchoolSys() ;
		
		for (int i = 0; i < simpleSys.getCollectionSize() ; i++) {
			System.out.println( simpleSys.getCollectionElement(i) );
		}
		
	}
	
}
