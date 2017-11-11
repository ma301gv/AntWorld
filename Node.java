package lab2017;

import java.util.Arrays;

public class Node {

	private Integer[] state;

	private Node parent;

	public Node(Integer[] s, Node n) {

		state = s;
		parent = n;
	}

	public Node getParent() {
		return parent;
	}
	
	public Integer[] getState(){
		return state;
	}
	
	public String toString(){
		return Arrays.toString(state);
	}

}
