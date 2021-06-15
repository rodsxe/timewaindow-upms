package model;

public class Arresta {
	
	private int i;
	private int j;
	private int k;
	private int n;
	
	public Arresta(int i, int j, int k) {
		super();
		this.i = i;
		this.j = j;
		this.k = k;
		this.n = 1;
	}
	
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	public int getJ() {
		return j;
	}
	public void setJ(int j) {
		this.j = j;
	}
	public int getK() {
		return k;
	}
	public void setK(int k) {
		this.k = k;
	}
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	
	public boolean equals(Arresta obj) {
		if(i == obj.getI() && j == obj.getJ() && k == obj.getK())return true;
		return false;
	}

	public void addN(){
		this.n ++;
	}
}	
