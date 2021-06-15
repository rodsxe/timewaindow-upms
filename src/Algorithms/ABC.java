package Algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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

public class ABC extends BaseAlgorithm {
	
	private Random random;
	private RandomSolutionGenerator boneMarrow = new RandomSolutionGenerator();
	
	private SchedulingInstancesConfig config;
	private ObjectFunctionEvaluator evaluator;
	
	private LocalSearch ls3;
	private long localSearchTime;
	
	private int N = 9;
	private int MAX_MUT_RATE = 4;
	private float THRESHOLT = 3.f;
	private float LIMIT = 100.f;
	private int igs_n = 5;
	private float igs_n_factor = 0.1105f;
	
	
	private Solution bestFoodSource;
	
	public ABC() {
		// TODO Auto-generated constructor stub
	}
	
	public long getLocalSearchTime() {
		return localSearchTime;
	}

	@Override
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand, HashMap<String, String> params) {
		
		bestFoodSource =  null;
		
		this.random = rand;
		this.config = config;
		this.evaluator = evaluator;
		this.localSearchTime = 0;
		//this.evaluator.resetNumberOfObjectFunctionAval();
		
		Solution.MAT_PLUS = 1.f/LIMIT;
		
		if(params!= null){

            N = new Integer(params.get("--n"));
            MAX_MUT_RATE = new Integer(params.get("--mut"));
            THRESHOLT = new Float(params.get("--ts"));
            LIMIT = new Float(params.get("--limit"));
            igs_n = Math.max(1, (int)(((float)config.getNumberOfJobs()) * new Float(params.get("--igs_n"))));


		} else
		
			igs_n = Math.max(1, (int)(((float)config.getNumberOfJobs()) * igs_n_factor));
		
		VND vnd = new BasicVND();
		vnd.addLocalSearch(new IGSNeighborhoodStructure(evaluator, igs_n));
		vnd.addLocalSearch(new InsertionNeighborhoodStructure(evaluator));
		vnd.addLocalSearch(new SwapNeighborhoodStructure(evaluator));
		
		
		this.ls3 = vnd;			
		
	}

	@Override
	public List<Solution> initialSolution() {
		
		List<Solution> pop = new ArrayList<Solution>(N);
		
		for (int i = 1; i < N; i++){
		
			Solution s = newFoodSource();
			pop.add(s);
		
		}
		
		updateTheBestFoodSource(pop);
		
		return pop;
		
	}
	
	public Solution newFoodSource() {
		
		Solution s = boneMarrow.newRandomCell(random, this.evaluator, this.config);
		calcFitness(s);
		return s;
	
	}
	
	private float calcFitness(Solution bee) {
		
		float fitness = 1.f /(1.f + bee.getObjectiveFunction());
		bee.setFitness(fitness);
		return fitness;
	
	}
	
	private Job[] linkedListToArray(MachineContainer[] machines) {
		
		int i = 0;
		Job[] aux = new Job[config.getNumberOfJobs() + config.getNumberOfMachines() - 1];
		List<Job> jobs;
		
		for (int j = 0; j < machines.length; j++) {
			
			MachineContainer machine = machines[j];
			jobs = machine.getJobs();
			for (Job job : jobs) aux[i++] = job;
			if (j < machines.length - 1) aux[i++] = new Job(-1, -1, false, -1, -1, -1, -1);
			
		}
		
		return aux;
		
	}
	
	private MachineContainer[] arrayTolinkedList(Job[] list) {
		
		int m = 0;
		MachineContainer[] machines = new MachineContainer[config.getNumberOfMachines()];
		machines[m] = new MachineContainer(m);
		for (Job job : list) {
			
			if (job.getId() ==-1) {m++; machines[m] = new MachineContainer(m); }
			else machines[m].addJob(job);
			
		}
		
		return machines;
		
	}
	
	private Solution crossOver(Solution p1, Solution p2){
		
		int pos;
		Job job;
		boolean foundTheJob;
		
		Job[] sol = new Job[config.getNumberOfJobs() + config.getNumberOfMachines() - 1];
		Job[] parent1 = linkedListToArray(p1.getMaquinas());
		Job[] parent2 = linkedListToArray(p2.getMaquinas());
		
		int tempPoint1 = random.nextInt(parent1.length);
		int tempPoint2 = random.nextInt(parent1.length);
		
		int point1 = Math.min(tempPoint1, tempPoint2);
		int point2 = Math.max(tempPoint1, tempPoint2) + 1;
	
		for (int i = point1; i < point2; i++) sol[i] = parent1[i];
		
		for (int i = 0; i < point1; i++) if (parent1[i].getId() == -1) sol[i] = parent1[i];
		
		for (int i = point2; i < parent1.length; i++) if (parent1[i].getId() == -1) sol[i] = parent1[i];
		
		pos = 0;
		if (sol[pos] != null) while (++pos < sol.length && sol[pos] != null);
		
		for (int i = 0; i < parent1.length; i++) {
			
			job = parent2[i];
			if (job.getId() > -1) {
				
				foundTheJob = false;
				for (int j = point1; j < point2 && !foundTheJob; j++) 
				
					if (sol[j].getId() == job.getId()) foundTheJob = true;
				
				if (!foundTheJob) {
					
					sol[pos] = job;
					while (++pos < sol.length && sol[pos] != null);
										
				}
			
			}
			
		}
		
		
		Solution newBee = new Solution(evaluator, random, this.arrayTolinkedList(sol));
		newBee.setPreviusObjectiveFunction(p2.getObjectiveFunction());
		return newBee;
		
	}
	
	private void employeeBeePhase(List<Solution> pop, double bestFitness){
		
		Solution bestFoodSourceInPop = getBestFoodSourceInPop(pop);
		Solution newFoodSource;
		int mut_rate = random.nextInt(MAX_MUT_RATE) + 1;
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodsource = pop.get(i);
			if (foodsource.getFitness() == bestFitness) {
				
				newFoodSource = foodsource.clone(); 
				newFoodSource.setPreviusObjectiveFunction(foodsource.getObjectiveFunction());
				newFoodSource.mutIGS(mut_rate);
				
			}			
			else newFoodSource = this.crossOver(bestFoodSourceInPop, foodsource);
			
			newFoodSource.setObjectiveFunction();
			calcFitness(newFoodSource);
			
			
			if ((newFoodSource.getFitness() - bestFitness)/bestFitness < THRESHOLT || newFoodSource.getFitness() >= bestFitness)
				localSearch(newFoodSource);
			
			calcFitness(newFoodSource);
						
			if (newFoodSource.getFitness() >= foodsource.getFitness()) pop.set(i, newFoodSource);
			
		}
		
	}

	private void localSearch(Solution newFoodSource) {
		
		long time = System.currentTimeMillis();
		
		this.ls3.run(random, newFoodSource, getTotalTime(), getInitialTime());
		
		this.localSearchTime += System.currentTimeMillis() - time;
		
	}
	
	private void onlookerPhase(List<Solution> pop, double bestFitness){
		
		Solution newFoodSource;
		int mut_rate = random.nextInt(MAX_MUT_RATE) + 1;
		double totalFitness = 0;
		double pi;
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodsource = pop.get(i);
			totalFitness += foodsource.getFitness();
			
		}
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodsource = pop.get(i);
			pi = foodsource.getFitness()/totalFitness;
			
			
			if (random.nextDouble() < pi) {
				
				newFoodSource = foodsource.clone(); 
				newFoodSource.setPreviusObjectiveFunction(foodsource.getObjectiveFunction());
				newFoodSource.mutIGS(mut_rate);
			
				newFoodSource.setObjectiveFunction();
				calcFitness(newFoodSource);
				
				if ((newFoodSource.getFitness() - bestFitness)/bestFitness < THRESHOLT || newFoodSource.getFitness() >= bestFitness)
					localSearch(newFoodSource);
				
				newFoodSource.setObjectiveFunction();
				calcFitness(newFoodSource);
							
				if (newFoodSource.getFitness() >= foodsource.getFitness()) pop.set(i, newFoodSource);
				
			}
			
		}
		
	}
	
	private void scoutPhase(List<Solution> pop){
	
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodSource = pop.get(i);
			
			if (foodSource.getObjectiveFunction() >= (foodSource.getPreviusObjectiveFunction())) {
				
				foodSource.increseMaturationFactor();
				if (foodSource.getFmat() < 0.1) {
					if(foodSource.getObjectiveFunction() < bestFoodSource.getObjectiveFunction()) 
						bestFoodSource = foodSource;
					
					pop.set(i, newFoodSource());
					
				}
				
			} else {
				
				foodSource.resetMaturationFactor();
				foodSource.setPreviusObjectiveFunction(foodSource.getObjectiveFunction());
			
			}
			
		}
		
	}
	
	private Solution getBestFoodSourceInPop(List<Solution> pop) {
	
		Solution best = pop.get(0);
		
		for (int i = 1; i < pop.size(); i++) {
			
			Solution foodSource = pop.get(i);
			if (best.getFitness() < foodSource.getFitness()) best = foodSource;
			
		}
		
		return best;
		
	}
	
	private void updateTheBestFoodSource(List<Solution> pop) {
		
		if(this.bestFoodSource == null) this.bestFoodSource = pop.get(0);
			
		for (int i = 0; i < pop.size(); i++) {
			
			Solution foodSource = pop.get(i);
			if (this.bestFoodSource.getFitness() < foodSource.getFitness()) this.bestFoodSource = foodSource;
			
		}
	
	}
	
	@Override
	public List<Solution> execIteration(List<Solution> pop) {
		
		employeeBeePhase(pop, this.bestFoodSource.getFitness());
		
		onlookerPhase(pop, this.bestFoodSource.getFitness());
		
		scoutPhase(pop);
		
		updateTheBestFoodSource(pop);
		
		return pop;
	}
	
	private List<Solution> sortByObjectiveFunction(List<Solution> Ab){
		Collections.sort(Ab, new Comparator<Solution>() {
			@Override
			public int compare(Solution s1, Solution s2) {
				
				if(s1.getObjectiveFunction() < s2.getObjectiveFunction())return -1;
				else if(s1.getObjectiveFunction() > s2.getObjectiveFunction())return 1;
				else if(s1.getFmat() < s2.getFmat())return 1; 
				else if(s1.getFmat() > s2.getFmat())return -1; 
				else return 0;
				
			}
		});
		return Ab;
	}
	
	@Override
	public String getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Solution> updateMemory(List<Solution> Ab) {
		Ab.add(0, bestFoodSource);
		
		return sortByObjectiveFunction(Ab);
	}

	@Override
	public Solution getBest() {
		
		return this.bestFoodSource;
		
	}

}
