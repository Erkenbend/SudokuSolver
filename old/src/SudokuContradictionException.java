
public class SudokuContradictionException extends Exception {
	
	private int line;
	private int col;
	
	public SudokuContradictionException(int line, int col) {
		this.line = line;
		this.col = col;
	}
	
	@Override
	public void printStackTrace() {
		System.err.println("Contradiction in cell "+(line+1)+" "+(col+1));
	}
	
}
