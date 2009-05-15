package net.animats.chess;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class BoardPanel extends JPanel {
	
	static final long serialVersionUID = 1;

	private Position theBoard;
	private Engine engine;
	private SwingInterface swingInterface;
	
	public BoardSquare sourceSquare = null;
	public BoardSquare highlightedSquare = null;
	
	private SquareAction squareAction = new SquareAction();
	
	private BoardSquare[][] squares = new BoardSquare[8][8];
	
	public BoardPanel(Engine _engine, SwingInterface _swingInterface) {
		engine = _engine;
		theBoard = _engine.theBoard;
		swingInterface = _swingInterface;
				
		setLayout(new GridLayout(8, 8, 0, 0));
				
		for (int rank = 7; rank > -1; rank--) {
			for (int file = 0; file < 8; file++) {
				squares[rank][file] = new BoardSquare(rank, file);
				add(squares[rank][file]);
			}
		}
		SynchronizeDisplay();
	}
	
	public void SynchronizeDisplay() {
		for (int rank = 7; rank > -1; rank--) {
			for (int file = 0; file < 8; file++) {
				if (theBoard.getPieceAt(rank, file) == null) {
					squares[rank][file].setIcon(null);
					squares[rank][file].setBorder(null);
				} else {
					java.net.URL imageURL = BoardPanel.class.getResource("/net/animats/chess/images/" + theBoard.getPieceAt(rank, file).icon);

					if (imageURL != null)
						squares[rank][file].setIcon(new ImageIcon(imageURL));

					if (theBoard.IsLastDestination(rank, file)) {
						highlightedSquare = squares[rank][file];
						highlightedSquare.setBorder(new LineBorder(Color.RED, 2));
					} else {
						squares[rank][file].setBorder(null);
					}
				}
			}
		}
	}
	
	private class BoardSquare extends JButton {
		
		static final long serialVersionUID = 1;
		public int rank;
		public int file;
		
		BoardSquare (int _rank, int _file) {
			super ();
			rank = _rank;
			file = _file;
			setMinimumSize(new Dimension(55, 55));
			setMaximumSize(new Dimension(55, 55));
			setPreferredSize(new Dimension(55, 55));
			setBorder(null);
			setFocusable(false);
			SetDefaultColour();
			addActionListener(squareAction);
		}
		
		public void SetDefaultColour() {
			if ((rank - file) % 2 != 0)
				//WHITE
				setBackground(new Color(210,190,150));
			else
				//BLACK
				setBackground(new Color(104,74,31));
		}
	}
	
	private class SquareAction implements ActionListener {
		
		public void actionPerformed(ActionEvent _event) {
			if (swingInterface.thinking)
				return;
			
			BoardSquare eventSource = (BoardSquare) _event.getSource(); 
						
			if (sourceSquare == null) {
				// There are currently no pieces highlighted.
				if (theBoard.getPieceAt(eventSource.rank, eventSource.file) == null) {
					// The source can't be an empty square so do nothing.
					return;
				} else {
					// Check if a piece of the correct colour has been selected.
					if (theBoard.getPieceAt(eventSource.rank, eventSource.file).colour != theBoard.getWhoseTurn())
						return;
					
					// Set this square as the source and make it red.
					sourceSquare = eventSource;
					if (highlightedSquare != null)
						highlightedSquare.setBorder(null);
					highlightedSquare = sourceSquare;
					sourceSquare.setBorder(new LineBorder(Color.RED, 2));
				}
			} else {
				// There is already one piece highlighted.
				if (sourceSquare == eventSource) {
					// The same square has been clicked again, so deselect it.
					sourceSquare.setBorder(null);
					highlightedSquare = null;
					sourceSquare = null;
				} else if (theBoard.getPieceAt(eventSource.rank, eventSource.file) != null && theBoard.getPieceAt(eventSource.rank, eventSource.file).colour == theBoard.getWhoseTurn()) {	
					// A different piece has been selected, so change the source to that one.
					sourceSquare = eventSource;
					if (highlightedSquare != null)
						highlightedSquare.setBorder(null);
					highlightedSquare = sourceSquare;
					sourceSquare.setBorder(new LineBorder(Color.RED, 2));
				} else{
					// This is an attempt to move so build the command string
					StringBuffer commandBuffer = new StringBuffer();
					commandBuffer.append(Move.fileLetter[sourceSquare.file]);
					commandBuffer.append(Move.rankNumber[sourceSquare.rank]);
					commandBuffer.append(Move.fileLetter[eventSource.file]);
					commandBuffer.append(Move.rankNumber[eventSource.rank]);
					String command = commandBuffer.toString();
					
					if (engine.IsLegalMove(command) != null) {
						sourceSquare = null;
						engine.HumanMove(command);
						SynchronizeDisplay();
						
						// Human has made a move, so if the other player is computer controlled, start thinking...
						if (AnimatsChess.player[theBoard.CurrentPlayer()].computer && swingInterface.thinking == false) {
							swingInterface.StartThinking(false);
						}
					}
				}
			}
		}
	}
}
