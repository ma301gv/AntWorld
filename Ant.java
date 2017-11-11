package lab2017;

import java.util.ArrayList;
import java.util.Random;

public class Ant {

	Location pos;
	int state;

	final static double TWOPI = 2 * Math.PI;

	/* directions relative to current cell */
	final static int EAST = 0, NORTHEAST = 1, NORTH = 2, NORTHWEST = 3,
			WEST = 4, SOUTHWEST = 5, SOUTH = 6, SOUTHEAST = 7;

	final static String BREADTH_FIRST = "breadth first",
			DEPTH_FIRST = "depth first";

	/* cells to visit */
	ArrayList<Location> frontier;

	/* cells already expanded */
	ArrayList<Location> explored;

	/* permit diagonal movement */
	boolean diagonal;

	Ant(int r, int c, AntWorld antWorld) {

		pos = new Location(r, c);
		state = 0;

		/* in case the search program does not check the initial position */
		if (antWorld.food.equals(pos))
			stop(antWorld);

		frontier = new ArrayList<Location>();
		frontier.add(new Location(pos));

		explored = new ArrayList<Location>();

		/* ant moves diagonally? */
		diagonal = false;
	}

	Ant(Location l, AntWorld antWorld) {

		this(l.row, l.col, antWorld);
	}

	/* return to nest */
	void stop(AntWorld antWorld) {

		/* If algorithm */
		System.out.println("found food in " + explored.size() + " steps");
		antWorld.pause = true;

		// returnToNest(antWorld);
	}

	void returnToNest(AntWorld antWorld) {

		pos = new Location(antWorld.nest);

		frontier.clear();
		frontier.add(new Location(pos));

		explored.clear();
	}

	/* tree search */
	public void simpleTreeSearch(AntWorld antWorld, String strategy) {

		if (frontier.isEmpty()) {
			System.out.println("failure");
			return;
		}

		Location loc = null;
		if (strategy.equals(BREADTH_FIRST)) {
			loc = frontier.remove(0);
		} else if (strategy.equals(DEPTH_FIRST)) {
			loc = frontier.remove(frontier.size() - 1);
		} else {
			System.out.println("strategy is not applicable");
			return;
		}

		move(loc);

		if (antWorld.food.equals(pos)) {
			stop(antWorld);
			return;
		}

		if (!inList(explored, pos))
			explored.add(new Location(pos));

		ArrayList<Location> expanded = availableCells(antWorld, pos);
		for (int i = 0; i < expanded.size(); i++) {
			frontier.add(expanded.get(i));
		}
	}

	/* graph search */
	public void simpleGraphSearch(AntWorld antWorld, String strategy) {

		if (frontier.isEmpty()) {
			System.out.println("failure");
			return;
		}

		Location loc = null;
		if (strategy.equals(BREADTH_FIRST)) {
			loc = frontier.remove(0);
		} else if (strategy.equals(DEPTH_FIRST)) {
			loc = frontier.remove(frontier.size() - 1);
		} else {
			System.out.println("strategy is not applicable");
			return;
		}

		move(loc);

		if (antWorld.food.equals(pos)) {
			stop(antWorld);
			return;
		}

		explored.add(new Location(pos));

		ArrayList<Location> expanded = availableCells(antWorld, pos);
		for (int i = 0; i < expanded.size(); i++) {
			Location l = expanded.get(i);
			if (!inList(frontier, l) && !inList(explored, l))
				frontier.add(l);
		}
	}

	/* informed search. Ant walks aimlessly, choosing neighbour cells at random */
	void randomSearch(AntWorld antWorld) {

		if (antWorld.food.equals(pos)) {
			stop(antWorld);
			return;
		}

		ArrayList<Location> available = availableCells(antWorld, pos);
		if (available.isEmpty())
			return;

		int index = antWorld.random.nextInt(available.size());
		Location loc = available.get(index);
		move(loc);

		if (!inList(explored, pos))
			explored.add(new Location(pos));

	}

