package Algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import Algorithms.vnd.BasicVND;
import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.localsearch.InsertionNeighborhoodStructure;
import operators.localsearch.SwapNeighborhoodStructure;
import operators.localsearch.IGSNeighborhoodStructure;

public class GRASP_ILS extends BaseAlgorithm {
	
	private Solution best;
	private Random random;
	private BasicVND vnd;
	
	private SchedulingInstancesConfig schedulingInstancesConfig;
	private ObjectFunctionEvaluator evaluator;
	
	private int IT_MAX = 300;
	private int IGS_D = 3;
	private int IGS_N = 3;
	float ALPHA = 0.2953f;
	float IGS_N_FACTOR =  0.1001f;
	float IGS_D_FACTOR =  0.1059f;
	
	
	public GRASP_ILS(){}

	@Override
	public void setInstanceValues(SchedulingInstancesConfig config, ObjectFunctionEvaluator evaluator, Random rand,
			HashMap<String, String> params) {
		
		this.random = rand;
		this.schedulingInstancesConfig = config;
		this.evaluator = evaluator;
		
		if(params!= null){
			
			IT_MAX = new Integer(params.get("--it_max"));
			IGS_N = Math.max(1, (int)(((float)config.getNumberOfJobs()) * new Float(params.get("--igs_n"))));
			IGS_D = Math.max(1, (int)(((float)config.getNumberOfJobs()) * new Float(params.get("--igs_d"))));
			this.ALPHA = new Float(params.get("--alpha"));
		
		} else {
		
			IGS_N = Math.max(1, (int)(((float)config.getNumberOfJobs()) * IGS_N_FACTOR));
			IGS_D = Math.max(1, (int)(((float)config.getNumberOfJobs()) * IGS_D_FACTOR));
		
		}
		
		BasicVND vnd = new BasicVND();
		vnd.addLocalSearch(new IGSNeighborhoodStructure(evaluator, IGS_N));
		vnd.addLocalSearch(new InsertionNeighborhoodStructure(evaluator));		
		vnd.addLocalSearch(new SwapNeighborhoodStructure(evaluator));
		this.vnd = vnd;

	}

	@Override
	public List<Solution> initialSolution() {
		
		List<Solution> result = new ArrayList<Solution>(1);
		
		Solution s = new_cell();
		result.add(s);
		best = s;
		return result;
		
	}

	public long getLocalSearchTime() {return 0;}
	
	
	public Solution new_cell() {
		
		Solution s = construction();
		this.vnd.run(this.random, s, getTotalTime(), getInitialTime());
		return s;
	
	}
	
	private MachineContainer[] createMachinesVector(SchedulingInstancesConfig config) {
		
		MachineContainer[] machines = new MachineContainer[config.getNumberOfMachines()];
		for(int i = 0; i < machines.length; i++){
			machines[i] = new MachineContainer(i);
		}
		return machines;
	}
	
	private List<Job> getJobsList(SchedulingInstancesConfig config) {
		
		List<Job> jobs = new ArrayList<Job>();
		for(int i = 0; i < config.getNumberOfJobs(); i++){
			
			jobs.add(config.getJob(i));
		
		}
		
		return jobs;
	}
	
	public Solution construction() {
		
		List<Job> jobs = getJobsList(schedulingInstancesConfig);
		
		MachineContainer[] machines = createMachinesVector(schedulingInstancesConfig);
		
		randomGreedyScheduling(machines, jobs, evaluator, ALPHA, random);
				
		return new Solution(evaluator, random, machines);
	
	}
	
	
	public MachineContainer[] randomGreedyScheduling(MachineContainer[] machines, List<Job> jobs, ObjectFunctionEvaluator evaluator, float alpha, Random rand) {
		
		while(!jobs.isEmpty()){
			
			Pair<Integer, Integer> selectedElement = selectJobLRC(jobs, machines, evaluator, alpha, rand);
			MachineContainer maquina = machines[selectedElement.fst];
			maquina.addJob(jobs.remove(selectedElement.snd.intValue()));
				
		}
		
		return machines;
	}
	
	private Pair<Integer, Integer> selectJobLRC(List<Job> jobs, MachineContainer[] maquinas, ObjectFunctionEvaluator evaluator, float alpha, Random rand) {
		
		Pair<Float, Pair<Integer, Integer>>[] list = getLRC(jobs, maquinas, evaluator);
		
		Arrays.sort(list, new LRCComparator());
		
		int selectedElement = rand.nextInt(Math.max(1, (int)(alpha * list.length)));
		
		Pair<Float, Pair<Integer, Integer>> element = list[selectedElement];
				
		return element.snd;
		
	}
	
	

	
	private Pair<Float, Pair<Integer, Integer>>[] getLRC(List<Job> jobs, MachineContainer[] maquinas, ObjectFunctionEvaluator evaluator) {
		
		int lrcSize = jobs.size() * maquinas.length;
		Pair<Float, Pair<Integer, Integer>>[] lrc = new Pair[lrcSize];
		for (int j = 0; j < maquinas.length; j++) {
			
			MachineContainer m = maquinas[j];
						
			for (int i = 0; i < jobs.size(); i++) {
			
				Job job = jobs.get(i);
				m.addJob(job);
				float value = evaluator.getObjectFunctionValue(null, m);
				lrc[j * jobs.size() + i] = new Pair<Float, Pair<Integer,Integer>>(value, new Pair<Integer, Integer>(j, i));
				m.removerLastJob();				
			}
		
		}
		
		
		return lrc;
		
	}
	
	
	public class LRCComparator implements Comparator<Pair<Float, Pair<Integer, Integer>>> {
		
	    public int compare(Pair<Float, Pair<Integer, Integer>> o1, Pair<Float, Pair<Integer, Integer>> o2) {
	        return o1.fst.compareTo(o2.fst);
	    }
	    
	}
	
	@Override
	public List<Solution> execIteration(List<Solution> solucoes) {
		
		solucoes.set(0, new_cell());
		
		int IT_S = 0;		
		while (IT_S < IT_MAX){
			
			Solution cell = solucoes.get(0);
			Solution newCell = cell.clone();
			
			newCell.mutIGS(IGS_D);
			newCell.setObjectiveFunction();
			
			this.vnd.run(this.random, newCell, getTotalTime(), getInitialTime());
			
			if(newCell.getObjectiveFunction() < cell.getObjectiveFunction()) {
				
				solucoes.set(0, newCell);			
				if(newCell.getObjectiveFunction() < best.getObjectiveFunction()) {
					best = cell;
				}
				
				IT_S = 0;
				
			} else{
				
				IT_S++;
				
			}
			
		}
		
		return solucoes;
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
