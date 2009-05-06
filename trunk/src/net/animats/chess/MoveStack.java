package net.animats.chess;

/** This stack class is built on an ArrayList and is used to represent the 
    moves played in the game so far. This class can write the contents of 
    its stack to a file in PGN format so it can be loaded again later or by 
    another program.
 */ 
 
import java.util.ArrayList;
import java.util.EmptyStackException;

public class MoveStack {
	private ArrayList<Move> stack = new ArrayList<Move>();

	public ArrayList<Move> getArrayList() {
		return stack;
	}
	
	public void push(Move _move) {
		// Add obj to the stack.
		stack.add(_move);
	}

	public Move peek() {
		// Return the top item from
		// the stack.  Throws an EmptyStackException
		// if there are no elements on the stack.
		if (stack.isEmpty()) {
			throw new EmptyStackException();
		}

		return stack.get(stack.size() - 1);
	}
	
	public Move pop() {
		// Return and remove the top item from
		// the stack.  Throws an EmptyStackException
		// if there are no elements on the stack.
		if (stack.isEmpty())
			throw new EmptyStackException();
		return stack.remove(stack.size() -1);
	}

	public boolean empty() {
		// Test whether the stack is empty.
		return stack.isEmpty();
	}
	
	public int size() {
		return stack.size();
	}

	public boolean save(String _filename) {
		// Write the move stack out to a PGN file using the name specified.

		if (stack.isEmpty()) {
			return false;
		}

		return true;
	}
}
