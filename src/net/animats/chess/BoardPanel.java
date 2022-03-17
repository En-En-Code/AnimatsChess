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
		
		private boolean promoting = false;
		
		public void actionPerformed(ActionEvent _event) {
			if (swingInterface.thinking)
				return;
			
			BoardSquare eventSource = (BoardSquare) _event.getSource();
			
			// This board square was created by the promotion dialog,
			// so do not try to do anything with it
			if (promoting)
				return;
						
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
				} else {
					// This is an attempt to move so build the command string
					StringBuffer commandBuffer = new StringBuffer();
					commandBuffer.append(Move.fileLetter[sourceSquare.file]);
					commandBuffer.append(Move.rankNumber[sourceSquare.rank]);
					commandBuffer.append(Move.fileLetter[eventSource.file]);
					commandBuffer.append(Move.rankNumber[eventSource.rank]);
					
					// Create a window if a pawn is being promoted to allow
					// the user to select the promotion of their choice
					if (theBoard.getPieceAt(sourceSquare.rank, sourceSquare.file) != null &&
						theBoard.getPieceAt(sourceSquare.rank, sourceSquare.file).FullName().equals("pawn") &&
						(eventSource.rank == 0 || eventSource.rank == 7)) {
						promoting = true;
						
						// Create the squares for the promotion dialog.
						// Their placement relative to <code>eventSource</code> guarantees it is off the
						// board and the color of the squares are the same as the square the pawn has moved to.
						BoardSquare promotionTiles[] = {new BoardSquare(eventSource.rank, eventSource.file - 20),
														new BoardSquare(eventSource.rank, eventSource.file - 20),
														new BoardSquare(eventSource.rank, eventSource.file - 20),
														new BoardSquare(eventSource.rank, eventSource.file - 20)};
						
						// Assign the value that each button should return in the menu
						promotionTiles[0].addActionListener(new ActionListener() {
			                @Override
			                public void actionPerformed(ActionEvent e) {
			                    JOptionPane pane = (JOptionPane)(((JComponent)e.getSource()).getParent().getParent());
			                    // set the value of the option pane
			                    pane.setValue(promotionTiles[0]);
			                }
			            });
						// Yes, I have to write this nearly identical code four times; I tried looping it
						promotionTiles[1].addActionListener(new ActionListener() {
			                @Override
			                public void actionPerformed(ActionEvent e) {
			                    JOptionPane pane = (JOptionPane)(((JComponent)e.getSource()).getParent().getParent());
			                    // set the value of the option pane
			                    pane.setValue(promotionTiles[1]);
			                }
			            });
						// The unresolved loop variable in the internal function causes an error
						promotionTiles[2].addActionListener(new ActionListener() {
			                @Override
			                public void actionPerformed(ActionEvent e) {
			                    JOptionPane pane = (JOptionPane)(((JComponent)e.getSource()).getParent().getParent());
			                    // set the value of the option pane
			                    pane.setValue(promotionTiles[2]);
			                }
			            });
						// Why does Java have to be like this?
						promotionTiles[3].addActionListener(new ActionListener() {
			                @Override
			                public void actionPerformed(ActionEvent e) {
			                    JOptionPane pane = (JOptionPane)(((JComponent)e.getSource()).getParent().getParent());
			                    // set the value of the option pane
			                    pane.setValue(promotionTiles[3]);
			                }
			            });
						
						// Put the pictures of the pieces in the squares
						promotionTiles[0].setIcon(new ImageIcon(BoardPanel.class.getResource("/net/animats/chess/images/wqueen.png")));
						promotionTiles[1].setIcon(new ImageIcon(BoardPanel.class.getResource("/net/animats/chess/images/wrook.png")));
						promotionTiles[2].setIcon(new ImageIcon(BoardPanel.class.getResource("/net/animats/chess/images/wbishop.png")));
						promotionTiles[3].setIcon(new ImageIcon(BoardPanel.class.getResource("/net/animats/chess/images/wknight.png")));
						
						// Create the dialog box that comes up when promoting and keep
						// showing it so long as the player has not selected a piece
						int promotionChoice = JOptionPane.CLOSED_OPTION;
						while (promotionChoice == JOptionPane.CLOSED_OPTION) {
							promotionChoice = JOptionPane.showOptionDialog(swingInterface, null, "Promote Pawn",
									JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, promotionTiles, promotionTiles[0]);
						}
						
						// Convert the choice into a number to use for <code>promotionLetter</code>
						promotionChoice = -promotionChoice + 5;
						commandBuffer.append(Move.promotionLetter[promotionChoice]);
						promoting = false;
					}
					
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
