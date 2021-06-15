package Algorithms;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import Algorithms.vnd.BasicVND;
import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.localsearch.IGSNeighborhoodStructure;
import operators.localsearch.InsertionNeighborhoodStructure;
import operators.localsearch.SwapNeighborhoodStructure;


public class VNSaiNET extends BaseAlgorithm {

	private Random random;
	private RandomSolutionGenerator boneMarrow;
	private BasicVND vnd;
	
	private SchedulingInstancesConfig schedulingInstancesConfig;
	private ObjectFunctionEvaluator evaluator;
	
	private List<Solution> memory;
	
	private int N = 30;
	private int N_CLONES = 6;
	public static float MAX_NUM_MUTACOES = 4.0f;	
	private float SUPRESS_THRESHOLD = 0.1590f;
	private float CONCENTRATION_DEC_RATE = 0.0194f;
	private int IGS_N = 4;
	private float IGS_N_FACTOR = 0.0756f;
	private float ALPHA = (float)0.6348;
	
	public VNSaiNET() {
		
		super();
		boneMarrow = new RandomSolutionGenerator();
		
	}
	
	@Override
	public Solution getBest() {
		// TODO Auto-generated method stub
		return (memory.isEmpty())?null:memory.get(0);
	}

	@Override
	public String getParameters() {
		return "";
	}
	public long getLocalSearchTime() {return 0;}
	@Override
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand, HashMap<String, String> params) {
		
		this.random = rand;
		this.schedulingInstancesConfig = config;
		this.evaluator = evaluator;
		
		if(params!= null){
			
			N = new Integer(params.get("--n"));
			N_CLONES = new Integer(params.get("--nC"));
			MAX_NUM_MUTACOES = new Float(params.get("--mutRate"));
			SUPRESS_THRESHOLD = new Float(params.get("--supTs"));
			CONCENTRATION_DEC_RATE = new Float(params.get("--dec_rate"));
			ALPHA = new Float(params.get("--alpha"));
			IGS_N = Math.max(1, (int)(((float)config.getNumberOfJobs()) * new Float(params.get("--igs_n"))));
				
		} else {
			IGS_N = Math.max(1, (int)(((float)config.getNumberOfJobs()) * IGS_N_FACTOR)); //new Float(0.0756)));
		}
		
		Solution.MAT_PLUS = CONCENTRATION_DEC_RATE;
		Solution.MAX_MUTATIONS = MAX_NUM_MUTACOES;
		
		BasicVND vnd = new BasicVND();
		vnd.addLocalSearch(new IGSNeighborhoodStructure(evaluator, IGS_N));
		vnd.addLocalSearch(new InsertionNeighborhoodStructure(evaluator));		
		vnd.addLocalSearch(new SwapNeighborhoodStructure(evaluator));
		this.vnd = vnd;
				
	}
	
	private void updateParameters(List<Solution> Ab) {

		for (Solution cell : Ab) {
			
			cell.setMaxNumberOfMutations(calcParameter(cell.getFmat(), cell.getMaxNumberOfMutations(), 0, 0, 1, MAX_NUM_MUTACOES));
			cell.setMutationOpsProb(calcParameter(cell.getFmat(), cell.getMutationOpsProb(), 0, 0, 0, 1));
			
		}
		
	}
	
	private float calcParameter(float fmat, float actualParameter, float mean, float std, float min, float max) {
		
		return Math.min(max, Math.max(min, (fmat * actualParameter) + ((1 - fmat) * ((float)random.nextFloat() * max))));
		
	}
	
	@Override
	public List<Solution> initialSolution() {
		
		this.memory = new ArrayList<Solution>();
		
		List<Solution> result = new ArrayList<Solution>(N);
		
		for (int i = 0; i < N; i++){
			Solution s = new_cell();
			result.add(s);
			//memory.add(s);
		}
		
		result = sortByObjectiveFunction(result);
		
		return result;
	}

	public Solution new_cell() {
		
		Solution s = boneMarrow.newRandomCell(random, this.evaluator, this.schedulingInstancesConfig);
		this.vnd.run(this.random, s, getTotalTime(), getInitialTime());
		return s;
	
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
	
	private List<Solution> sortByFitness(List<Solution> Ab){
		Collections.sort(Ab, new Comparator<Solution>() {
			@Override
			public int compare(Solution s1, Solution s2) {
				
				if (s1.getFitness() > s2.getFitness()) return -1;
				else if (s1.getFitness() < s2.getFitness())return 1;
				else if(s1.getObjectiveFunction() < s2.getObjectiveFunction())return -1;
				else if(s1.getObjectiveFunction() > s2.getObjectiveFunction())return 1;
				else return 0;
				
			}
		});
		return Ab;
	}
	
	private void updateFitness(List<Solution> pop){
		
		pop = sortByObjectiveFunction(pop);
		
		float minMake = 1;
		float maxMake = pop.size();
		
		
		for (int i = 0; i < pop.size(); i++) {
			
			Solution solucao = pop.get(i);
			float funcPercent = 1 - ((new Float(i + 1) - minMake)/(maxMake - minMake));
			solucao.setFitness((1-ALPHA) * funcPercent + ALPHA * solucao.getFmat());
			
		}
		
		
	}
	
	private List<Solution> clonalExpansion(List<Solution> cellsPop){
		
		List<Solution> pop = new ArrayList<Solution>();
		
		int quant_clones;
		double fitness;
		for (int i = 0; i < cellsPop.size(); i++) {
			
			fitness = cellsPop.get(i).getFitness();
			
			quant_clones  = Math.max((int)(fitness * N_CLONES), 1);
			
			for(int c = 0; c < quant_clones; c++) {
				pop.add(cellsPop.get(i).clone());
			}
		
		}
		
		return pop;
	}
	
	private List<Solution> maturate(List<Solution> pop){
		
		for (Solution solucao : pop) {
			
			hypermutation(random, solucao);
			vnd.run(this.random, solucao, getTotalTime(), getInitialTime());
			
		}
		return pop;
	}
	
	private void hypermutation(Random random, Solution cell) {
		
		double exp = Math.max(Math.exp(-cell.getFitness()) * (float)cell.getMaxNumberOfMutations(), 1);
		
		int operacoesExatas = (int)exp;
		for (int i = 0; i < operacoesExatas; i++) {
			float r = random.nextFloat();
			if(r < cell.getMutationOpsProb()) cell.trocaInternaAleatoriamente(1);
			else cell.trocaExternaAleatoriamente();
		};
							
	}
	
	private float obterDistanciaParaMemoria(List<Solution> memoria, Solution s2){
		
		float menor = 1000000000;
		
		for (Solution s1 : memoria) {
			float distancia = s1.calcularDistanciaArrestas(s2);
			if(distancia < menor)menor = distancia;
		}
		
		return menor;
	}
	
		
	private void removeNullAgeCells(List<Solution> Ab){
		
		List<Solution> newMemory = new ArrayList<Solution>();
		
		boolean updateMemory = false;
		
		Iterator<Solution> it = Ab.iterator();
		
		while (it.hasNext()) {
			
			Solution cell = (Solution) it.next();
			
			if (cell.getFmat() < 0.001){
				it.remove();
				if (evaluator.isValidCell(cell)){
					
					this.memory.add(cell);
					updateMemory = true;
				
				}
				
			}
					
		}
		
		if(updateMemory){
			
			this.memory = sortByObjectiveFunction(this.memory);
			if (!this.memory.isEmpty()) newMemory.add(this.memory.remove(0));
			supressAb2(this.memory, newMemory, N, SUPRESS_THRESHOLD);
			this.memory = newMemory;
		
		}
				
	}
	
	
	
	private List<Solution> supress(List<Solution> Ab){
		
		List<Solution> memoriaImunologica = new ArrayList<Solution>();
		
		Ab = sortByFitness(Ab);
		
		memoriaImunologica.add(Ab.remove(0));
		
		List<Solution> excludedCells = supressAb(Ab, memoriaImunologica, N, SUPRESS_THRESHOLD);
		
		updateMemory(excludedCells);
				
		return memoriaImunologica;
		
	}
	
	private void supressAb2(List<Solution> Ab, List<Solution> newAb, int N, float SUPRESS_THRESHOLD) {
		
		float distance;
		for (int i = 0; i < Ab.size() && newAb.size() < N; i++) {
			
			Solution cell = Ab.get(i);
							
			distance = obterDistanciaParaMemoria(newAb, cell);
			
			if (distance >= SUPRESS_THRESHOLD)
				
				newAb.add(cell);
					
		}
		
	}
	
	private List<Solution> supressAb(List<Solution> Ab, List<Solution> newAb, int N, float SUPRESS_THRESHOLD) {
		
		List<Solution> excludedCells = new ArrayList<>();
		
		float distance;
		int i;
		
		for (i = 0; i < Ab.size() && newAb.size() < N; i++) {
			
			Solution cell = Ab.get(i);
							
			distance = obterDistanciaParaMemoria(newAb, cell);
			
			if (distance >= SUPRESS_THRESHOLD)
				
				newAb.add(cell);
			
			else excludedCells.add(cell);
			
		}
		
		for ( ; i < Ab.size(); i++) { excludedCells.add(Ab.get(i));}
		
		return excludedCells;
		
	}
	
	private void updateCellsConcentration(List<Solution> solucoes){
		
		for (Solution solucao : solucoes) {
			
			if (solucao.getObjectiveFunction() >= (solucao.getPreviusObjectiveFunction())) {
				solucao.increseMaturationFactor();
			} else {
				solucao.resetMaturationFactor();
				solucao.setPreviusObjectiveFunction(solucao.getObjectiveFunction());
			}
			
		}
		
	}
	
	public void newCells(List<Solution> cellsPop, int pop_size){
		
		while(cellsPop.size() < pop_size){
			
			Solution s = new_cell();
			cellsPop.add(s);
			
		}
		
	}
	
	public void removeConstrainsValidationCells(List<Solution> bcellPop){
		
		Iterator<Solution> it = bcellPop.iterator();
		while (it.hasNext()) {
			
			Solution cell = (Solution) it.next();
			if (!this.evaluator.isValidCell(cell)) {
				
				it.remove();
				
			}
			
		}
		
	}
	
	@Override
	public List<Solution> execIteration(List<Solution> bcellPop){
		
		updateFitness(bcellPop);
		
		List<Solution> clones = clonalExpansion(bcellPop);
		
		clones = maturate(clones);
		
		bcellPop.addAll(clones);
		
		for (Solution cell : bcellPop) {
			cell.setObjectiveFunction();
		}		
		
		updateCellsConcentration(bcellPop);
		
		updateParameters(bcellPop);
		
		removeNullAgeCells(bcellPop);
		
		if (!bcellPop.isEmpty()){
			
			updateFitness(bcellPop);
			bcellPop = supress(bcellPop);
		
		}
		
		newCells(bcellPop, N);
		
		return bcellPop;
		
	}
	
	public List<Solution> updateMemory(List<Solution> Ab){
		
		List<Solution> newMemory = new ArrayList<Solution>();
		
		Iterator<Solution> it = Ab.iterator();
		
		while (it.hasNext()) {
			
			Solution cell = (Solution) it.next();
			
			if (evaluator.isValidCell(cell)){
				
				this.memory.add(cell);
				
			}
				
		}
		
		this.memory = sortByObjectiveFunction(this.memory);
		
		if (!this.memory.isEmpty()) newMemory.add(this.memory.remove(0));
		
		supressAb2(this.memory, newMemory, N, SUPRESS_THRESHOLD);
		this.memory = newMemory;
		
		return this.memory;
		
	}
	
}
