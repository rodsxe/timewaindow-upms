import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import Algorithms.Algorithm;
import exception.BestObjectFunctionException;
import instance.loader.BestSol;
import instance.loader.LoaderInstance;
import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class ExperimentRunner {
	
	/*Constantes para criterio de parada*/
	public static final int STOPPPING_CRITERIA_ITERATIONS_WITHOUT_IMPROVEMENT = 1;
	public static final int STOPPPING_CRITERIA_TIME = 2;
	public static final int STOPPPING_CRITERIA_TARGET = 3;
	public static final int STOPPPING_CRITERIA_ITERATIONS_WITHOUT_IMPROVEMENT_OR_MAX_NEF = 4;
	
	public static final int EXECUTION_TYPE_STANDARD = 1;
	
	private int[][] custos;
	private int[][][] custoDeSetup;
	
	/*Melhor valor conhecido para as inst�ncias*/
	private HashMap<String, BestSol> bestSolutions;
	
	private Random rand;
	private int criterioDeParada;
	private int tipo_execucao;
	private Algorithm algoritmo;
	private LoaderInstance loader;
	private ObjectFunctionEvaluator evaluator;	
	
	/*Caminhos de arquivos*/
	private String mainDir;
	private String writeSolDir;
	private String resultArqName;
	private String[] fileDir;
	private String FILE_TYPE;
	
	private int RUNS_NUMBER;
	
	private double PERCENTUAL_ALVO;
	private int IT_MAX;
	private int[] millis;
	private long number_of_eval;
	private long MAX_NFE;
	
	public ExperimentRunner() {
		
		super();
		this.algoritmo = ExperimentConfig.algorithm;
		this.loader = ExperimentConfig.loader;
		this.rand = ExperimentConfig.rand;
		this.criterioDeParada = ExperimentConfig.stopping_criteria;
		this.mainDir = ExperimentConfig.main_dir;
		this.writeSolDir = ExperimentConfig.dir_to_write_the_best_solutions;
		this.resultArqName = ExperimentConfig.result_file_name;
		this.fileDir = ExperimentConfig.dir_instances;
		this.FILE_TYPE = ExperimentConfig.file_instance_type;
		this.RUNS_NUMBER = ExperimentConfig.number_of_experiments_per_instance;
		this.PERCENTUAL_ALVO = ExperimentConfig.percentual_alvo;
		this.IT_MAX = ExperimentConfig.iterations_without_improvement;
		this.millis = ExperimentConfig.time_millis;
		this.tipo_execucao = ExperimentConfig.execution_type;
		this.number_of_eval = ExperimentConfig.max_number_of_eval;
		
	}
	
	public ExperimentRunner(Random rand) {
		
		super();
		this.rand = rand;
		this.algoritmo = ExperimentConfig.algorithm;
		this.loader = ExperimentConfig.loader;
		this.criterioDeParada = ExperimentConfig.stopping_criteria;
		this.mainDir = ExperimentConfig.main_dir;
		this.writeSolDir = ExperimentConfig.dir_to_write_the_best_solutions;
		this.resultArqName = ExperimentConfig.result_file_name;
		this.fileDir = ExperimentConfig.dir_instances;
		this.FILE_TYPE = ExperimentConfig.file_instance_type;
		this.RUNS_NUMBER = ExperimentConfig.number_of_experiments_per_instance;
		this.PERCENTUAL_ALVO = ExperimentConfig.percentual_alvo;
		this.IT_MAX = ExperimentConfig.iterations_without_improvement;
		this.millis = ExperimentConfig.time_millis;
		this.tipo_execucao = ExperimentConfig.execution_type;
		this.number_of_eval = ExperimentConfig.max_number_of_eval;
		
	}

	private void loadArq(String path, HashMap<String, String> params){
		
		try{
			
			custos = loader.loadProcessTime(path);
			custoDeSetup = loader.loadSetupTime(path);
			SchedulingInstancesConfig config = new SchedulingInstancesConfig(custoDeSetup, custos, loader.loadDueDate(path));
			this.evaluator = loader.getEvaluator(path);
			this.evaluator.setSchedulingConfig(config);
			algoritmo.setInstanceValues(config,
										evaluator,
										rand, 
										params);
			
        }
        catch(FileNotFoundException e){
        	e.printStackTrace();
            System.out.println("Arquivo n�o encontrado.");
        }
        catch(IOException o){
        	o.printStackTrace();
            System.out.println("ERRO");
        }
	}
	
	private long getProcessTime(int milli) {
		//long processTime = custos.length * (custos[0].length / 2) * milli;
		long processTime = ((custos[0].length + custos.length - 1) / (custos.length)) * milli;
		//processTime = (custos[0].length * custos.length ) * milli;
		
		//processTime = 6000000;
		return processTime;
	}
	
	private void setMaxEval() {
		
		this.MAX_NFE = ((custos[0].length + custos.length - 1) / (custos.length - 1)) * this.number_of_eval;
		//System.out.println(processingCosts[0].length + ":" + processingCosts.length);	
	}
	
	private boolean criterioDeParada(int itSemMelhora, long processTime, long tempoTotal, long alvo, float cost, long nEF){
		
		if(criterioDeParada == STOPPPING_CRITERIA_TIME)
			return (tempoTotal > processTime)?false:true;
		else if(criterioDeParada == STOPPPING_CRITERIA_TARGET)
			return (cost > alvo && tempoTotal > processTime)?false:true;
		else if(criterioDeParada == STOPPPING_CRITERIA_ITERATIONS_WITHOUT_IMPROVEMENT)
			return (itSemMelhora < IT_MAX)?false:true;
		else if(criterioDeParada == STOPPPING_CRITERIA_ITERATIONS_WITHOUT_IMPROVEMENT_OR_MAX_NEF) {
			return (itSemMelhora < IT_MAX && nEF < MAX_NFE)?false:true;
			//return (nEF < MAX_NFE)?false:true;
		}
		
		return true;
		
	}
	
	public Long getAlvo(String instancia){
		
		String instanciaName = instancia.substring(0, instancia.indexOf(FILE_TYPE));
		if (bestSolutions == null) return new Long(0);
		BestSol bestSol = bestSolutions.get(instanciaName);
		return (long)(bestSol.getBestSol() * PERCENTUAL_ALVO);
	}
	
	public Solution getBestValidCell(List<Solution> cells){
		
		cells = sortObjFunction(cells);
		for (Solution cell : cells) {
			if(evaluator.isValidCell(cell)) return cell;
		}
		
		return null;
		
	}
	
	public Solution execOneTime(String arqPath, String instancia, PrintWriter printWriter, HashMap<String, String> params) throws BestObjectFunctionException, IOException, ParseException{
		
		List<Float> timeSolutions = new ArrayList<Float>();
		this.loadArq(arqPath, params);
		long tempoTotal = getProcessTime(millis[millis.length - 1]);
		this.setMaxEval();
		this.evaluator.resetNumberOfObjectFunctionAval();
		float lastChangeMakespan = Float.MAX_VALUE;
		long alvo = (instancia != null)? getAlvo(instancia): 0;
		int itSemMelhora = 0;
		int it = 0;
		long tempoInicial = System.currentTimeMillis();
		long processTime = System.currentTimeMillis() - tempoInicial;
		long tempoAlvo = -1;
		
		List<Solution> solucoes = algoritmo.initialSolution();
		Solution bestSol = getBestValidCell(solucoes);
		if (printWriter != null) writeResultInstance(printWriter, instancia, bestSol.getObjectiveFunction(), System.currentTimeMillis() - tempoInicial, this.evaluator.getNumberOfObjectFunctionAval(), "initial");
		//System.out.println("------BEGIN-------");
				
		while(!criterioDeParada(itSemMelhora, processTime, tempoTotal, alvo, lastChangeMakespan, this.evaluator.getNumberOfObjectFunctionAval())){
			
			solucoes = algoritmo.execIteration(solucoes);
			
			bestSol = getBestValidCell(solucoes);
			Solution bestSol2 = algoritmo.getBest();
			if (bestSol == null && bestSol2 != null) bestSol = bestSol2;
			else if(bestSol == null && bestSol2 == null) bestSol = null;
			else if(bestSol2 != null && bestSol!= null) bestSol = (bestSol.getObjectiveFunction() < bestSol2.getObjectiveFunction())?bestSol:bestSol2;
			
			processTime = System.currentTimeMillis() - tempoInicial;
			
			
			if(bestSol == null || lastChangeMakespan <= bestSol.getObjectiveFunction()){
				
				itSemMelhora++;
				if (bestSol != null && bestSol.getObjectiveFunction() == 0) break;
				System.out.println("itSemMelhora:" + itSemMelhora);
				
			}
			else{
				
				lastChangeMakespan = bestSol.getObjectiveFunction();
				System.out.println("v:" + lastChangeMakespan);
				System.out.println("it:" + it);

				itSemMelhora = 0;
				if (printWriter != null) writeResultInstance(printWriter, instancia, bestSol.getObjectiveFunction(), processTime, this.evaluator.getNumberOfObjectFunctionAval(), "middle");
				
			}		
						
			
			if (bestSol != null && tempoAlvo < 0 && bestSol.getObjectiveFunction() < alvo) tempoAlvo = processTime;
			
			++it;
			
			//printWriter.print(it+";" + bestSol.getObjectiveFunction() + ";" + processTime + ";");
			//System.out.println("it:" + it);

		}
		//System.out.println("------END-------");
		if (printWriter != null) writeResultInstance(printWriter, instancia, algoritmo.getBest().getObjectiveFunction(), processTime, this.evaluator.getNumberOfObjectFunctionAval(), "final");
				
		processTime = System.currentTimeMillis() - tempoInicial;
		System.out.println("Tempo:" + (processTime/1000));
		System.out.println("ObjF:" + (lastChangeMakespan));
		
		

		if (tempoAlvo < 0) tempoAlvo = Integer.MAX_VALUE;
		
		//System.out.println(it);
		
		solucoes = algoritmo.updateMemory(solucoes);
		
		if (solucoes.isEmpty()) return null;
				
		Solution result = solucoes.get(0);
		
		if(lastChangeMakespan != result.getObjectiveFunction()) throw new BestObjectFunctionException("Bug in algorithm, best object function return in memory (updateMemory) is not the same showed in rum time. Run Time:"+ lastChangeMakespan + "; Memory:" +  result.getObjectiveFunction());
		
		timeSolutions.clear();
				
		result.setTempo(processTime);
		
		if(criterioDeParada == STOPPPING_CRITERIA_ITERATIONS_WITHOUT_IMPROVEMENT)timeSolutions.add(lastChangeMakespan);
		else if(criterioDeParada == STOPPPING_CRITERIA_TARGET && lastChangeMakespan <= alvo){
			timeSolutions.add(lastChangeMakespan);
		}
		
		result.setTimeSolutions(timeSolutions);
		timeSolutions.clear();
		
		/*if (printWriter != null){
			if(criterioDeParada != CRITERIO_DE_PARADA_ALVO)writeResultInstance(instancia, printWriter, result.getTimeSolutions(), solucoes, tempoAlvo);
			else writeTargetResultInstance(instancia, printWriter, result);
		}*/
		
		return result;
	}
	
	public void run()throws Exception{
		
		switch(tipo_execucao) {
			
		case EXECUTION_TYPE_STANDARD:
			
			runStandard();
			break;

		}
		
	}
	
	public void runStandard() throws Exception{
		
		for (int j = 0; j < fileDir.length; j++) {
			String dirName = mainDir + fileDir[j];
			
			try {
				
				FileWriter fileWriter = new FileWriter(mainDir + resultArqName + "_" + fileDir[j] + ".txt", true);
				PrintWriter printWriter = new PrintWriter(fileWriter);
				//Boolean pontoDeParada = false;
				
				ArrayList<String> instancias = loadInstancesNames(dirName); 
				for (String instancia : instancias) {
					
					//System.out.println(instancia);
					//if (!pontoDeParada && !instancia.contains("A224")) continue;
					
					//pontoDeParada = true;
					
					float melhorCusto = 100000000;
					float media = 0;
					
					Solution solucao = null;
					int it = 0;
					
					while(it < RUNS_NUMBER){
						
						Solution s = execOneTime(dirName + "/" + instancia, fileDir[j] + "_" +instancia, printWriter, null);
						float custo = s.getObjectiveFunction();
						
						if(custo < melhorCusto){
							
							melhorCusto = custo;
							solucao = s;
						
						}
						media = media + custo;
						
						it ++;
					}
				
					if(criterioDeParada != STOPPPING_CRITERIA_TARGET){
						ValidadorDeSolucoes vs = new ValidadorDeSolucoes(solucao, evaluator, custos, custoDeSetup);
						vs.writeSolucaoInArq(mainDir + "/" + writeSolDir + "/" + fileDir[j] + "/" + instancia, instancia, FILE_TYPE);
					}
				
				}
				
				printWriter.close();
				fileWriter.close();
				
			} catch (IOException e) { e.printStackTrace();}
		
		}
		
	}
	
	
	
	
	private List<Solution> sortObjFunction(List<Solution> Ab){
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
	
	private float get_distance(List<Solution> memoria, int i){
		
		float menor = 1000000000;
		Solution s2 = memoria.get(i);
		
		for (int j = 0; j < memoria.size(); j++) {
			
			if (j != i){
				
				Solution s1 = memoria.get(j);
				float distancia = s1.calcularDistanciaArrestas(s2);
				if(distancia < menor)menor = distancia;
			
			}
		}
		
		return menor;
	}
	
	public Pair<Float, Float> getAvgDistance(List<Solution> memory, int N) {
		
		float avg = 0, var = 0;
		List<Solution> list = new ArrayList<Solution>();
		float[] distances = new float[N];
		
		for (int i = 0; i < N; i++) list.add(memory.get(i));
				
		for (int i = 0; i < N; i++) {
			
			distances[i] = get_distance(list, i);
			avg += distances[i];
		
		}
		
		avg = avg/(float)N;
		
		for (int i = 0; i < distances.length; i++) {
			
			var += Math.pow(avg - distances[i], 2);
			
		}
		
		var = var/(float)(N - 1);
		
		return new Pair<Float, Float>(avg, var);
		
	}
	
	public float getAvgMakespan(List<Solution> memory, int N) {
		
		float result = 0;
		
		for (int i = 0; i < N; i++) {
			
			result += memory.get(i).getObjectiveFunction();
			
		}
		
		return result/(float)N;
	}
	
	private void writeResultInstance(PrintWriter printWriter, String instancia, float cost, long time, long nFE, String type) throws ParseException {
		
		String instanciaName = instancia.substring(0, instancia.indexOf(FILE_TYPE));
				
		printWriter.println(instanciaName + ";" + time + ";" + cost+";" + nFE + ";" + type);
		printWriter.flush();
		
	}
	
	

	/**
	 * Metodo que retorna o camilho de todas as instancias que est�o dentro diret�rio dirName.
	 * @param dirName
	 * @return
	 */
	private ArrayList<String> loadInstancesNames(String dirName) {
		ArrayList<String> instancias = new ArrayList<String>();
		File dir = new File(dirName);  
		
		String[] children = dir.list();  
		if (children == null);
		else {  
		    for (int i=0; i < children.length; i++) {  
		        // Get filename of file or directory  
		        String filename = children[i]; 
		        if(!filename.contains(FILE_TYPE)) continue;
		        instancias.add(filename);
		    }  
		}
		return instancias;
	}
	
		
	public static void main(String[] args) {
		
		if (args.length >= 4){
			
			Random rand = new Random(new Long(args[2]));
			String instance = args[3];
			HashMap<String, String> params = new HashMap<String, String>();
			
			for (int i = 4; i < args.length; i+=2)
				params.put(args[i], args[i + 1]);
						
			ExperimentRunner experiment = new ExperimentRunner(rand);
			try {
				
				Solution s = experiment.execOneTime(instance, null, null, params);
				System.out.println("Best " + ((s == null)? Float.MAX_VALUE : s.getObjectiveFunction()) + "0");
				
			} catch (Exception e) {e.printStackTrace();}
			
		} else {
			
			ExperimentRunner experiment = new ExperimentRunner();
			try {
				experiment.run();
			} catch (Exception e) { e.printStackTrace(); }
			
		}
	}
	
	
}
