package Algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import instance.loader.SchedulingInstancesConfig;
import model.Job;
import model.MachineContainer;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class RandomSolutionGenerator {
	
	public RandomSolutionGenerator() {}
	
	public Solution newRandomCell(Random random, ObjectFunctionEvaluator evaluator,SchedulingInstancesConfig config){
		
		MachineContainer.custoDeSetup = config.setupTime;
		MachineContainer.custos = config.processTime;
		MachineContainer[] maquinas = new MachineContainer[config.getNumberOfMachines()];
		for(int i = 0; i < maquinas.length; i++){
			maquinas[i] = new MachineContainer(i);
		}
		Solution s = newRandomCell(random, maquinas, evaluator, config);
		s.setSolucaoInicial(s.getObjectiveFunction());
		return s;
		
	}
	
	private Solution newRandomCell(Random random, MachineContainer[] maquinas, ObjectFunctionEvaluator evaluator, SchedulingInstancesConfig config){
		
		List<Job> list = new ArrayList<Job>();
		
		for(int job = 0; job < config.getNumberOfJobs(); job++) 
			list.add(new Job(job, config.getDueDate(job), config.isRestrict(job), config.getReleaseDate(job), config.getTardinessWeight(job), config.getEarliestWeight(job), config.getDueDateEarly(job)));
				
		for (int i = 0; i < config.getNumberOfJobs(); i++) {
			
			MachineContainer maquina = maquinas[random.nextInt(maquinas.length)];
			int indice = 0;
			indice = random.nextInt(list.size());
			maquina.addJob(list.get(indice));
			list.remove(indice);
			
		}
		
		return newInstance(evaluator, random, maquinas);
		
	}
 
	protected Solution newInstance(ObjectFunctionEvaluator evaluator, Random random, MachineContainer[] maquinas) {
		return new Solution(evaluator, random, maquinas);
	}
	
}
