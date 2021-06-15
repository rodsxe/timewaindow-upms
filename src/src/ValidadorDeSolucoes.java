import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;
import model.Solution;
import model.Job;


public class ValidadorDeSolucoes {
	
	private int[][][] custoDeSetup;
	private int[][] custos;
	private Solution solucao;
	private ObjectFunctionEvaluator evaluator;
	
	public ValidadorDeSolucoes(Solution solucao, ObjectFunctionEvaluator evaluator, int[][] custos, int[][][] custoDeSetup) {
		super();
		this.solucao = solucao;
		this.custos = custos;
		this.custoDeSetup = custoDeSetup;
		this.evaluator = evaluator;
	}
	
	
	public void writeSolucaoInArq(String arq, String instance, String FILE_TYPE){
		try {
			
			solucao.setObjectiveFunction();
			
			String instanciaName = instance.substring(0, instance.indexOf(FILE_TYPE));
			
			FileWriter fileWriter = new FileWriter(arq, false);
			PrintWriter printWriter = new  	PrintWriter(fileWriter);
			printWriter.println(instanciaName + "\t" + custos[0].length + "\t" + custos.length + "\t" + this.solucao.getObjectiveFunction());
			List<Job> jobs = new ArrayList<Job>();
			MachineContainer[] maquinas = this.solucao.getMaquinas();
			for (int i = 0; i < maquinas.length; i++) {
				MachineContainer maquina = maquinas[i];
				printWriter.print(evaluator.getObjectFunctionValue(this.solucao, maquina) + "\t");
				int numTarefas = maquina.getNumberOfJobs();
				printWriter.print(numTarefas + "\t");
				for (int j = 0; j < numTarefas; j++) {
					printWriter.print((maquina.getJob(j).getId() + 1) + "\t");
					jobs.add(maquina.getJob(j));
				}
				printWriter.println();
			}
			
			jobs.sort((o1, o2)->o1.getId()-o2.getId());	
			printWriter.print("Job" + "\t");
			printWriter.print("Cj" + "\t");
			printWriter.print("Ej" + "\t");
			printWriter.print("Tj" + "\t");
			printWriter.println();
			for (Job job : jobs) {
				
				printWriter.print((job.getId() + 1) + "\t");
				printWriter.print((job.getCompletionTime()) + "\t");
				printWriter.print((job.getEarliness()) + "\t");
				printWriter.print((job.getTardiness()) + "\t");
				printWriter.println();
				
			}
			
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
