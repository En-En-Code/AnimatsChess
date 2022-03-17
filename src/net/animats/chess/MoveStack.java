/*
 * Animats Chess Engine, started 8 August 2005, played its first game 9 September 2005
 * Copyright (C) 2005-2009 Stuart Allen, 2022 En-En
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

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
