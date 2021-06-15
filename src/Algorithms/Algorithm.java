package Algorithms;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;


public interface Algorithm {
	
	public void setTimeProperties(long totalTime, long initialTime);
	
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand, HashMap<String, String> params);
		
	public List<Solution> initialSolution();
	
	public List<Solution> execIteration(List<Solution> solucoes);
	
	public String getParameters();
	
	public List<Solution> updateMemory(List<Solution> Ab);
	
	public Solution getBest();
	
	public long getLocalSearchTime();
}
