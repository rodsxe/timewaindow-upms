package operators.localsearch;

import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class InsertionNeighborhoodStructure implements LocalSearch {
	
	private ObjectFunctionEvaluator evaluator;
	private boolean firstImprovement = false;
	
	public InsertionNeighborhoodStructure setFirstImprovement(boolean firstImprovement) {
		this.firstImprovement = firstImprovement;
		return this;
	}

	public InsertionNeighborhoodStructure(ObjectFunctionEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
	}

	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		
		boolean foundNewBest = false;
		List<MachineContainer> maquinasNaoExploradas = s.getMachines(s.getMaquinas());
		
		int indexMachine = evaluator.getLSMachineIndex(rand, s, maquinasNaoExploradas);
		if(indexMachine < 0)return false;
		
		MachineContainer maquinaTardiness = maquinasNaoExploradas.get(indexMachine);
		
		
		while(!foundNewBest && maquinasNaoExploradas.size() > 0){
			
			indexMachine = rand.nextInt(maquinasNaoExploradas.size());
			MachineContainer m2 = maquinasNaoExploradas.remove(indexMachine);
			
			if (!(m2.getNumberOfJobs() > 0)) {
				
				if (m2.getId() == maquinaTardiness.getId()) {
					
					foundNewBest = this.internalLS(s, maquinaTardiness);
					
				}else {
					
					float costM1 = evaluator.getObjectFunctionValue(s, maquinaTardiness);
					float costM2 =  evaluator.getObjectFunctionValue(s, m2);
					
					Pair<Integer, Integer> indice = getBestImprovementInsercaoExterna(s, maquinaTardiness, m2, rand, costM1, costM2);
					
		 			if (indice != null) {
		 				
						Job t1 = maquinaTardiness.getJob(indice.fst);
						maquinaTardiness.removerJob(indice.fst);
						m2.addJob(t1, indice.snd);
						foundNewBest = true;
					
		 			}
	 			
				} 
				
			}
			
		}
		
		return foundNewBest;
		
	}
	
	public boolean internalLS(Solution s, MachineContainer maquina) {
		int numTarefasAlocadas = maquina.getNumberOfJobs();
		int melhorI = -1;
		int melhorP = -1;
		float newCost, actualCost = evaluator.getObjectFunctionValue(s, maquina);
		
		boolean stopping = false;
		
		for (int i = 0; i < numTarefasAlocadas && !stopping; i++) {
			
			Job t1 = maquina.getJob(i);
			
			t1 = maquina.removerJob(i);
			
			for (int j = 0; j < numTarefasAlocadas && !stopping; j++) {
					
				maquina.addJob(t1, j);
				
				newCost = evaluator.getObjectFunctionValue(s, maquina);
				if (newCost < actualCost) {
					
					actualCost = newCost;
					melhorI = i;
					melhorP = j;
					
					if(firstImprovement) stopping = true;
					
				}
				
				maquina.removerJob(j);
					
			}
			
			maquina.addJob(t1, i);
			
		}
		
		
		if (melhorI != -1) {
			
			Job t = maquina.getJob(melhorI);
			maquina.removerJob(melhorI);
			maquina.addJob(t, melhorP);
			return true;
		
		}
		
		return false;
	}
	
	
	
	private Pair<Integer, Integer> getBestImprovementInsercaoExterna(Solution cell, MachineContainer m1, MachineContainer m2, Random rand, float costM1, float costM2) {
		
		boolean stopping = false;
		
		float actualCost = evaluator.getLSCost(costM1, costM2);//Math.max(costM1, costM2);// costM1 + costM2;
						
		Pair<Integer, Integer> indice = null;
		
		Job jobM1; 
				
		for (int i = 0; i < m1.getNumberOfJobs() && !stopping; i++) {
			
			jobM1 = m1.getJob(i);
			
			jobM1 = m1.removerJob(i);
			costM1 = evaluator.getObjectFunctionValue(cell, m1);
						
			for (int j = 0; j <= m2.getNumberOfJobs() && !stopping; j++) {
				
				m2.addJob(jobM1, j);
				costM2 = evaluator.getObjectFunctionValue(cell, m2);
				
				if (evaluator.getLSCost(costM1, costM2) < actualCost) {
					
					actualCost = evaluator.getLSCost(costM1, costM2);
					indice = new Pair<Integer, Integer>(i, j);
					
					if (firstImprovement) stopping = true;
					
				}
								
				m2.removerJob(j);
							
			}
				
			
			m1.addJob(jobM1, i);
			
		}
		
		return indice;
		
	}
	
}
