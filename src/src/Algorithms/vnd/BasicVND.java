package Algorithms.vnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Solution;
import operators.localsearch.LocalSearch;

public class BasicVND implements VND{

	private List<LocalSearch> seekers = new ArrayList<LocalSearch>();
	
	public void addLocalSearch(LocalSearch localSearch) { 
		seekers.add(localSearch);
	}
	
	public boolean run(Random rand, Solution s, long totalTime, long initialTime){
		
		boolean improvement = false;
		List<LocalSearch> removedLSOp = new ArrayList<LocalSearch>();
		while (!seekers.isEmpty()) {
			
			LocalSearch ls = seekers.remove(0);
			removedLSOp.add(ls);
						
			if (ls.run(rand, s, totalTime, initialTime)) {
				
				seekers.addAll(0, removedLSOp);
				removedLSOp.clear();
				improvement = true;
				
			} 
			
		}
		
		seekers.addAll(removedLSOp);
		s.setObjectiveFunction();
		
		return improvement;
	}
	
	
}
