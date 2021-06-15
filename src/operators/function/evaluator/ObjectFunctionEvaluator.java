package operators.function.evaluator;

import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import model.MachineContainer;
import model.base.Container;

public interface ObjectFunctionEvaluator {
	
	public float getObjectFunctionValue(Solution cell);
	
	public float getObjectFunctionValue(Solution cell, Container container);
	
	public void setSchedulingConfig(SchedulingInstancesConfig schedulingConfig);
	
	public SchedulingInstancesConfig getSchedulingConfig();
		
	public boolean isValidCell(Solution cell);
	
	public void setCONSTRAINT_PENALTY(float cONSTRAINT_PENALTY);
	
	public void writeResult(Solution cell, PrintWriter printWriter);
	
	public int getLSMachineIndex(Random rand, Solution cell, List<MachineContainer> machines);
	
	public float getLSCost(float costM1, float costM2);
	
	public void resetNumberOfObjectFunctionAval();
	
	public long getNumberOfObjectFunctionAval();
	
}
