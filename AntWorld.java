package lab2017;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import processing.core.PApplet;

public class AntWorld extends PApplet {

	private static final long serialVersionUID = 1L;

	/* drawing, animation */
	boolean pause, toggle;

	/* cells */
	Cell[][] cells;
	int numRows, numCols;
	float cellWidth, cellHeight;

	/* single random object for all pseudorandom numbers */
	Random random;

	/* the ants */
	int numAnts;
	ArrayList<Ant> ants;

	/* food and nest - start and goal */
	Location food, nest, falseFood;

	/* Colours */
	final static int ANT = 255 << 24 | 128 << 16;
	final static int VISITED = 64 << 24 | 32 << 16;

	Map<Integer, Integer> stateToColour = new HashMap<Integer, Integer>();

	/* environment */
	enum Env {
		UNINFORMED, DISTANCE, NOISE, DECEPTION
	};

	Env env;

	/* search strategy */
	enum Strategy {
		BFS_TREE, DFS_TREE, BFS_GRAPH, DFS_GRAPH, RANDOM, GREEDY, SWARM
	};

	Strategy strategy;

	public void setup() {

		/* animation */
		size(512, 512);
		pause = true;
		toggle = true; /* toggle obstacle/background when dragging mouse */
		frameRate(10);

		/* initialise world and ants */
		initWorld();

		/* colours for cell state */
		stateToColour.put(Cell.UNDEFINED, -1);
		stateToColour.put(Cell.BACKGROUND, 255 << 24 | 255 << 16 | 255 << 8
				| 255);
		stateToColour.put(Cell.FOOD, 255 << 24 | 128);
		stateToColour.put(Cell.NEST, 255 << 24 | 128 << 8);
		stateToColour.put(Cell.OBSTACLE, 255 << 24);
		stateToColour.put(Cell.FALSEFOOD, 64 << 24 | 128);
	}

	public void initWorld() {

		/* environment and strategy */
		env = Env.DECEPTION;
		strategy = Strategy.SWARM;

		/* number of searchers */
		if (strategy == Strategy.SWARM)
			numAnts = 5;
		else
			numAnts = 1;

		/* PRNG */
		random = new Random();

		/* the world */
		numRows = numCols = 16;
		cellWidth = cellHeight = width / numCols;
		cells = new Cell[numRows][numCols];
		for (int row = 0; row < numRows; row++)
			for (int col = 0; col < numCols; col++)
				cells[row][col] = new Cell(new Location(row, col),
						Cell.BACKGROUND);

		nest = new Location(2, 2);
		cells[nest.row][nest.col].state = Cell.NEST;
		
	
		food = new Location(numRows - 3, numCols - 3);
		cells[food.row][food.col].state = Cell.FOOD;

		if (env == Env.DECEPTION) {
			
			falseFood = new Location(2, numCols - 3);
			cells[falseFood.row][falseFood.col].state = Cell.FALSEFOOD;
			
		}

		/* ants */
		ants = new ArrayList<Ant>();
		for (int i = 0; i < numAnts; i++)
			ants.add(new Ant(nest, this));
		
		
		/* add obstacle */
		// cells[numRows / 2][numCols / 2] = OBSTACLE;
		// cells[numRows / 2 - 1][numCols / 2] = OBSTACLE;
		// cells[numRows / 2 + 1][numCols / 2 - 2] = OBSTACLE;
		// cells[numRows / 2 + 2][numCols / 2 - 2] = OBSTACLE;
	}

	public void draw() {

		background(stateToColour.get(Cell.BACKGROUND));

		drawCells();
		drawAnts();
		drawGrid();

		if (!pause)
			updateAnts();
	}

	void updateAnts() {

		for (int i = 0; i < ants.size(); i++) {

			Ant ant = ants.get(i);

			switch (strategy) {

			/* uniformed */
			default:
			case BFS_TREE:
				ant.simpleTreeSearch(this, Ant.BREADTH_FIRST);
				break;
			case DFS_TREE:
				ant.simpleTreeSearch(this, Ant.DEPTH_FIRST);
				break;
			case BFS_GRAPH:
				ant.simpleGraphSearch(this, Ant.BREADTH_FIRST);
				break;
			case DFS_GRAPH:
				ant.simpleGraphSearch(this, Ant.DEPTH_FIRST);
				break;

			/* informed */
			case RANDOM:
				ant.randomSearch(this);
				break;
			case GREEDY:
				ant.greedySearch(this);
				break;
			case SWARM:
				ant.swarmSearch(this);
				break;
			}
		}
	}

