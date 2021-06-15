package Algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class PathRelinking extends BaseAlgorithm {
	
	private BaseAlgorithm algorithm;
	private List<Solution> eliteSolutions; 
	private Random rand;
	private ObjectFunctionEvaluator evaluator;
	
	private int SIZE_ELITE_POOL = 30;
	
	public PathRelinking(BaseAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand,
			HashMap<String, String> params) {

		algorithm.setInstanceValues(config, evaluator, rand, params);
		this.evaluator = evaluator;
		this.eliteSolutions = new ArrayList<Solution>();
		this.rand = rand;
		if(params!= null){
			this.SIZE_ELITE_POOL = new Integer(params.get("--elite"));
		}
	}

	@Override
	public List<Solution> initialSolution() {
		List<Solution> initial = algorithm.initialSolution();
		this.eliteSolutions.addAll(initial);
		return initial;
	}
	
	private Solution mixedPathRelinking(Solution s0, Solution guide) {
		
		Solution s1 = guide.clone();
		Solution sGuide = s0.clone();
		Solution best = s0.clone();
		Solution aux;
		double newValue; 
		double oldValue = best.getObjectiveFunction();
		boolean improve;
		
		do {
			
			improve = false;
			
			aux = s1;
			s1 = sGuide;
			sGuide = aux;
			
			s1 = doPathRelinking(s1, sGuide);
			newValue = s1.getObjectiveFunction();
			
			if(newValue < oldValue) {
				best = s1;
				improve = true;
				oldValue = newValue;
				
			}
			
		} while(improve);
		
		return best;
		
	}
	
	private Solution doPathRelinking(Solution s0, Solution guide) {
		
		List<Pair<Integer, Integer>> diffJobs = getDiffJobs(s0, guide);
		
		List<Pair<Integer, Integer>> swapPos = getSwapPos(s0, guide, diffJobs);
		
		List<Pair<Integer, Integer>> insertMachines = getInsertMachines(s0, guide);
		
		doSwapPathRelinking(s0, diffJobs, swapPos);
		
		doInsertionPathRelinking(s0, insertMachines);
		
		return s0;
		
	}

	private void doInsertionPathRelinking(Solution s0, List<Pair<Integer, Integer>> insertMachines) {
		
		double actualCost = s0.getObjectiveFunction();
		int bestIndice = -1;
		for (int i = 0; i < insertMachines.size(); i++) {
			
			Pair<Integer, Integer> posSrc = insertMachines.get(i);
			MachineContainer srcMachine = s0.getMaquina(posSrc.fst);
			MachineContainer destMachine = s0.getMaquina(posSrc.snd);
			Job jobsrc = srcMachine.removerLastJob();
			destMachine.addJob(jobsrc);
			double newCost = evaluator.getObjectFunctionValue(s0);
			
			if (newCost < actualCost) {
				
				actualCost = newCost;
				bestIndice = i;
				
			}
			destMachine.removerLastJob();
			srcMachine.addJob(jobsrc);
			
		}
		
		if (bestIndice != -1) {
			
			Pair<Integer, Integer> posSrc = insertMachines.get(bestIndice);
			MachineContainer srcMachine = s0.getMaquina(posSrc.fst);
			MachineContainer destMachine = s0.getMaquina(posSrc.snd);
			Job jobsrc = srcMachine.removerLastJob();
			destMachine.addJob(jobsrc);
			s0.setObjectiveFunction();
			
		}
	}

	private void doSwapPathRelinking(Solution s0, List<Pair<Integer, Integer>> diffJobs, List<Pair<Integer, Integer>> swapPos) {
		
		double actualCost = s0.getObjectiveFunction();
		int bestIndice = -1;
		for (int i = 0; i < diffJobs.size(); i++) {
			
			Pair<Integer, Integer> posSrc = diffJobs.get(i);
			Pair<Integer, Integer> posDest = swapPos.get(i);
			MachineContainer srcMachine = s0.getMaquina(posSrc.fst);
			MachineContainer destMachine = s0.getMaquina(posDest.fst);
			
			if (posDest.snd < destMachine.getNumberOfJobs()) {
				
				Job jobsrc = srcMachine.getJob(posSrc.snd);
				Job jobdest = destMachine.getJob(posDest.snd);
						
				srcMachine.replaceJob(posSrc.snd, jobdest);
				destMachine.replaceJob(posDest.snd, jobsrc);
				
				double newCost = evaluator.getObjectFunctionValue(s0);
				
				if (newCost < actualCost) {
					
					actualCost = newCost;
					bestIndice = i;
					
				}
				
				srcMachine.replaceJob(posSrc.snd, jobsrc);
				destMachine.replaceJob(posDest.snd, jobdest);
				
			}
			
		}
		
		if (bestIndice != -1) {
			
			Pair<Integer, Integer> posSrc = diffJobs.get(bestIndice);
			Pair<Integer, Integer> posDest = swapPos.get(bestIndice);
			MachineContainer srcMachine = s0.getMaquina(posSrc.fst);
			MachineContainer destMachine = s0.getMaquina(posDest.fst);
							
			Job jobsrc = srcMachine.getJob(posSrc.snd);
			Job jobdest = destMachine.getJob(posDest.snd);
					
			srcMachine.replaceJob(posSrc.snd, jobdest);
			destMachine.replaceJob(posDest.snd, jobsrc);
			s0.setObjectiveFunction();
		
		}
		
	}

	private List<Pair<Integer, Integer>> getInsertMachines(Solution s0, Solution guide) {
		
		List<Pair<Integer, Integer>> machinesInsertion = new ArrayList<Pair<Integer, Integer>>();
		
		for (int i = 0; i < s0.getMaquinas().length; i++) {
			
			if (s0.getMaquinas()[i].getNumberOfJobs() > guide.getMaquinas()[i].getNumberOfJobs()) {
				int choosedMachine = rand.nextInt(guide.getMaquinas().length);
				while (s0.getMaquinas()[choosedMachine].getNumberOfJobs() >= guide.getMaquinas()[choosedMachine].getNumberOfJobs())
					choosedMachine = rand.nextInt(guide.getMaquinas().length);
				
				machinesInsertion.add(new Pair<Integer, Integer>(i, choosedMachine));
			}
		}
		
		return machinesInsertion;
	}

	private List<Pair<Integer, Integer>> getSwapPos(Solution s0, Solution guide, List<Pair<Integer, Integer>> diffJobs) {
		
		List<Pair<Integer, Integer>> jobsSwap = new ArrayList<Pair<Integer, Integer>>();
		
		for (Pair<Integer, Integer> jobPos : diffJobs) {
			
			Integer searchJobId = guide.getMaquina(jobPos.fst).getJob(jobPos.snd).getId();
			boolean found = false;
			
			for (int i = 0; i < s0.getMaquinas().length && !found; i++) {
				MachineContainer m = s0.getMaquinas()[i];
				for (int j = 0; j < m.getNumberOfJobs() && !found; j++) {
					if (m.getJob(j).getId() == searchJobId) {
						found = true;
						jobsSwap.add(new Pair<Integer, Integer>(i, j));
					}
				}
			}
		}
		
		return jobsSwap;
	}

	private List<Pair<Integer, Integer>> getDiffJobs(Solution s0, Solution guide) {
		
		List<Pair<Integer, Integer>> jobsDiff = new ArrayList<Pair<Integer, Integer>>();
		
		for (MachineContainer m : guide.getMaquinas()) {
			for (int j = 0; j < m.getNumberOfJobs(); j++) {
				if (j < s0.getMaquina(m.getId()).getNumberOfJobs())
					if (m.getJob(j).getId() != s0.getMaquina(m.getId()).getJob(j).getId()) {
						jobsDiff.add(new Pair<Integer, Integer>(m.getId(), j));
					}
			}
		}
		
		return jobsDiff;
		
	}
	
	public void eliteSetUpdate(Solution sol) {
		
		if (this.eliteSolutions.size() < SIZE_ELITE_POOL) {
			this.eliteSolutions.add(sol);
		} else {
			double minDist = Double.MAX_VALUE;
			int selectedIndex = -1;
			for (int i = 0; i < eliteSolutions.size(); i++) {
			
				Solution eliteSol = eliteSolutions.get(i);
				if (eliteSol.getObjectiveFunction() >= sol.getObjectiveFunction()) {
					double dist = sol.calcularDistanciaArrestas(eliteSol);
					if (dist < minDist) {
						minDist = dist;
						selectedIndex = i;
					}
				}
			}
			if (selectedIndex != -1 && minDist > 0) {
				eliteSolutions.set(selectedIndex, sol);
			}
		}
		
		
	}
		
	@Override
	public List<Solution> execIteration(List<Solution> solucoes) {
		
		solucoes = this.algorithm.execIteration(solucoes);
		
		Solution eliteCell = this.eliteSolutions.get(this.rand.nextInt(this.eliteSolutions.size()));
		int cellPos = this.rand.nextInt(solucoes.size());
		Solution sol = solucoes.get(cellPos);
		
		Solution newSol = mixedPathRelinking(sol, eliteCell);
		this.eliteSetUpdate(newSol);
		solucoes.set(cellPos, sol);
				
		return solucoes;
		
	}

	@Override
	public String getParameters() {
		return null;
	}

	@Override
	public List<Solution> updateMemory(List<Solution> Ab) {
		this.eliteSolutions = sortByObjectiveFunction(this.eliteSolutions);
		return eliteSolutions;
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
	public Solution getBest() {
		this.eliteSolutions = sortByObjectiveFunction(this.eliteSolutions);
		return this.eliteSolutions.get(0);
	}

	@Override
	public long getLocalSearchTime() {
		return 0;
	}

}
