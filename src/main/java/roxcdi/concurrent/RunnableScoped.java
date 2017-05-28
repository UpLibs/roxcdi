package roxcdi.concurrent;

abstract class RunnableScoped implements Runnable {

	protected RunnableScoped() {
	
	}
	
	private volatile boolean finished = false ;
	
	public boolean isFinished() {
		return finished;
	}
	
	void notifyFinished() {
		synchronized (this) {
			finished = true ;
			this.notifyAll();
		}
	}
	
	public void waitFinished() {
		synchronized (this) {
			while (!finished) {
				try {
					this.wait();
				} catch (InterruptedException e) {}
			}	
		}
	}
	
}
