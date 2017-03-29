package roxcdi.concurrent;

import roxcdi.RoxCDI;

abstract public class RunnableSessionScoped implements Runnable {

	@Override
	final public void run() {
		defineSession();
		
		RoxCDI.startContextSessionScoped();
		
		try {
			runSessionScoped();
		}
		finally {
			RoxCDI.stopContextSessionScoped();
		}
	}
	

	public void defineSession() {}
	
	abstract public void runSessionScoped() ;

}
