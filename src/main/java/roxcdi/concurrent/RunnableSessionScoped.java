package roxcdi.concurrent;

import roxcdi.RoxCDI;

abstract public class RunnableSessionScoped implements Runnable {

	@Override
	public void run() {
		RoxCDI.startContextRequestScoped() ;
		
		try {
			runRequestScoped();
		}
		finally {
			RoxCDI.stopContextRequestScoped() ;
		}
	}
	
	abstract public void runRequestScoped() ;

}
