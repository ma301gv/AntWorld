package lab2017;

public class Location {

	int row, col;

	Location(int r, int c) {

		row = r;
		col = c;
	}
	
	Location(Location rc){
		
		row = rc.row;
		col = rc.col;
	}
	
	public String toString(){
		
		return "[" + row + ", " + col + "]";
	}
	
	public boolean equals(Location rc){
		
		return row == rc.row && col == rc.col;
	}
}

