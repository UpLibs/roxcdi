package roxcdi.concurrent;

import roxcdi.RoxCDI;

abstract public class RunnableRequestScoped extends RunnableScoped {

	@Override
	final public void run() {
		RoxCDI.startContextRequestScoped();
		
		try {
			runRequestScoped();
		}
		finally {
			RoxCDI.stopContextRequestScoped();
			notifyFinished();
		}
	}
	
	abstract public void runRequestScoped() ;

}
