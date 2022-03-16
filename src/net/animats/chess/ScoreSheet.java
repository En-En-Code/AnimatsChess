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
import java.awt.*;

import javax.swing.*;

public class ScoreSheet extends JTextArea {
	
	static final long serialVersionUID = 1;
	private Position theBoard;
	
	public ScoreSheet (int _height, int _width, Position _theBoard) {
		super (_height, _width);
		setLineWrap(true);
		setWrapStyleWord(true);
		setEditable(false);
		setFont(new Font("SansSerif", Font.PLAIN, 12));
		setForeground(new Color(0, 0, 0));
		setBorder(BorderFactory.createEtchedBorder());
		theBoard = _theBoard;
	}
	
	public void SynchronizeDisplay() {
		setText("");
		for (Move move : theBoard.getScoreSheet().getArrayList()) {
			DisplayMove(move);
		}
	}
	
	public void DisplayMove(Move _move) {
		// Display the move that was played.
		if (_move.madeBy == Resources.WHITE) {
			append(_move.ScoreSheetAlgebraic());
			append(" ");
		} else {
			append(_move.ScoreSheetAlgebraic());
			append(" ");
		}
		
		if (theBoard.getGameState() == Move.GameState.WHITE_CHECKMATED)
			append("\n\ncheckmate: 0-1");
		if (theBoard.getGameState() == Move.GameState.BLACK_CHECKMATED)
			append("\n\ncheckmate: 1-0");
		if (theBoard.getGameState() == Move.GameState.WHITE_STALEMATED || theBoard.getGameState() == Move.GameState.BLACK_STALEMATED)
			append("\n\nstalemate: 1/2 - 1/2");
	}
}
