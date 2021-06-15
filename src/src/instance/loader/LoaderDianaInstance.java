package instance.loader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.function.evaluator.scheduling.EarliestTardinessEvaluator;

public class LoaderDianaInstance implements LoaderInstance {
	
	private static final EarliestTardinessEvaluator evaluator = new EarliestTardinessEvaluator();
	
	public ObjectFunctionEvaluator getEvaluator(String arqPath) {
		
		return evaluator;
		
	}
	
	@Override
	public int[][] loadDueDate(String arqPath) throws FileNotFoundException, IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(arqPath));
        String line = in.readLine();
        StringTokenizer st = new StringTokenizer(line, " ");
        Integer n = new Integer(st.nextToken());
        Integer m = new Integer(st.nextToken());
        
        int[][] dueDates = new int[n][6];
        
        int cont = 0;
        while (cont < n + 7 + m + n * m) {cont++; in.readLine();}
        
        in.readLine();
        pressDueDate(in, LoaderInstance.DUE_DATE, dueDates);
        in.readLine();
        pressDueDate(in, LoaderInstance.DUE_DATE_EARLY, dueDates);
        in.readLine();
        pressDueDate(in, LoaderInstance.TARDINESS_WEIGHT, dueDates);
        in.readLine();
        pressDueDate(in, LoaderInstance.EARLINESS_WEIGHT, dueDates);
        
        for (int i = 0; i < n; i++) {dueDates[i][LoaderInstance.IS_RESTRICT] = 0;}
    	for (int i = 0; i < n; i++) {dueDates[i][LoaderInstance.RELEASE_DATE] = 0;}
    	
    	return dueDates;
		
	}
	
	public int[][] loadDueDate2(String arqPath) throws FileNotFoundException, IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(arqPath));
        String line = in.readLine();
        StringTokenizer st = new StringTokenizer(line, " ");
        Integer n = new Integer(st.nextToken());
        Integer m = new Integer(st.nextToken());
        
        int[][] dueDates = new int[n][6];
        
        int cont = 0;
        while (cont < n + 7 + m + n * m) {cont++; in.readLine();}
        
        in.readLine();
        pressDueDate(in, LoaderInstance.DUE_DATE, dueDates);
        in.readLine();
        pressDueDate(in, LoaderInstance.DUE_DATE_EARLY, dueDates);
        in.readLine();
        pressDueDate(in, LoaderInstance.TARDINESS_WEIGHT, dueDates);
        in.readLine();
        pressDueDate(in, LoaderInstance.EARLINESS_WEIGHT, dueDates);
        
         
        for (int i = 0; i < n; i++) {dueDates[i][LoaderInstance.IS_RESTRICT] = 0;}
    	for (int i = 0; i < n; i++) {dueDates[i][LoaderInstance.RELEASE_DATE] = 0;}
    	
    	return dueDates;
		
	}

	private void pressDueDate(BufferedReader in, int j, int[][] dueDates) throws IOException {
		
		StringTokenizer st;
		int i = 0;
        st = new StringTokenizer(in.readLine(), " ");
        while (st.hasMoreElements()) {
			String valueAttribute = ((String)st.nextToken());
			dueDates[i][j] = new Integer(valueAttribute);
			i++;
		}
		
	}

	@Override
	public int[][] loadProcessTime(String arqPath) throws FileNotFoundException, IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(arqPath));
        String line = in.readLine();
        StringTokenizer st = new StringTokenizer(line, " ");
        Integer n = new Integer(st.nextToken());
        Integer m = new Integer(st.nextToken());
        
        int[][] custos = new int[m][n];
        int contTarefas = 0;
        while (contTarefas < 3) {contTarefas++; in.readLine();}
        contTarefas = 0;
        
        while (contTarefas < n) {
        	
            st = new StringTokenizer(in.readLine(), " ");
            int contMaquinas = 0;
            while (st.hasMoreElements()){
                String valueAttribute = ((String)st.nextToken());
                custos[contMaquinas][contTarefas] = new Integer(valueAttribute);
                contMaquinas++;
            }
            contTarefas++;
            
        }
        in.close();
        return custos;
    	
	}

	@Override
	public int[][][] loadSetupTime(String arqPath) throws FileNotFoundException, IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(arqPath));
        String line = in.readLine();
        StringTokenizer st = new StringTokenizer(line, " ");
        
        Integer n = new Integer(st.nextToken());
        Integer m = new Integer(st.nextToken());
        
        int contTarefas = 0;
        int maquina = 0;
        int contTarefas2 = 0;
        int[][][] custoDeSetup = new int[m][n][n];
        
        while (contTarefas < n + 7) {contTarefas++; in.readLine();}
                
        while (maquina < m) {
        	
        	if (contTarefas2 >= n) {
        		contTarefas2 = 0;
    			maquina ++;	
    			in.readLine();
        	} else {
        		st = new StringTokenizer(in.readLine(), " ");
                contTarefas = 0;
        		while (st.hasMoreElements()) {
					String valueAttribute = ((String)st.nextToken());
					custoDeSetup[maquina][contTarefas2][contTarefas] = new Integer(valueAttribute);
					contTarefas ++;
				}
				contTarefas2 ++;
        	}
        	
        	
        }
        in.close();
        
        return custoDeSetup;
	}

	@Override
	public HashMap<String, BestSol> loadBestSol(String arqPath)
			throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
