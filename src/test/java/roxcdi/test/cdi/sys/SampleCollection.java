package roxcdi.test.cdi.sys;

public class SampleCollection {

	private int init = 10 ;
	private int end = 30 ;
	
	public SampleCollection() {
	
	}
	
	public int size() {
		return end - init ;
	}
	
	public int get(int idx) {
		if (idx >= size()) throw new ArrayIndexOutOfBoundsException(idx);
		return init+idx ;
	}
	
}
