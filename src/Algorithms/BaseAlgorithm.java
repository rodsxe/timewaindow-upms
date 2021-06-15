package Algorithms;

abstract public class BaseAlgorithm implements Algorithm {
	
	private long totalTime;
	private long initialTime;
	
	@Override
	public void setTimeProperties(long totalTime, long initialTime) {
		
		this.totalTime = totalTime;
		this.initialTime = initialTime;

	}

	protected long getTotalTime() {
		return totalTime;
	}

	protected long getInitialTime() {
		return initialTime;
	}

	
}
