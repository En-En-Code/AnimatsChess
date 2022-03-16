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

/**
 * <code>InputInterface</code> simply defines a standard entry
 * point for the <code>AnimatsChess</code> class to call in
 * order to enter the main command loop.
 *
 */

interface IOInterface {
	/**
	 * This method is called when an informational text string
	 * is to be displayed.
	 */
	void Message(String _message);

	/**
	 * This method is called when an informational text string
	 * regarding the engine's thinking is to be displayed.
	 */
	void Thinking(int _ply, int _evaluation, double _time, int _nodes, String _line);

	/** 
	 * This method is called when the board has changed and
	 * the display needs to be updated to relfect the change.
	 * It takes a single parameter that is a reference to a
	 * <code>Move</code> object representing the move that
	 * was made.
	 */
	void MoveMade(Move _move);

	/** 
	 * This method is called when the engine has analysed the
	 * position and calculated a suggested best move.
	 * It takes a single parameter that is a reference to a
	 * <code>Move</code> object representing the move that
	 * was made.
	 */
	void SuggestedMove(Move _move);

	/**
	 * The method to be implemented by a specific instance
	 * of an interface that ultimately enters the command
	 * loop. It is passed a reference to the <code>Engine</code>
	 * that does the thinking.
	 */
	public void Start ();

	/**
	 * This method is called by the engine when it has finished processing
	 * its move.
	 */
	public void Finished(double _timeTaken, int _movesCalcuated, Move _move, int _nodesPerSecond);
	
}
