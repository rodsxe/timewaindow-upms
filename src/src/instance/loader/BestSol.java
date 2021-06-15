package instance.loader;

public class BestSol {
	
	private String name;
	private int maquinas;
	private int tarefas;
	private int bestSol;
	private int setup;
	
	public BestSol(String name, int maquinas, int tarefas, int bestSol, int setup) {
		super();
		this.name = name;
		this.maquinas = maquinas;
		this.tarefas = tarefas;
		this.bestSol = bestSol;
		this.setup = setup;
	}
	
	public BestSol() {
		super();
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getMaquinas() {
		return maquinas;
	}
	public void setMaquinas(int maquinas) {
		this.maquinas = maquinas;
	}
	public int getTarefas() {
		return tarefas;
	}
	public void setTarefas(int tarefas) {
		this.tarefas = tarefas;
	}
	public int getBestSol() {
		return bestSol;
	}
	public BestSol setBestSol(int bestSol) {
		this.bestSol = bestSol;
		return this;
	}
	public int getSetup() {
		return setup;
	}
	public void setSetup(int setup) {
		this.setup = setup;
	}
	
	
	
}
