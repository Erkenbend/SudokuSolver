import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class Grid {

	private Cell[][] content;
	private static Stack<Operation> stack = new Stack<Operation>();
	public static int nbPasses = 0;

	public Grid() {
		this.content = new Cell[9][9];
	}

	public Grid(Cell[][] content) {
		if (content.length != 9 && content[0].length != 9) {
			System.err.println("Cannot create grid : invalid dimensions");
			System.exit(1);
		} else {
			this.content = content;
		}
	}

	public Grid(int[][] content) {
		if (content.length != 9 && content[0].length != 9) {
			System.err.println("Cannot create grid : invalid dimensions");
			System.exit(1);
		} else {
			this.content = new Cell[9][9];
			for (int i = 0; i < 9; i++)
				for (int j = 0; j < 9; j++)
					this.content[i][j] = new Cell(content[i][j]);
		}
	}
	
	public Grid(Grid other) {
		Cell[][] content = new Cell[9][9];
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				content[i][j] = new Cell(other.content[i][j]);
			}
		}
		this.content = content;
	}
	
	public void restoreOldState(Grid old) {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				this.content[i][j] = new Cell(old.content[i][j]);
			}
		}
	}

	@Override
	public String toString() {
		String res = "";
		Cell[][] content = this.content;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				int val = content[i][j].getVal();
				if (val == 0) {
					res += " "; // empty spot
				} else {
					res += val;
				}
				if ((j + 1) % 3 == 0) {
					res += "  "; // vertical sep
				}
			}
			res += "\n";
			if ((i + 1) % 3 == 0) {
				res += "\n"; // horizontal sep
			}
		}
		return res;
	}
	
	public int getFirstFreeCell() {
		Cell[][] content = this.content;
		for (int i=0; i<9; i++) {
			for (int j=0; j<9; j++) {
				if (content[i][j].getVal() == 0) {
					return 10*i+j;
				}
			}
		}
		return -1;
	}
	
	public int getCellToGuess() {
		Cell[][] content = this.content;
		int minNbCandidates = 10;
		int cellCoordinates = -1;
		int size;
		for (int i=0; i<9; i++) {
			for (int j=0; j<9; j++) {
				if (content[i][j].getVal() == 0) {
					size = content[i][j].getCandidates().size();
					if (size < minNbCandidates) {
						cellCoordinates = 10*i+j;
					}
				}
			}
		}
		return cellCoordinates;
	}
	
	public boolean isCompleted() {
		Cell[][] content = this.content;
		if (this.getFirstFreeCell() != -1) {
			return false;
		}
		//checking lines
		for (int i=0; i<9; i++) {
			CheckList cl = new CheckList();
			for (int j=0; j<9; j++) {
				cl.setPresent(content[i][j].getVal());
			}
			if (!cl.isEveryoneThere()) {
				return false;
			}
		}
		//checking lines
		for (int j=0; j<9; j++) {
			CheckList cl = new CheckList();
			for (int i=0; i<9; i++) {
				cl.setPresent(content[i][j].getVal());
			}
			if (!cl.isEveryoneThere()) {
				return false;
			}
		}
		//checking blocks
		for (int k=0; k<3; k++) {
			for (int l=0; l<3; l++) {
				CheckList cl = new CheckList();
				for (int i=0; i<3; i++) {
					for (int j=0; j<3; j++) {
						cl.setPresent(content[3*k+i][3*l+j].getVal());
					}
				}
				if (!cl.isEveryoneThere()) {
					return false;
				}
			}
		}
		return true;
	}

	public void findCandidates(int line, int col) throws SudokuContradictionException {
		Cell[][] content = this.content; // caching
		Cell c = content[line][col];
		if (c.getVal() != 0) {
			return;
		} else {
			
//			System.out.println("cell "+(line+1)+" "+(col+1));
			
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			for (int k = 1; k < 10; k++)
				candidates.add(new Integer(k));
			c.setCandidates(candidates);
			
			// checking line
			for (int j = 0; j < 9; j++) {
				candidates.remove((Object) content[line][j].getVal());
			}

			// checking column
			for (int i = 0; i < 9; i++) {
				candidates.remove((Object) content[i][col].getVal());
			}

			// checking block
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					candidates.remove((Object) content[i + 3 * (line / 3)][j
							+ 3 * (col / 3)].getVal());
				}
			}
			
