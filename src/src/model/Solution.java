package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import exception.InfactibilidadeException;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class Solution {
	
	private ObjectFunctionEvaluator evaluator;
	private Random rand;
	private Integer nTarefas;
	private float maturationFactor;
	private MachineContainer[] machines;
	private float objectiveFunction;
	private float previusObjectiveFunction;
	private double fitness;
	private float solucaoInicial;
	
	public float maxMutationOps;
	public float mutationOpsProb;
	
	private List<Float> timeSolutions;
	public static float MAX_MAT_FACTOR = 1;
	public static float MAT_PLUS = 0.035f;
	public static float MAX_MUTATIONS = 4f;
		
	private long tempo;
	private Arresta[] vetorDeArrestas;

	public Solution(ObjectFunctionEvaluator evaluator, Random rand, MachineContainer[] maquinas){
		
		this.evaluator = evaluator;
		this.machines = maquinas;
		this.objectiveFunction = calcObjectiveFunction();
		this.previusObjectiveFunction = objectiveFunction;
		this.maturationFactor = 0f;
		timeSolutions = new ArrayList<Float>();
		this.rand = rand;
		maxMutationOps = rand.nextFloat() * MAX_MUTATIONS;
		this.mutationOpsProb = rand.nextFloat();
		
	}
	
	private Solution(ObjectFunctionEvaluator evaluator, 
				Random rand, 
				MachineContainer[] maquinas, 
				float objectiveFunction, 
				float previusObjectiveFunction,
				float prioridadeClonagem,
				double fitness,
				float initialSolution, float parentMaxMutationOps, float parentMutationOpsProb){
		
		this.rand = rand;
		this.evaluator = evaluator;
		this.machines = maquinas;
		this.objectiveFunction = objectiveFunction;
		this.previusObjectiveFunction = previusObjectiveFunction;
		this.maturationFactor = prioridadeClonagem;
		timeSolutions = new ArrayList<Float>();
		this.fitness = fitness;
		this.solucaoInicial = initialSolution;
		
		maxMutationOps = parentMaxMutationOps; 
		this.mutationOpsProb = parentMutationOpsProb;
		
	}
	
	public Integer getNumberOfJobs() {
		if(nTarefas != null)return nTarefas;
		nTarefas = 0;
		for (MachineContainer maquina : machines) {
			nTarefas += maquina.getNumberOfJobs();
		}
		return nTarefas;
	}
	
	public void setEvaluator(ObjectFunctionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public float getProbPriority() {
		return getMaturationFactor();
	}
	
	public MachineContainer[] getMaquinas() {
		return machines;
	}
	
	public void setMaquinas(MachineContainer[] maquinas) {
		this.machines = maquinas;
		this.objectiveFunction = calcObjectiveFunction();
	}

	public List<Float> getTimeSolutions() {
		return timeSolutions;
	}

	public void setTimeSolutions(List<Float> timeSolutions) {
		this.timeSolutions = timeSolutions;
	}

	public void addSolutions(){
		timeSolutions.add(this.getObjectiveFunction());
	}
	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public long getTempo() {
		return tempo;
	}

	public void setTempo(long tempo) {
		this.tempo = tempo;
	}

	public float getPreviusObjectiveFunction() {
		return previusObjectiveFunction;
	}

	public void setPreviusObjectiveFunction(float previusObjectiveFunction) {
		this.previusObjectiveFunction = previusObjectiveFunction;
	}

	public void increseMaturationFactor(){
		maturationFactor = Math.min(MAX_MAT_FACTOR, maturationFactor + MAT_PLUS);
	}
	
	public void resetMaturationFactor() {
		this.maturationFactor = 0f;
	}
	
	public float getMaturationFactor(){
		return maturationFactor;
	}
	public float getFmat() {
		return 1.f - maturationFactor;
	}
	public float getSolucaoInicial() {
		return solucaoInicial;
	}
	
	public void setSolucaoInicial(float solucaoInicial) {
		this.solucaoInicial = solucaoInicial;
	}
	
	public MachineContainer getMaquina(int index){
		return machines[index];
	}
	
	public void setMaxNumberOfMutations(float maxNumberOfMutations) {
		this.maxMutationOps = maxNumberOfMutations;
	}
	
	public float getMaxNumberOfMutations() {
		return this.maxMutationOps;//maxNumberOfMutations;
	}
	
	public float getMutationOpsProb() {
		return mutationOpsProb;
	}

	public void setMutationOpsProb(float mutationOpsProb) {
		this.mutationOpsProb = mutationOpsProb;
	}

	public float calcObjectiveFunction(){
		
		return evaluator.getObjectFunctionValue(this);
		
	}
	
	public float getObjectiveFunction() {
		
		return objectiveFunction;
	
	}
	
	public float setObjectiveFunction() {
		
		this.objectiveFunction = calcObjectiveFunction();
		return objectiveFunction;
		
	}
	
	public Solution clone(){
		
		MachineContainer[] maquinasClones = new MachineContainer[machines.length];
		
		for (int i = 0; i < maquinasClones.length; i++) {
			maquinasClones[i] = machines[i].clone();
		}
		
		Solution s = new Solution(	this.evaluator, 
							this.rand, 
							maquinasClones, 
							this.getObjectiveFunction(), 
							this.getPreviusObjectiveFunction(),
							this.getMaturationFactor(),
							this.getFitness(),
							this.getSolucaoInicial(), this.getMaxNumberOfMutations(), this.getMutationOpsProb());
		
		s.getTimeSolutions().addAll(this.timeSolutions);
		
		return s;
		
	}
	
	
	/**
	 * Metodo que retorna aleatoriamente uma m�quina na lista maquinasNaoExploradas que contenha algum atraso.
	 * @param random
	 * @param machines
	 * @return
	 */
	public MachineContainer getMachineWithCost(Random random, List<MachineContainer> machines) {
		
		MachineContainer choosed = machines.remove(random.nextInt(machines.size()));
		List<MachineContainer> observed = new ArrayList<MachineContainer>();
		float cost;
		
		while (((cost = evaluator.getObjectFunctionValue(this, choosed)) < 0.01) && machines.size() > 0) {
			
			observed.add(choosed);
			choosed = machines.remove(random.nextInt(machines.size()));
		
		}
		
		if(cost == 0)return null;
		
		machines.addAll(observed);
		return choosed;
	
	}
	
	public MachineContainer getMaxCostMachine(Random random, List<MachineContainer> machines) {
		
		float max = 0, actual;
		MachineContainer choosed = null;
		
		for (MachineContainer machineContainer : machines) {
			
			actual = evaluator.getObjectFunctionValue(this, machineContainer);
			if (actual > max) {
				
				max = actual;
				choosed = machineContainer;
				
			}
			
			
		}
		
		return choosed;
	
	}
	
	public float[] getMachineRolete() {
		
		float[] roleta = new float[this.machines.length];
		float[] costs = new float[this.machines.length];
		float cost, previous_cost = 0;;
		float total_cost = 0;
		
		for (int i = 0; i < this.machines.length; i++) {
			
			MachineContainer m = machines[i];
			cost = this.evaluator.getObjectFunctionValue(this, m);
			costs[i] = cost;
			total_cost += cost;
			
		}
		
		for (int i = 0; i < costs.length; i++) {
			
			cost = costs[i];
			roleta[i] = (total_cost == 0)? 0 : cost/total_cost + previous_cost;
			previous_cost += roleta[i];
			
		}
		
		
		return roleta;
		
	}
	
	/**
	 * Metodo que retorna aleatoriamente uma m�quina na lista maquinasNaoExploradas que contenha algum atraso.
	 * @param random
	 * @param machines
	 * @return
	 */
	public MachineContainer getMachineWithHighestCost(Random random, List<MachineContainer> machines) {
		
		MachineContainer choosed;
		
		float cost = 0;
		float highestCost = -1;
		
		int it = 0, machine_selected = 0;
		
		while (it < machines.size()) {
			
			choosed = machines.get(it);
			cost = evaluator.getObjectFunctionValue(this, choosed);
			if (highestCost < cost) {
				
				machine_selected = it;
				highestCost = cost;
				
			}
			
			it++;
		}
		
		if (highestCost <= 0)return null;
		
		return machines.remove(machine_selected);
	
	}
	
	public int getNumberOfMachines() {
	
		return this.machines.length;
		
	}

	public List<MachineContainer> getMachines(MachineContainer[] maquinasOrder) {
		
		List<MachineContainer> maquinas = new ArrayList<MachineContainer>();
		int maxIndice = maquinasOrder.length;
		
		for(int i =0; i < maxIndice; i++ )maquinas.add(maquinasOrder[i]);		
		
		return maquinas;
	
	}
	
		
	
	/**
	 * Metodo que reliza na solu��o aleatoriamento numRealocacoes movimentos de troca interna. 
	 * @param numRealocacoes
	 */
	public void trocaInternaAleatoriamente(int numRealocacoes){
		for (int i = 0; i < numRealocacoes; i++) {
			Random random = new Random();
			MachineContainer maquina = this.machines[random.nextInt(this.machines.length)];
			int numTarefasAlocadas = maquina.getNumberOfJobs();
			if(numTarefasAlocadas > 2){
				int indiceT1 = random.nextInt(numTarefasAlocadas);
				int indiceT2 = random.nextInt(numTarefasAlocadas);
				maquina.swapJobs(indiceT1, indiceT2);
			}
		}
		//fitness = obterCusto();
	}
	
	public void trocaExternaAleatoriamente(){
		
		MachineContainer[] maquinasOrder = getMaquinas();
		
		int indexm1 = evaluator.getLSMachineIndex(rand, this, this.getMachines(maquinasOrder));
		if (indexm1 == -1) indexm1 = this.rand.nextInt(this.machines.length);
		MachineContainer m1 = maquinasOrder[indexm1];
		
		int iM2 = this.rand.nextInt(this.machines.length);
		MachineContainer m2 = this.machines[iM2];
		int numTM1 = m1.getNumberOfJobs();
		int numTM2 = m2.getNumberOfJobs();
		if(numTM1 > 0 && numTM2 > 0 ){
			
			int indiceTM1 = this.rand.nextInt(numTM1);
			int indiceTM2 = this.rand.nextInt(numTM2);
			
			Job t1 = m1.getJob(indiceTM1);
			Job t2 = m2.getJob(indiceTM2);
			m1.replaceJob(indiceTM1, t2);
			m2.replaceJob(indiceTM2, t1);
		}
		
	}
	
	public void insercaoExternaAleatoriamente(){
		
		MachineContainer[] maquinasOrder = getMaquinas();
		int indexm1 = evaluator.getLSMachineIndex(rand, this, this.getMachines(maquinasOrder));
		if (indexm1 == -1) indexm1 = this.rand.nextInt(this.machines.length);
		MachineContainer m1 = maquinasOrder[indexm1];
		
		int numTM1 = m1.getNumberOfJobs();
		if(numTM1 > 0){
			
			int indiceTM1 = this.rand.nextInt(numTM1);
			Job tarefa = m1.getJob(indiceTM1);
			m1.removerJob(indiceTM1);
			
			int iM2 = this.rand.nextInt(this.machines.length);
			MachineContainer m2 = this.machines[iM2];
			int numTM2 = m2.getNumberOfJobs();
			int indiceTM2 = 0;
			if(numTM2 > 1) indiceTM2 = this.rand.nextInt(numTM2);
			m2.addJob(tarefa, indiceTM2);
			
		}
		
	}
	
	public void mutIGS (int N) {
		
		
		MachineContainer sortedMachine;
		
		int bestM = -1;
		int bestJ = -1;
		float newCost, minCost, actualCost;
		
		Job job;
		List<Job> sortedJobs = new ArrayList<Job>();
		
		for (int i = 0; i < N; i++){
			
			sortedMachine = this.machines[rand.nextInt(this.machines.length)];
			if (sortedMachine.getNumberOfJobs() == 0)	i--;
			else sortedJobs.add(sortedMachine.removerJob(rand.nextInt(sortedMachine.getNumberOfJobs())));
		
		}
				
		for (int i = 0; i < N; i++) {
				
			minCost = Float.MAX_VALUE;
			job = sortedJobs.get(i);
			bestM = -1;
			bestJ = -1;
			
			for (int m = 0; m < this.machines.length; m++) {
				
				sortedMachine = this.machines[m];
				actualCost = evaluator.getObjectFunctionValue(this, sortedMachine);
				
				for (int j = 0; j <= sortedMachine.getNumberOfJobs(); j++) {
					
					sortedMachine.addJob(job, j);
					newCost = evaluator.getObjectFunctionValue(this, sortedMachine) - actualCost;
					
					if(newCost < minCost){
						
						minCost = newCost;
						bestJ = j;
						bestM = m;
												
					}
					
					sortedMachine.removerJob(j);
				
				}
				
			}
			
			sortedMachine = this.machines[bestM];
			
			sortedMachine.addJob(job, bestJ);
			
		}
		
	}
	
	public Arresta[] obterVetorDeArrestas(){
		
		if(this.vetorDeArrestas != null)return this.vetorDeArrestas;
		Arresta[] result = new Arresta[getNumberOfJobs()];
		for (MachineContainer maquina : machines) {
			List<Job> tarefas = maquina.getJobs();
			int anterior = -1;
			for (int j = 0; j < tarefas.size(); j++) {
				Job tarefa = tarefas.get(j);
				result[tarefa.getId()] = new Arresta(maquina.getId(), tarefa.getId(), anterior);
				anterior = tarefa.getId();
			}
			
		}
		this.vetorDeArrestas = result;
		return result;
		
	}
	
	public void reset(){
		this.vetorDeArrestas =  null;
	}
	
	public float calcularDistanciaArrestas(Solution s2){
		
		Arresta[] vetorArrestasS1 = this.obterVetorDeArrestas();
		Arresta[] vetorArrestasS2 = s2.obterVetorDeArrestas();
		int distancia = 0;
		for (int j = 0; j < vetorArrestasS1.length; j++) {
			Arresta arresta = vetorArrestasS1[j];
			int i = arresta.getI();
			int k = arresta.getK();
			arresta = vetorArrestasS2[j];
			int w = arresta.getI();
			int z = arresta.getK();
			if(i != w || k != z)distancia++;
		}
		
		return (float)distancia/(float)vetorArrestasS1.length;
	}
	
	public void printTarefas(){
		for (int i = 0; i < machines.length; i++) {
			System.out.println("M" + i + ":");
			List<Job> list = machines[i].getJobs();
			for (Job tarefa : list) {
				System.out.print("T" + tarefa.getId() + "  -----> ");
			}
			System.out.println();
		}
	}
}




