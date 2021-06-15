package model;
import java.util.ArrayList;
import java.util.List;

import model.base.Container;

public class MachineContainer extends Container{
	
	private int id;
	public static int[][][] custoDeSetup;
	public static int[][] custos;
	private List<Job> jobs;	
	
	public MachineContainer(int id) {
		super();
		this.id = id;
		this.jobs = new ArrayList<Job>();
	}
	
	public MachineContainer(int id, Job[] jobs) {
		
		super();
		this.id = id;
		this.jobs = new ArrayList<Job>();
		for (Job job : jobs) this.jobs.add(job);
		
	}
	
	public int getId() {
		return id;
	}
	
	public int getProcessTime(Job tarefa){
		return custos[id][tarefa.getId()];
	}
	//TODO DESFAZER
	public int getSetupTime(Job anterior, Job posterior){
		if (anterior == null) anterior = posterior;
		return custoDeSetup[id][anterior.getId()][posterior.getId()];
	}
	
	public void addJob(Job tarefa){
		jobs.add(tarefa);
	}
	
	public void addJob(Job tarefa, int index){
		jobs.add(index, tarefa);
	}
	
	public void removeJob(Job tarefa){
		int indiceParaRemover = -1;
		for (int i = 0; i < jobs.size(); i++){
			Job element = jobs.get(i);
			if(tarefa.getId() == element.getId()){
				indiceParaRemover = i;
				break;
			}
		}
		if(indiceParaRemover != -1)removerJob(indiceParaRemover);
		
	}
	
	public Job removerLastJob(){
		Job tarefa = jobs.remove(jobs.size() - 1);
		return tarefa;
	}
	
	public Job removerJob(int indice){
		Job tarefa = jobs.remove(indice);
		return tarefa;
	}
	
	public void replaceJob(int indiceT1, Job t2){
		jobs.set(indiceT1, t2);
	}
	
	public void swapJobs(int i, int j){
		Job t1 = jobs.get(i);
		Job t2 = jobs.get(j);
		jobs.set(i, t2);
		jobs.set(j, t1);
	}
	
	
	
	public void setJobs(List<Job> tarefaAlocadas) {
		this.jobs = tarefaAlocadas;
	}



	public Job getLastJob(){
		return (getJobs().isEmpty())?null:getJobs().get(getJobs().size() - 1);
	}
	
	public Job getJob(int index){
		return this.jobs.get(index);
	}
		
	public MachineContainer clone(){
		
		MachineContainer clone = new MachineContainer(this.id);
		cloneJob(clone);
		return clone;
		
	}
	
	public void cloneJob(MachineContainer clone){
		
		for (Job tarefa : jobs) 
			clone.addJob(new Job(tarefa.getId(), tarefa.getDueDate(), tarefa.isRestrict(), tarefa.getReleaseDate(), tarefa.getTardinessWeight(), tarefa.getEarlinessWeight(), tarefa.getDueDateEarly()));
		
	}
	
	public int getNumberOfJobs(){
		return jobs.size();
	}

	public List<Job> getJobs() {
		return jobs;
	}
	
	
}