	/* informed search. Ant moves to best cell. */
	public void greedySearch(AntWorld antWorld) {

		ArrayList<Location> available = availableCells(antWorld, pos);
		if (available.isEmpty())
			return;

		ArrayList<Location> best = new ArrayList<Location>();
		double bestVal = antWorld.f(pos);
		best.add(pos);

		for (int i = 0; i < available.size(); i++) {

			Location loc = available.get(i);
			double val = antWorld.f(loc);

			if (val < bestVal) {
				bestVal = val;
				best.clear();
				best.add(loc);
			} else if (val == bestVal) {
				best.add(loc);
			}
		}

		move(best.get(antWorld.random.nextInt(best.size())));

		if (antWorld.food.equals(pos)) {
			stop(antWorld);
			return;
		}

		if (!inList(explored, pos))
			explored.add(new Location(pos));
	}

	/* informed search with a swarm */
	public void swarmSearch(AntWorld antWorld) {

		/*
		 * parameters - ants move towards each other with probability p if
		 * separation is larger than d
		 */
		double d = 2 * antWorld.distance(new Location(0, 0), new Location(1, 1));
		double p = 0.5;

		ArrayList<Location> available = availableCells(antWorld, pos);
		if (available.isEmpty())
			return;

		/* find best position */
		ArrayList<Ant> ants = antWorld.ants;

		double bestValue = antWorld.f(ants.get(0).pos);
		Location bestPos = ants.get(0).pos;
		for (int i = 1; i < ants.size(); i++) {
			Ant ant = ants.get(i);
			double val = antWorld.f(ant.pos);
			if (val < bestValue) {
				bestPos = ant.pos;
			}
		}

		/* find closest available cell(s) to best position */
		ArrayList<Location> closest = new ArrayList<Location>();
		closest.add(available.get(0));
		double dist = antWorld.distance(available.get(0), bestPos);

		for (int i = 1; i < available.size(); i++) {

			if (antWorld.distance(available.get(i), bestPos) < dist) {

				closest.clear();
				closest.add(available.get(i));
				dist = antWorld.distance(available.get(i), bestPos);
			}

			else if (antWorld.distance(available.get(i), bestPos) == dist) {

				closest.add(available.get(i));
			}
		}

		Random random = antWorld.random;

		if (dist > d && random.nextDouble() < p) {
			move(closest.get(random.nextInt(closest.size())));
		} else {
			int index = antWorld.random.nextInt(available.size());
			Location loc = available.get(index);
			move(loc);
		}

		if (antWorld.food.equals(pos)) {
			stop(antWorld);
			return;
		}

		if (!inList(explored, pos))
			explored.add(new Location(pos));

	}

	/* check if a location is in a list */
	public static boolean inList(ArrayList<Location> list, Location loc) {

		for (Location l : list) {
			if (loc.equals(l)) {
				return true;
			}
		}
		return false;
	}

	/* converts an integer (can be negative) to mod N */
	public static int clock(int N, int n) {

		return ((Math.abs(n) / N + 1) * N + n) % N;
	}

	/* where can the ant go from here? */
	ArrayList<Location> availableCells(AntWorld antWorld, Location loc) {

		ArrayList<Location> list = new ArrayList<Location>();
		int incr = diagonal ? 1 : 2;
		for (int dir = 0; dir < 8; dir += incr) {

			Location l = add(loc, dir);
			if (antWorld.inWorld(l)
					&& antWorld.getCellState(l) != Cell.OBSTACLE) {
				list.add(l);
			}
		}
		return list;
	}

	void move(Location loc) {

		pos.row = loc.row;
		pos.col = loc.col;
	}

	Location add(Location l, int dir) {

		Location loc = new Location(l);

		if (dir == EAST) {
			loc.col++;
		} else if (dir == NORTHEAST) {
			loc.row--;
			loc.col++;
		} else if (dir == NORTH) {
			loc.row--;
		} else if (dir == NORTHWEST) {
			loc.row--;
			loc.col--;
		} else if (dir == WEST) {
			loc.col--;
		} else if (dir == SOUTHWEST) {
			loc.row++;
			loc.col--;
		} else if (dir == SOUTH) {
			loc.row++;
		} else if (dir == SOUTHEAST) {
			loc.row++;
			loc.col++;
		}
		return loc;
	}

	public String toString() {

		return "posn = " + pos.toString();
	}
}