	double f(Location loc) {

		switch (env) {
		case DISTANCE:
			return distance(loc, food);
		case NOISE:
			return noisy(loc);
		case DECEPTION:
			return deceptive(loc);
		default:
		case UNINFORMED:
			return 0;
		}
	}

	private double noisy(Location loc) {

		double variance = 0.1 * width;
		double noise = variance * random.nextGaussian();

		return f(loc) + noise;
	}

	double distance(Location locA, Location locB) {

		double xA = (locA.col + 0.5) * cellWidth;
		double yA = (locA.row + 0.5) * cellHeight;

		double xB = (locB.col + 0.5) * cellWidth;
		double yB = (locB.row + 0.5) * cellHeight;

		double delX = xA - xB;
		double delY = yA - yB;

		return Math.sqrt(delX * delX + delY * delY);
	}

	private double deceptive(Location loc) {

		return Math.min(distance(loc, food), 2 * distance(loc, falseFood));
	}

	void drawAnts() {

		rectMode(CORNER);
		noStroke();

		for (int i = 0; i < ants.size(); i++) {

			Ant ant = ants.get(i);

			for (Location l : ant.explored) {
				fill(VISITED);
				rect(l.col * cellWidth, l.row * cellHeight, cellWidth,
						cellHeight);
			}

			if (inWorld(ant)) {
				fill(ANT);
				rect(ant.pos.col * cellWidth, ant.pos.row * cellHeight,
						cellWidth, cellHeight);
			}

		}
	}

	/* BACKGROUND cells are not drawn */
	void drawCells() {

		noStroke();
		rectMode(CORNER);

		for (int col = 0; col < numCols; col++) {
			for (int row = 0; row < numRows; row++) {

				if (cells[row][col].state != Cell.BACKGROUND) {

					fill(stateToColour.get(cells[row][col].state));
					rect(col * cellWidth, row * cellHeight, cellWidth,
							cellHeight);
				}
			}
		}
	}

	void drawGrid() {

		stroke(128);

		for (int col = 1; col <= numCols; col++) {
			line(col * cellWidth, 0, col * cellWidth, height);
		}

		for (int row = 1; row <= numRows; row++) {
			line(0, row * cellHeight, width, row * cellHeight);
		}
	}

	boolean inWorld(Ant ant) {

		return inWorld(ant.pos.row, ant.pos.col);
	}

	boolean inWorld(Location pos) {

		return inWorld(pos.row, pos.col);
	}

	boolean inWorld(int row, int col) {

		if (row > -1 && row < numRows && col > -1 && col < numCols)
			return true;
		else
			return false;
	}

	int getCellState(int row, int col) {

		if (inWorld(row, col))
			return cells[row][col].state;
		else
			return Cell.UNDEFINED;
	}

	int getCellState(Location pos) {

		return getCellState(pos.row, pos.col);
	}

	void setCellState(int row, int col, int state) {

		if (inWorld(row, col))
			cells[row][col].state = state;
	}

	public void keyPressed() {

		if (key == ' ')
			pause = !pause;
		else if (key == 't')
			toggle = !toggle;
	}

	public void mouseClicked() {

		int row = (int) (mouseY / cellWidth);
		int col = (int) (mouseX / cellHeight);
		Location loc = new Location(row, col);
		if (!loc.equals(food) && !loc.equals(nest))
			setCellState(row, col, toggle ? Cell.BACKGROUND : Cell.OBSTACLE);

		println(f(loc));
	}

	public void mouseDragged() {

		int row = (int) (mouseY / cellWidth);
		int col = (int) (mouseX / cellHeight);
		Location loc = new Location(row, col);
		if (!loc.equals(food) && !loc.equals(nest))
			setCellState(row, col, toggle ? Cell.OBSTACLE : Cell.BACKGROUND);
	}

	public static void main(String[] args) {

		PApplet.main(new String[] { "lab2017.AntWorld" });
	}
}
