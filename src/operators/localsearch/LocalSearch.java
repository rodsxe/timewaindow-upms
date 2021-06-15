package operators.localsearch;
import java.util.Random;

import model.Solution;

public interface LocalSearch {
	
	public boolean run(Random rand, Solution s, long totalTime, long initialTime);
	
}
