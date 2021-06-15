package instance.loader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import operators.function.evaluator.ObjectFunctionEvaluator;


public interface LoaderInstance {
	
	public static final int DUE_DATE = 0;
	public static final int IS_RESTRICT = 1;
	public static final int RELEASE_DATE = 2;
	public static final int TARDINESS_WEIGHT = 3;
	public static final int EARLINESS_WEIGHT = 4;
	public static final int DUE_DATE_EARLY = 5;
	
	public int[][] loadDueDate(String arqPath)throws FileNotFoundException, IOException;		
	public int[][] loadProcessTime(String arqPath)throws FileNotFoundException, IOException;
	public int[][][] loadSetupTime(String arqPath)throws FileNotFoundException, IOException;
	public HashMap<String, BestSol> loadBestSol(String arqPath)throws FileNotFoundException, IOException;
	
	public ObjectFunctionEvaluator getEvaluator(String arqPath);
	
}
