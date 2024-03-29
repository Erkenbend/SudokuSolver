package oldmodernized;

import java.util.ArrayList;


public class Cell {

	private Integer val;
	private ArrayList<Integer> candidates;
	
	public Cell(Integer val) {
		this.val = val;
		this.candidates = new ArrayList<>();
	}
	
	public Cell(Cell other) {
		this.val = other.val;
		this.candidates = new ArrayList<>(other.candidates);
	}
	
	public int getVal() {
		return val;
	}

	public ArrayList<Integer> getCandidates() {
		return candidates;
	}

	public void setCandidates(ArrayList<Integer> candidates) {
		this.candidates = candidates;
	}

	public void writeValue() {
		if (this.candidates.size() != 1) {
			System.err.println("Cannot write from candidates : need exactly one candidate");
			System.exit(1);
		} else {
			this.val = candidates.get(0);
			candidates.clear();
		}
	}
	
	public void writeValue(int val) {
		this.val = val;
		candidates.clear();
	}
}
