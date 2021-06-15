package model;

public class Job {
	
	private int id;
	private int dueDate;
	private boolean isRestrict;
	private boolean haAtraso;
	private int releaseDate;
	private int tardinessWeight; 
	private int earlinessWeight;
	private int dueDateEarly;
	private int tardiness;
	private int earliness;
	private int completionTime;
	
	
	public Job(int id, int dueDate, boolean isRestrict, int releaseDate, int tardinessWeight, int earlinessWeight, int dueDateEarly) {
		
		super();
		this.id = id;
		this.dueDate = dueDate;
		this.isRestrict = isRestrict;
		this.haAtraso = false;
		this.releaseDate = releaseDate;
		this.tardinessWeight = tardinessWeight;
		this.earlinessWeight = earlinessWeight;
		this.dueDateEarly = dueDateEarly;
		
	}

	
	public int getCompletionTime() {
		return completionTime;
	}


	public void setCompletionTime(int completionTime) {
		this.completionTime = completionTime;
	}


	public int getTardiness() {
		return tardiness;
	}


	public void setTardiness(int tardiness) {
		this.tardiness = tardiness;
	}


	public int getEarliness() {
		return earliness;
	}


	public void setEarliness(int earliness) {
		this.earliness = earliness;
	}


	public int getDueDateEarly() {
		return dueDateEarly;
	}


	public int getTardinessWeight() {
		return tardinessWeight;
	}


	public int getEarlinessWeight() {
		return earlinessWeight;
	}


	public int getReleaseDate() {
		return releaseDate;
	}


	public boolean isHaAtraso() {
		return haAtraso;
	}


	public void setHaAtraso(boolean haAtraso) {
		this.haAtraso = haAtraso;
	}


	public int getId() {
		return id;
	}

	public int getDueDate() {
		return dueDate;
	}

	public boolean isRestrict() {
		return isRestrict;
	}

}
