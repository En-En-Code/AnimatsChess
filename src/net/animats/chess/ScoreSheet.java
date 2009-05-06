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
