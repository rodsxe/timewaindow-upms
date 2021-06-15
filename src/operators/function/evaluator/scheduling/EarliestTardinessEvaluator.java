package operators.function.evaluator.scheduling;

import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import model.Job;
import model.MachineContainer;
import model.base.Container;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class EarliestTardinessEvaluator implements ObjectFunctionEvaluator {

	private SchedulingInstancesConfig schedulingConfig;
	private long numberOfObjectFunctionAval;
	private boolean isToContEval = true;
	
	public SchedulingInstancesConfig getSchedulingConfig() {
		return schedulingConfig;
	}

	public void setSchedulingConfig(SchedulingInstancesConfig schedulingConfig) {
		this.schedulingConfig = schedulingConfig;
	}
	
	public void resetNumberOfObjectFunctionAval() {
		this.numberOfObjectFunctionAval = 0;
	}
	
	public long getNumberOfObjectFunctionAval() {return this.numberOfObjectFunctionAval;}
	
	public long addNEFunction() {
		
		long new_numberOfObjectFunctionAval = this.numberOfObjectFunctionAval + ((long)1);
		
		assert(new_numberOfObjectFunctionAval>this.numberOfObjectFunctionAval);
		
		return this.numberOfObjectFunctionAval = new_numberOfObjectFunctionAval;
		
	}
	
	@Override
	public int getLSMachineIndex(Random rand, Solution cell, List<MachineContainer> machines) {
		
		float cost = -1;
		int index = -1;
		int cont = 0;
		this.isToContEval = false;
		
		while (cost <= 0 && cont < machines.size()) {
			
			index = rand.nextInt(machines.size());
			MachineContainer choosed = machines.get(index);
			cost = getObjectFunctionValue(cell, choosed);
			cont++;
			
		}
		
		this.isToContEval = true;
		this.addNEFunction();
		
		if(cost == 0) return -1;
		
		return index;
		
	}
	
	@Override
	public float getLSCost(float costM1, float costM2) {
		return costM1 + costM2;
	}
	
	public float getObjectFunctionValue(Solution cell) {
		
		float custoTotal = 0;
		MachineContainer[] maquinas = cell.getMaquinas();
		this.isToContEval = false;
		for(int i = 0; i < maquinas.length; i++){
			
			MachineContainer maquina = maquinas[i];
			custoTotal += getObjectFunctionValue(cell, maquina);
		
		}
		this.isToContEval = true;
		this.addNEFunction();
		return custoTotal;
	
	}
	
	@Override
	public float getObjectFunctionValue(Solution cell, Container container) {
		
		long initialTime = System.nanoTime();
				
		MachineContainer machine = (MachineContainer)container;
		
		List<Job> jobs = machine.getJobs();
		
		if (jobs.isEmpty()) return 0;
		
		int[] completion_times = new int[jobs.size()];
		int[] starting_time = new int[jobs.size()];
		
		initStartAndCompletionTime(machine.getId(), jobs, starting_time, completion_times);
		
		getIdleTime(machine.getId(), jobs, starting_time, completion_times);
		
		float result = calcTotalCost(jobs, completion_times);
		long end = System.nanoTime(); 
		if (isToContEval) this.addNEFunction();
		//System.out.println("test:" + (end - initialTime));
		container.setCost(result);
		return result;
		
	}
	
	public void initStartAndCompletionTime(int machine, List<Job> jobs, int[] starting_time, int[] completion_times){
		
		Job job, previus_job = jobs.get(0);
		int procTime, previous_completion_time = 0;
				
		for (int i = 0; i < jobs.size(); i++) {
			
			job = jobs.get(i);
			procTime = 	  schedulingConfig.getProcessTime(machine, job.getId())
						+ schedulingConfig.getSetupTime(machine, previus_job.getId(), job.getId());
			
			starting_time[i] = previous_completion_time;
			completion_times[i] = starting_time[i] + procTime;
			
			previus_job = job;
			previous_completion_time = completion_times[i];
			
		}
		
	}
	
	public int getInitialBlock(int i, int[] block , int[] starting_time, int[] completion_times){
		
		boolean endBlock = false;
		int block_size = 1;
		block[block_size - 1] = i;
				
		for (int j = i + 1; j < completion_times.length && !endBlock; j++) {
			
			if (completion_times[j-1] >= starting_time[j]) {
				
				block_size++;
				block[block_size - 1] = j;
				
			} else endBlock = true;
			
		}
		
		return block_size;
		
	}
	
	public void getIdleTime(int machine, List<Job> jobs, int[] starting_time, int[] completion_times){
		
		int block_size, last_block_item, idle_stride;
		float bc_cost;
		
		int[] block = new int[jobs.size()];
		
		for (int i = jobs.size() - 1; i >= 0 ; i--) {
			
			if(completion_times[i] < jobs.get(i).getDueDateEarly()) {
				
				block_size = getInitialBlock(i, block, starting_time, completion_times);
				bc_cost = get_BC_cost(jobs, block_size, completion_times, block);
				
				while (bc_cost < 0) {
					
					last_block_item = block[block_size - 1];
					int m1 = min_tardiness(jobs, block_size, completion_times, block);
					int m3 = (last_block_item < jobs.size() - 1)? starting_time[last_block_item + 1] - completion_times[last_block_item] : Integer.MAX_VALUE;
					int m2 = min_earliest(jobs, block_size, completion_times, block);
					
					idle_stride = Math.min(m2, Math.min(m1, m3));
					
					for (int j = 0; j < block_size; j++) {
						
						starting_time[block[j]] += idle_stride;
						completion_times[block[j]] += idle_stride;
						
					}
					
					while(last_block_item < jobs.size() - 1 && completion_times[last_block_item] >= starting_time[last_block_item + 1]) {
						
						last_block_item += 1;
						block[block_size] =  last_block_item;
						block_size++;
											
					}
					
					
					bc_cost = get_BC_cost(jobs, block_size, completion_times, block);
					
				}
				
			}
			
		}
		
	}
	
	private int min_tardiness(List<Job> tarefas, int bc_size, int[] completionJobTime, int[] BC) {
		
		int tardiness, min_tardiness = Integer.MAX_VALUE;
		int index, completionTime;
		Job job;
		
		for(int j = 0; j < bc_size; j++){
			
			index = BC[j];
			job = tarefas.get(index);
			completionTime = completionJobTime[index];
			if (job.getDueDateEarly() <= completionTime && completionTime < job.getDueDate()) {
				
				tardiness = job.getDueDate() - completionTime;
				if (tardiness < min_tardiness) min_tardiness = tardiness;
							
			}
			
		}
		
		return min_tardiness;
		
	}
	
	private int min_earliest(List<Job> tarefas, int bc_size, int[] completionJobTime, int[] BC) {
		
		int earliest, min_earliest = Integer.MAX_VALUE;
		int index, completionTime;
		Job job;
		
		for(int j = 0; j < bc_size; j++){
			
			index = BC[j];
			job = tarefas.get(index);
			completionTime = completionJobTime[index];
			if (completionTime < job.getDueDateEarly()) {
				
				earliest = job.getDueDateEarly() - completionTime;
				if (min_earliest > earliest) min_earliest = earliest;
				
			}
			
		}
		
		return min_earliest;
	
	}
	
	private float get_BC_cost(List<Job> tarefas, int bc_size, int[] t1, int[] BC) {
		
		float bc_cost = 0;
		int index, completionTime;
		Job job;
		
		for(int j = 0; j < bc_size; j++){
			
			index = BC[j];
			job = tarefas.get(index);
			completionTime = t1[index];
			
			if(completionTime >= job.getDueDate()) bc_cost += job.getTardinessWeight();
			if(completionTime < job.getDueDateEarly()) bc_cost -= job.getEarlinessWeight(); 
			
		}
		
		return bc_cost;
	}
	
	private float calcTotalCost(List<Job> jobs, int[] completion_times){
		
		float total_cost = 0;
		Job job;
		for (int i = 0; i < jobs.size(); i++) {
			
			job = jobs.get(i);
			total_cost += 	Math.max(0, job.getDueDateEarly() - completion_times[i]) * job.getEarlinessWeight() +
							Math.max(0, completion_times[i] - job.getDueDate()) * job.getTardinessWeight();
			
			job.setEarliness(Math.max(0, job.getDueDateEarly() - completion_times[i]));
			job.setTardiness(Math.max(0, completion_times[i] - job.getDueDate()));
			job.setCompletionTime(completion_times[i]);
		}
		
		return total_cost;
		
	}
	
	public static void swapJobs(Job[] jobs, int i, int j) {
	
		Job aux = jobs[i];
		jobs[i] = jobs[j];
		jobs[j] = aux;
		
	}
	
	public static float newSol(Job[] partialSol, Job[] jobs, int atualPosition, ObjectFunctionEvaluator evaluator) {
		
		float minSolCost = Float.MAX_VALUE; 
		float solCost;
		
		if (atualPosition < partialSol.length) {
			
			for (int i = atualPosition; i < jobs.length; i++) {
				
				partialSol[atualPosition] = jobs[i];
				swapJobs(jobs, i, atualPosition);
				solCost = newSol(partialSol, jobs, atualPosition + 1, evaluator);
				swapJobs(jobs, i, atualPosition);
				
				minSolCost = (solCost < minSolCost)? solCost : minSolCost;
				
			}
			
			
		} else {
			
			MachineContainer[] machines = {new MachineContainer(0, partialSol)};
			Solution cell = new Solution(evaluator, new Random(), machines);
			minSolCost = cell.calcObjectiveFunction();
		}
		
		return minSolCost;
		
	}
	
	
	public boolean isValidCell(Solution cell) {
		return true;
	}
	
	public void setCONSTRAINT_PENALTY(float cONSTRAINT_PENALTY) {}
	
	public void writeResult(Solution cell, PrintWriter printWriter){
		
		printWriter.print(";" + getObjectFunctionValue(cell)+";");
		
	}
	
}
