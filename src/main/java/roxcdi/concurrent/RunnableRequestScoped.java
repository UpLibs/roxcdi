package roxcdi.concurrent;

import roxcdi.RoxCDI;

abstract public class RunnableRequestScoped implements Runnable {

	@Override
	public void run() {
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
