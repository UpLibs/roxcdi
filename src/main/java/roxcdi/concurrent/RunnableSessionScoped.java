package roxcdi.concurrent;

import roxcdi.RoxCDI;

abstract public class RunnableSessionScoped extends RunnableScoped {

	@Override
	final public void run() {
		defineSession();
		
		RoxCDI.startContextSessionScoped();
		
		try {
			runSessionScoped();
		}
		finally {
			RoxCDI.stopContextSessionScoped();
			notifyFinished();
		}
	}
	

	public void defineSession() {}
	
	abstract public void runSessionScoped() ;

}
