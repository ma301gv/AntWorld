package lab2017;

public class Cell {
	
	int state;
	Location loc;
	
	// cell state
	final static int UNDEFINED = -1;
	final static int BACKGROUND = 0;//255 << 24 | 255 << 16 | 255 << 8 | 255;
	final static int FOOD = 1; //255 << 24 | 128;
	final static int NEST = 2; //255 << 24 | 128 << 8;
	final static int OBSTACLE = 3; //255 << 24;
	final static int FALSEFOOD = 4;
	
	public Cell(Location l, int s){
		
		state = s;
		loc = l;
	}
}
