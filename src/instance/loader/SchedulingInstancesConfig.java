package instance.loader;

import java.util.List;

import model.Job;
import model.MachineContainer;

public class SchedulingInstancesConfig {
	
	public static final int DUE_DATE = 0;
	public static final int IS_RESTRICT = 1;
	public static final int RELEASE_DATE = 2;
	public static final int TARDINESS_WEIGHT = 3;
	public static final int EARLINESS_WEIGHT = 4;
	public static final int DUE_DATE_EARLY = 5;
	
	public int[][][] setupTime;
	public int[][] processTime;
	public int[][] dueDates;
	
	public SchedulingInstancesConfig(int[][][] setupTime, int[][] processTime, int[][] dueDates) {
		super();
		this.setupTime = setupTime;
		this.processTime = processTime;
		this.dueDates = dueDates;
	}
	
	public Job getJob(int job) {
		
		return new Job(job, getDueDate(job), isRestrict(job), getReleaseDate(job), getTardinessWeight(job), getEarliestWeight(job), getDueDateEarly(job));
		
	}
	
	public int getProcessTime(int machine, int job){
		return processTime[machine][job];
	}
	
	public int getSetupTime(int machine, int previousJob, int job){
		if(previousJob == -1)previousJob = job;
		return setupTime[machine][previousJob][job];
	}
	
	public boolean isRestrict(int job){
		return (dueDates[job][IS_RESTRICT] == 0)? false : true;
	}
	
	public int getReleaseDate(int job){
		return dueDates[job][RELEASE_DATE];
	}
	
	public int getTardinessWeight(int job){
		return dueDates[job][TARDINESS_WEIGHT];
	}
	
	public int getEarliestWeight(int job){
		return dueDates[job][EARLINESS_WEIGHT];
	}
	
	public int getDueDate(int job){
		return dueDates[job][DUE_DATE];
	}
	
	public int getDueDateEarly(int job){
		//return (dueDates[0].length > DUE_DATE_EARLY)? dueDates[job][DUE_DATE_EARLY] : dueDates[job][DUE_DATE];
		return (dueDates[0].length > DUE_DATE_EARLY)? dueDates[job][DUE_DATE_EARLY] : dueDates[job][DUE_DATE];
		
	}

	public int[][][] getSetupTime() {
		return setupTime;
	}

	public void setSetupTime(int[][][] setupTime) {
		this.setupTime = setupTime;
	}

	public int[][] getProcessTime() {
		return processTime;
	}

	public void setProcessTime(int[][] processTime) {
		this.processTime = processTime;
	}
	
	public int getNumberOfMachines() {
		return processTime.length;
	}
	
	public int getNumberOfJobs() {
		return processTime[0].length;
	}
	
	public Long calcularCompletationTime(MachineContainer machine){
		
		List<Job> tarefaAlocadas = machine.getJobs();
		long completationTime = 0;
		Job previousJob = (tarefaAlocadas.isEmpty())? null: tarefaAlocadas.get(0);
		
		for (int i = 0; i < tarefaAlocadas.size(); i++){
			
			Job job = tarefaAlocadas.get(i);
			
			completationTime += Math.max(job.getReleaseDate(), completationTime + getSetupTime(machine.getId(), previousJob.getId(), job.getId()));
			
			completationTime += getProcessTime(machine.getId(), job.getId());
						
			previousJob = job;
			
		}
		/*if(this.completationTime != completationTime){
			System.out.println("eeeeeeeeeerrrrrrrrrrrrrrrooooooooooooorrrrrrrrrrrrr");
		}*/
		return completationTime;
	}
	
}
