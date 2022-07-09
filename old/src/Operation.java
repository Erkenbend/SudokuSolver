
public class Operation {
	
	private int line;
	private int col;
	private int val;
	private Grid stateSaved;
	
	public Operation(int line, int col, int val, Grid stateSaved) {
		this.line = line;
		this.col = col;
		this.val = val;
		this.stateSaved = stateSaved;
	}

	public int getLine() {
		return line;
	}

	public int getCol() {
		return col;
	}
	
	public Grid getStateSaved() {
		return stateSaved;
	}
	
	public String toString() {
		return ">"+this.line+" "+this.col+" - "+this.val;
	}
	
}
