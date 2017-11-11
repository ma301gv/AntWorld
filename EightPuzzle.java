package lab2017;

import java.util.ArrayList;

public class EightPuzzle {

	public static void println(String s) {
		System.out.println(s);
	}

	public static void print(String s) {
		System.out.print(s);
	}

	public static void println() {
		System.out.println();
	}

	public static void display(Integer[] state) {
		for (int i = 0; i < 9; i++) {
			if (i % 3 == 0)
				println();
			print(state[i] != 0 ? "" + state[i] : " ");
			print(" ");
		}
		println();
	}

	public static Integer[] swap(int i, int j, Integer[] a) {

		Integer[] swapped = new Integer[a.length];
		System.arraycopy(a, 0, swapped, 0, a.length);
		swapped[i] = a[j];
		swapped[j] = a[i];
		return swapped;
	}

	public static boolean compareState(Integer[] a, Integer[] b) {

		boolean equal = true;
		for (int i = 0; i < a.length && equal; i++) {
			if (a[i] != b[i])
				equal = false;
		}
		return equal;
	}

	public static boolean stateIsInList(ArrayList<Node> list, Node node) {

		boolean found = false;
		for (int i = 0; i < list.size() && !found; i++) {
			if (compareState(list.get(i).getState(), node.getState()))
				found = true;
		}
		return found;
	}

	private static ArrayList<Integer[]> availableStates(Integer[] state) {

		ArrayList<Integer[]> available = new ArrayList<Integer[]>();
		int pos = 0;
		for (; state[pos] != 0; pos++)
			; // don't try this at home!
		int row = pos / 3;
		int col = pos % 3;

		if (col < 2) {

			Integer[] s = swap(pos, pos + 1, state);
			available.add(s);
		}

		if (col > 0) {

			Integer[] s = swap(pos, pos - 1, state);
			available.add(s);
		}

		if (row < 2) {

			Integer[] s = swap(pos, pos + 3, state);
			available.add(s);
		}

		if (row > 0) {

			Integer[] s = swap(pos, pos - 3, state);
			available.add(s);
		}

		return available;
	}

	private static ArrayList<Node> getSolution(Node node) {

		ArrayList<Node> solution = new ArrayList<Node>();
		while (node.getParent() != null) {
			solution.add(node);
			node = node.getParent();
		}
		solution.add(node);
		return solution;
	}

	private static int depth(Node node) {

		int depth = 0;
		while (node.getParent() != null) {
			node = node.getParent();
			depth++;
		}
		return depth;
	}

	public static ArrayList<Node> solver(Integer[] initialState,
			boolean graphSearch, boolean bfs, int maxDepth, int maxAttempts) {

		ArrayList<Node> frontier = new ArrayList<Node>();
		ArrayList<Node> explored = new ArrayList<Node>();

		Node node = new Node(initialState, null);
		println("Initial state");
		display(initialState);

		// add initial to frontier
		frontier.add(node);

		int count = 0;
		while (count++ < maxAttempts) {

			// failure?
			if (frontier.isEmpty()) {
				println("\nfrontier is empty, terminating search...");
				return null;
			}

			// System.out.println("\n" + "frontier\n" + frontier);

			// remove node
			Node removed;
			if (bfs)
				removed = frontier.remove(0); // bfs
			else
				removed = frontier.remove(frontier.size() - 1); // dfs

			Integer[] state = removed.getState();

			// display removed
			println("\nAttempt: " + count);
			println("\nremoved");
			display(removed.getState());

			// a solution?
			boolean solved = true;
			for (int i = 0; i < 9 && solved; i++) {
				if (state[i] != i)
					solved = false;
			}
			if (solved)
				return getSolution(removed);

			// add to explored
			if (graphSearch)
				explored.add(removed);

			// find depth
			int depth = depth(removed);

			if (depth <= maxDepth) {

				// expand
				ArrayList<Integer[]> available = availableStates(state);
				for (int i = 0; depth <= maxDepth && i < available.size(); i++) {
					Node n = new Node(available.get(i), removed);
					if (!graphSearch
							|| (!stateIsInList(frontier, n) && !stateIsInList(
									explored, n)))
						frontier.add(n);
				}
			} else
				println("\ntoo deep, not expanding");

			println("\nDepth: " + depth);
			println("Size of frontier: " + frontier.size());
			println("Size of explored: " + explored.size());
			println("Number of moves: " + count);
		}

		// failed
		println("\nran out of attempts, terminating search...");
		return null;
	}

	public static void main(String[] args) {

		Integer[] initialState = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
		initialState = swap(0, 1, initialState);
		initialState = swap(1, 2, initialState);
		// initialState = swap(2, 5, initialState);
		// initialState = swap(5, 4, initialState);
		// initialState = swap(4, 7, initialState);
		// initialState = swap(7, 8, initialState);
		// initialState = swap(8, 5, initialState);
		// initialState = swap(5, 2, initialState);
		// initialState = swap(2, 1, initialState);
		// initialState = swap(1, 4, initialState);
		// initialState = swap(4, 7, initialState);
		// initialState = swap(7, 6, initialState);

		boolean graphSearch = false;
		boolean bfs = true;
		int maxDepth = 40;
		int maxAttempts = Integer.MAX_VALUE;

		ArrayList<Node> solution = solver(initialState, graphSearch, bfs,
				maxDepth, maxAttempts);
		if (solution == null)
			println("\nI failed");
		else {
			println("\nI did it in " + (solution.size() - 1)
					+ " moves! Here's my solution:");
			for (int i = solution.size() - 1; i >= 0; i--)
				display(solution.get(i).getState());

			println("\nMoves: " + (solution.size() - 1));
		}

	}
}
