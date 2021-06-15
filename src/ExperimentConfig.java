import java.util.Random;

import Algorithms.ABC;
import Algorithms.Algorithm;
import Algorithms.VNSaiNET;
import Algorithms.GA;
import Algorithms.GRASP_ILS;
import Algorithms.IGS;
import Algorithms.PathRelinking;
import instance.loader.LoaderDianaInstance;
import instance.loader.LoaderInstance;

public class ExperimentConfig {
	
	/**The execution type, it may be default, or calibration of the parameters.*/
	public static final int execution_type = ExperimentRunner.EXECUTION_TYPE_STANDARD;
	
	/**Number of experiments will be runned by instance.*/
	public static final int number_of_experiments_per_instance = 5;
	
	/**Type of stopping criteria used*/
	public static final int stopping_criteria = ExperimentRunner.STOPPPING_CRITERIA_ITERATIONS_WITHOUT_IMPROVEMENT_OR_MAX_NEF;
	
	public static final long max_number_of_eval = (long)(0.5*Math.pow(10, 7));
	
	/**if the stoppping criteria is target, the percent of the target used.*/
	public static final double percentual_alvo = 1.00;
	
	/**if used stopping criteria iterations without improvement, 
	 * this parameter represents the number of iterations without improvement.*/
	public static final int iterations_without_improvement = 50;
	
	/**if used stopping criteria of time or target, this parameter represents the maximum time expend in milliseconds.
	 * The time used is given by the equation time_millis * (n + m -1)/(m-1), in which n is the number of jobs and m the number of machines.*/
	public static final int[] time_millis = {2000};
	
	/**The main directory, in which the other files and directories must be subdirectory of it.*/
	public static final String main_dir = "/home/rodney/instances/large/";
	
	/**The list of directories of the instances processed in the experiment. This directories must be subdirectories of main_dir.*/
	public static final String[] dir_instances = {"4m-60n"}; 
	
	/**Directory where the complete solution will be saved. 
	 * This solution is saved with the complete structure of the scheduling, the order of each job and the machine in which their are assigned.*/
	public static final String dir_to_write_the_best_solutions = "sol";
	
	/**The file where is saved only the cpu time and the value of the object function to each instance.*/
	public static final String result_file_name = "grasp_ils";
	
	/**Type of the files to read in dir_instances.*/
	public static final String file_instance_type = ".dat";
	
	/**The random variable used by the experiments.*/
	public static final Random rand = new Random();
	
	/**The class used to load the instances. */
	public static final LoaderInstance loader = new LoaderDianaInstance();
	
	/**The algorithm used*/
	//public static final Algorithm algorithm = new PathRelinking(new GRASP_ILS());
	//public static final Algorithm algorithm = new ABC();
	//public static final Algorithm algorithm = new GA();
	public static final Algorithm algorithm = new VNSaiNET();
	//public static final Algorithm algorithm = new IGS();
	
}


