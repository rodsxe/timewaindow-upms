package Algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import Algorithms.vnd.BasicVND;
import Algorithms.vnd.VND;
import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.localsearch.InsertionNeighborhoodStructure;
import operators.localsearch.SwapNeighborhoodStructure;
import operators.localsearch.IGSNeighborhoodStructure;
import operators.localsearch.LocalSearch;

public class GA extends BaseAlgorithm {
	
	private Random random;
	private RandomSolutionGenerator boneMarrow = new RandomSolutionGenerator();
	
	private SchedulingInstancesConfig config;
	private ObjectFunctionEvaluator evaluator;
	
	private LocalSearch vnd;
	
	private int N = 60;
	private float Pc = 0.5105f;
	private float Pmut = 0.1234f;
	private float Ps = 0.3984f;
	private float igs_n_factor = 0.0831f;
	private int igs_n = 3;
	
	private Solution best;
	
	public long getLocalSearchTime() {return 0;}
	
	@Override
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand,
			HashMap<String, String> params) {
		
		best = null;
		this.random = rand;
		this.config = config;
		
		this.evaluator = evaluator;
		
		if(params!= null){
				
				N = new Integer(params.get("--n"));
				Pc = new Float(params.get("--pc"));
				Pmut = new Float(params.get("--pmut"));
				Ps = new Float(params.get("--ps"));
				igs_n = Math.max(1, (int)(((float)config.getNumberOfJobs()) * new Float(params.get("--igs_n"))));
				
		} else
		
			igs_n = Math.max(1, (int)(((float)config.getNumberOfJobs()) * igs_n_factor));
		
		
		VND vnd = new BasicVND();
		vnd.addLocalSearch(new IGSNeighborhoodStructure(evaluator, igs_n));
		vnd.addLocalSearch(new InsertionNeighborhoodStructure(evaluator));
		vnd.addLocalSearch(new SwapNeighborhoodStructure(evaluator));
		
		this.vnd = vnd;
		
	}
	
	public Solution newChromosome() {
		
		Solution s = boneMarrow.newRandomCell(random, this.evaluator, this.config);
		calcFitness(s);
		return s;
	
	}
	
	private float calcFitness(Solution chromosome) {
		
		float fitness = 1.f /(1.f + chromosome.getObjectiveFunction());
		chromosome.setFitness(fitness);
		return fitness;
	
	}
	
	@Override
	public List<Solution> initialSolution() {
		
		List<Solution> pop = new ArrayList<Solution>(N);
		
		for (int i = pop.size(); i < N; i++){
		
			Solution s = newChromosome();
			pop.add(s);
		
		}
		
		sortByObjectiveFunction(pop);
		
		this.best = pop.get(0);
		
		return pop;
	}
	
	private void updateFitness(List<Solution> pop) {
		
		for (Solution chromosome : pop) calcFitness(chromosome);
			
	}
	
	private void localSearch(List<Solution> pop) {
		
		for (Solution chromosome : pop) vnd.run(random, chromosome, getTotalTime(), getInitialTime());
			
	}
	
	private Solution selectionChromossome(List<Solution> pop, double[] rouletteWheel) {
		
		double rand = this.random.nextDouble();
		
		for (int i = 0; i < rouletteWheel.length; i++) {
			
			double pi = rouletteWheel[i];
			if (pi > rand) return pop.get(i);
					
		}
        
        return null;
	
	}
	
	private Solution[] parrentSelection(List<Solution> pop) {
	
		Solution[] selectedCells = new Solution[pop.size()*2];
		double totalObjFunction = 0;
		double pi;
		double[] rouletteWheel = new double[pop.size()];
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution chromossome = pop.get(i);
			totalObjFunction += chromossome.getFitness();
			
		}
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution chromossome = pop.get(i);
			pi = chromossome.getFitness() / totalObjFunction;
			rouletteWheel[i] = ((i == 0)? 0: rouletteWheel[i - 1]) + pi;
			
		}
		
		for (int i = 0; i < selectedCells.length; i += 2) {
		
			selectedCells[i] = selectionChromossome(pop, rouletteWheel);
			selectedCells[i + 1] = selectionChromossome(pop, rouletteWheel);
			
		}
		
		
		return selectedCells;
		
	}
	
	private Solution crossover(Solution parent1, Solution parent2, int[] points) {
		
		TreeMap<Integer, Integer> jobsScheduling = new TreeMap<Integer, Integer>();
		Job job;
		MachineContainer[] machines = new MachineContainer[points.length];
				
		for (int i = 0; i < machines.length; i++) {
			
			MachineContainer machineP1 = parent1.getMaquina(i);
			MachineContainer machinesOffspring = new MachineContainer(i);
			
			for (int j = 0; j < points[i]; j++) {
				
				job = machineP1.getJob(j);
				machinesOffspring.addJob(job);				
				jobsScheduling.put(job.getId(), job.getId());
				
			}
			
			machines[i] = machinesOffspring;
			
		}
		
		for (int i = 0; i < machines.length; i++) {
			
			MachineContainer machineP2 = parent2.getMaquina(i);
			MachineContainer machinesOffspring = machines[i];
						
			for (int j = points[i]; j < machineP2.getNumberOfJobs(); j++) {
				
				job = machineP2.getJob(j);
				if (!jobsScheduling.containsKey(job.getId())) {
					
					machinesOffspring.addJob(job);
					jobsScheduling.put(job.getId(), job.getId());
					
				} 
				
			}
			
		}
		
		for (int i = 0; i < machines.length; i++) {
			
			MachineContainer machineP1 = parent1.getMaquina(i);
			
			for (int j = points[i]; j < machineP1.getNumberOfJobs(); j++) {
				
				job = machineP1.getJob(j);
				if (!jobsScheduling.containsKey(job.getId())) 
					
					machines[random.nextInt(machines.length)].addJob(job);;
			
			}
			
		}
		
		return new Solution(evaluator, random, machines);
		
	}
	
	private List<Solution> crossover(Solution[] parents) {
		
		List<Solution> offsprings = new ArrayList<Solution>();
		
		for (int i = 0; i < parents.length; i += 2) {
			
			if (this.random.nextDouble() < Pc) {
				
				Solution parent1 = parents[i];
				Solution parent2 = parents[i + 1];
				int[] points = new int[parent1.getMaquinas().length];
				
				for (int j = 0; j < points.length; j++) {
					
					int ni = Math.min(parent1.getMaquina(j).getNumberOfJobs(), parent2.getMaquina(j).getNumberOfJobs());
					
					points[j] = (ni > 0)? random.nextInt(ni) : 0; 
					
				}
				offsprings.add(crossover(parent1, parent2, points));
				offsprings.add(crossover(parent2, parent1, points));
				
			}
			
		}
		
		return offsprings;
			
	}
	
	private void mutation(List<Solution> offsprings) {
	
		for (Solution chromossome : offsprings) {
			
			if (this.random.nextDouble() < Pmut) chromossome.insercaoExternaAleatoriamente();
			
		}
	
	}
	
	private void selection(List<Solution> pop, List<Solution> offsprings) {
		
		int replaceSols = (int)(offsprings.size() * Ps);
		
		for (int i = pop.size() - replaceSols; i < pop.size(); i++) {
			
			Solution chromossome = offsprings.remove(random.nextInt(offsprings.size()));
			pop.set(i, chromossome);
			
		}
		
	}
	
	private List<Solution> sortByObjectiveFunction(List<Solution> Ab){
		Collections.sort(Ab, new Comparator<Solution>() {
			@Override
			public int compare(Solution s1, Solution s2) {
				
				if(s1.getObjectiveFunction() < s2.getObjectiveFunction())return -1;
				else if(s1.getObjectiveFunction() > s2.getObjectiveFunction())return 1;
				else return 0;
				
			}
		});
		return Ab;
	}

	@Override
	public List<Solution> execIteration(List<Solution> pop) {
		
		updateFitness(pop);
		
		Solution[] parents = parrentSelection(pop);
		
		List<Solution> offsprings = crossover(parents);
		
		mutation(offsprings);
		
		selection(pop, offsprings);
				
		localSearch(pop);
		
		sortByObjectiveFunction(pop);
		
		this.best = pop.get(0);
		
		return pop;
	}

	@Override
	public String getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Solution> updateMemory(List<Solution> Ab) {
		
		return Ab;
	
	}

	@Override
	public Solution getBest() {
		return best;
	}

}
