package Algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import Algorithms.vnd.BasicVND;
import Algorithms.vnd.VND;
import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.localsearch.InsertionNeighborhoodStructure;
import operators.localsearch.SwapNeighborhoodStructure;
import operators.localsearch.IGSNeighborhoodStructure;
import operators.localsearch.LocalSearch;

public class IGS extends BaseAlgorithm {
	
	private Solution best;
	
	private Random random;
	private RandomSolutionGenerator boneMarrow = new RandomSolutionGenerator();
	
	private SchedulingInstancesConfig config;
	private ObjectFunctionEvaluator evaluator;
	
	private LocalSearch vnd;
	
	private float BETA = 0.5414f;
	private float TEMPERATURE = 0.9f;
	private int IT_MAX = 200;
	private float IGS_D_factor = 0.116f;
	private float igs_n_factor = 0.1097f;
	private int IGS_D = 3;
	private int igs_n = 3;
	
	
	private long MAX_NFE;
	
	long tempoInicial;
	
	public float getTemperature(SchedulingInstancesConfig config){
		
		float pTotal = 0;
		int[][] processTime = config.processTime;
		float N = config.getNumberOfJobs();
		float M = config.getNumberOfMachines();
		
		for (int i = 0; i < M; i++) 
			
			for (int j = 0; j < N; j++)  pTotal += processTime[i][j];
				
		
		return pTotal/(N * M * 10);
		
	}
	
	@Override
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand,
			HashMap<String, String> params) {
		
		best = null;
		this.random = rand;
		this.config = config;
		this.evaluator = evaluator;
		float N = config.getNumberOfJobs();
		float M = config.getNumberOfMachines();
		
		this.MAX_NFE = (long)((N + M - 1) / (M - 1)) * (long)(0.5*Math.pow(10, 7));
		
		IT_MAX = (int)((N+M-1));
		
		if(params!= null){

            IGS_D = Math.max(1, (int)(((float)config.getNumberOfJobs()) * new Float(params.get("--igs_d"))));
            BETA = new Float(params.get("--beta"));
            igs_n = Math.max(1, (int)(((float)config.getNumberOfJobs()) * new Float(params.get("--igs_n"))));

		} else {
		
			IGS_D = Math.max(1, (int)(((float)config.getNumberOfJobs()) * IGS_D_factor));
			igs_n = Math.max(1, (int)(((float)config.getNumberOfJobs()) * igs_n_factor));	
		
		}
		this.TEMPERATURE = BETA * (getTemperature(config));
		
		VND vnd = new BasicVND();
		vnd.addLocalSearch(new IGSNeighborhoodStructure(evaluator, igs_n));
		vnd.addLocalSearch(new InsertionNeighborhoodStructure(evaluator));
		vnd.addLocalSearch(new SwapNeighborhoodStructure(evaluator));
		
		this.vnd = vnd;		
		
	}

	@Override
	public List<Solution> initialSolution() {
		
		tempoInicial = System.currentTimeMillis();
		
		List<Solution> pop = new ArrayList<Solution>(1);
		
		Solution attractionCell = new_cell(); 
		pop.add(0, attractionCell);
		pop.add(1, attractionCell);
		
		best = attractionCell;
		
		return pop;
		
	}

	public long getLocalSearchTime() {return 0;}
	
	
	@Override
	public List<Solution> execIteration(List<Solution> solucoes) {
		
		
		int IT_S = 0;		
		while (IT_S < IT_MAX && this.evaluator.getNumberOfObjectFunctionAval() < this.MAX_NFE){
			
			Solution cell = solucoes.get(1);
			
			Solution newCell = cell.clone();
			
			newCell.mutIGS(IGS_D);
			newCell.setObjectiveFunction();
			
			this.vnd.run(this.random, newCell, getTotalTime(), getInitialTime());
			
			if(newCell.getObjectiveFunction() < cell.getObjectiveFunction()) {
				
				solucoes.set(1, newCell);			
				if(newCell.getObjectiveFunction() < best.getObjectiveFunction()) {
					
					best = newCell;
					solucoes.set(0, newCell);			
					
				}
				
				IT_S = 0;
				
			} else{
				
				IT_S++;
				if (this.random.nextFloat() < Math.exp(-(newCell.getObjectiveFunction() - cell.getObjectiveFunction())/(TEMPERATURE) )) 
					solucoes.set(1, newCell);			
				
			}
			
		}
		
		Solution newCell = new_cell();
		solucoes.set(1, newCell);
		
		if(newCell.getObjectiveFunction() < best.getObjectiveFunction()) {
			
			best = newCell;
			solucoes.set(0, newCell);			
			
		}
		
		
		return solucoes;
	}

	public Solution new_cell() {
		
		Solution s = boneMarrow.newRandomCell(random, this.evaluator, this.config);
		this.vnd.run(this.random, s, getTotalTime(), getInitialTime());
		return s;
	
	}
	
	@Override
	public String getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Solution> updateMemory(List<Solution> Ab) {
		Ab.set(0, best);
		return Ab;
	}

	@Override
	public Solution getBest() {
		return best;
	}

}