//			for (Integer k : candidates)
//				System.out.print(k);
//			System.out.print("\n");

			if (candidates.size() == 0) {
				throw new SudokuContradictionException(line, col);
			}
			c.setCandidates(candidates);

		}
	}

	public boolean convertLoneCandidate(int i, int j) {
		Cell[][] content = this.content; // caching
		if (content[i][j].getCandidates().size() == 1) {
			// System.out.println("LC> cell "+(i+1)+" "+(j+1)+" : writing "+content[i][j].getCandidates().get(0));
			content[i][j].writeValue();
			return true;
		} else {
			return false;
		}
	}

	public boolean convertLine(int i) {
		Cell[][] content = this.content; // caching
		boolean hasChanged = false;
		for (int testedVal = 1; testedVal < 10; testedVal++) {
			ArrayList<Integer> colsFound = new ArrayList<Integer>();
			for (int j=0; j<9; j++) {
				Cell c = content[i][j];
				if (c.getVal() == testedVal || c.getCandidates().contains(new Integer(testedVal))) {
					colsFound.add(new Integer(j));
				}
				if (colsFound.size()>=2) {
					break;
				}
			}
			if (colsFound.size() == 1 && content[i][colsFound.get(0)].getVal() == 0) {
				// System.out.println("CL> cell "+(i+1)+" "+(colsFound.get(0)+1)+" : writing "+testedVal);
				content[i][colsFound.get(0)].writeValue(testedVal);
				hasChanged = true;
			}
		}
		return hasChanged;
	}

	public boolean convertCol(int j) {
		Cell[][] content = this.content; // caching
		boolean hasChanged = false;
		for (int testedVal = 1; testedVal < 10; testedVal++) {
			ArrayList<Integer> linesFound = new ArrayList<Integer>();
			for (int i=0; i<9; i++) {
				Cell c = content[i][j];
				if (c.getVal() == testedVal || c.getCandidates().contains(new Integer(testedVal))) {
					linesFound.add(new Integer(i));
				}
				if (linesFound.size()>=2) {
					break;
				}
			}
			if (linesFound.size() == 1 && content[linesFound.get(0)][j].getVal() == 0) {
				content[linesFound.get(0)][j].writeValue(testedVal);
				hasChanged = true;
			}
		}
		return hasChanged;
	}

	public boolean convertBlock(int k, int l) {
		Cell[][] content = this.content; // caching
		boolean hasChanged = false;
		for (int testedVal = 1; testedVal < 10; testedVal++) {
			ArrayList<Integer> cellsFound = new ArrayList<Integer>();
			for (int i=0; i<3; i++) {
				for (int j=0; j<3; j++) {
					Cell c = content[3 * k + i][3 * l + j];
					if (c.getVal() == testedVal || c.getCandidates().contains(new Integer(testedVal))) {
						cellsFound.add(new Integer(10*i+j));
					}
					if (cellsFound.size()>=2) {
						break;
					}
				}
			}
			if (cellsFound.size() == 1 && content[3*k+cellsFound.get(0)/10][3*l+cellsFound.get(0)%10].getVal() == 0) {
				content[3*k+cellsFound.get(0)/10][3*l+cellsFound.get(0)%10].writeValue(testedVal);
				hasChanged = true;
			}
		}
		return hasChanged;
	}
	
	public void completeAsMuchAsPossible() throws SudokuContradictionException {
		boolean evolving = true;
		while (evolving) {
			evolving = false;

			// 1) determining candidates for each cell
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					this.findCandidates(i, j);
				}
			}

			// 2) only one candidate for the cell ? it's the one
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					// System.out.println((i+1)+" "+(j+1)+" "+this.content[i][j].getCandidates().size());
					evolving = this.convertLoneCandidate(i, j) || evolving;
				}
			}

			// 3) only one spot in a line/col/block for a value ? it's the one
			for (int i = 0; i < 9; i++) {
				evolving = this.convertLine(i) || evolving;
			}
			for (int j = 0; j < 9; j++) {
				evolving = this.convertCol(j) || evolving;
			}
			for (int k = 0; k < 3; k++) {
				for (int l = 0; l < 3; l++) {
					evolving = this.convertBlock(k, l) || evolving;
				}
			}
			
			// System.out.println("-------------\n\n" + this);
		}
	}
	
	public void backtrakingSolve() {
		// System.out.println(stack);
		try {
			this.completeAsMuchAsPossible();
			//System.out.println("Got until here");
			//System.out.println(this);
			if (this.isCompleted()) {
				System.out.println("Grid completed !\n");
				System.out.print(this);
				System.out.println(nbPasses+" guesses needed");
				System.exit(0);
			} else {
				int nextFreeCellCoordinates = this.getFirstFreeCell();
				//int nextFreeCellCoordinates = this.getCellToGuess();
				if (nextFreeCellCoordinates != -1) {
					Cell nextFreeCell = this.content[nextFreeCellCoordinates/10][nextFreeCellCoordinates%10];
					ArrayList<Integer> candidatesList = new ArrayList<Integer>(nextFreeCell.getCandidates());
					for (int v : candidatesList) {
						this.content[nextFreeCellCoordinates/10][nextFreeCellCoordinates%10].writeValue(v);
						stack.push(new Operation(nextFreeCellCoordinates/10, nextFreeCellCoordinates%10, v, new Grid(this)));
						nbPasses++;
						this.backtrakingSolve();
					}
				}
				try {
					Operation lastOp = stack.pop();
					this.restoreOldState(lastOp.getStateSaved());
				} catch (EmptyStackException ese) {
					System.err.println("Grid invalid : got stuck with empty stack");
				}
			}
		} catch (SudokuContradictionException sce) {
			// sce.printStackTrace();
			// System.out.println(this);
			try {
				Operation lastOp = stack.pop();
				this.restoreOldState(lastOp.getStateSaved());
			} catch (EmptyStackException ese) {
				System.err.println("Grid invalid : got stuck with empty stack");
			}
		}
	}

}
