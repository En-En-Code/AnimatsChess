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

import java.awt.*;
import java.awt.event.*;

public class SwingInterface extends JFrame implements IOInterface {

	static final long serialVersionUID = 1;
	
	private Engine engine;
	private Position theBoard;
	private BoardPanel boardPanel;
	private JTextArea thinkingArea;
	private ScoreSheet scoreSheet;
	private JMenuItem undoItem;
	public boolean thinking = false;
	
	public void Message(String _message) {
		// TODO Auto-generated method stub
	}

	public void Thinking(int _ply, int _evaluation, double _time, int _nodes, String _line) {
		StringBuffer output = new StringBuffer();
		output.append(" " + _ply);
		ColumnPadding(output, 4);
		output.append('[');
		if (_evaluation > 0)
			output.append('+');
	       	output.append( _evaluation + "]");
		ColumnPadding(output, 11);
		output.append((long) _time + "ms");
		ColumnPadding(output, 21);
	       	output.append(_nodes + " nodes");
		ColumnPadding(output, 36);
		output.append(_line);
		output.append("\n");
		thinkingArea.append(output.toString());
	}
	
	private void ColumnPadding(StringBuffer _buffer, int _column) {
		while (_buffer.length() < _column)
			_buffer.append(' ');
	}

	public void MoveMade (Move _move) {
		scoreSheet.DisplayMove(_move);
		undoItem.setEnabled(true);
	}
	
	public void SuggestedMove (Move _move) {
		// Popup modal window with hint.
		JOptionPane.showMessageDialog(SwingInterface.this, "I recommend playing " + _move.Algebraic() + ".", "Hint", JOptionPane.PLAIN_MESSAGE);
	}
	
	private void UpdateDisplay(Position _position) {
		boardPanel.SynchronizeDisplay();
        scoreSheet.SynchronizeDisplay();
	}

	public void Start() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	public void Close() {
		engine.Quit();
		SwingInterface.this.dispose();
	}

	public void Finished(double _timeTaken, int _movesCalculated, Move _move, int _nodesPerSecond) {
		thinking = false;
		boardPanel.SynchronizeDisplay();
		thinkingArea.append ("\ntotal time = " + (int) _timeTaken + "ms, total nodes = " + _movesCalculated + " (" + _nodesPerSecond + " nodes/second)\n\n");
		
		// Scroll to the bottom.
		//thinkingArea.validate();
		//JScrollBar sb = thinkingPane.getVerticalScrollBar();
		//sb.setValue(sb.getMaximum());	
	}
	
	public void StartThinking(Boolean _analysis_only) {
		thinking = true;
		thinkingArea.setText(null);
		engine.StartThinking(_analysis_only);
	}

	public SwingInterface(Engine _engine) {
		engine = _engine;
		theBoard = engine.theBoard;
		
		setTitle("Animats Chess");
		setLayout(new GridBagLayout());
		
		boardPanel = new BoardPanel(engine, this);
		boardPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		GridBagConstraints boardConstraints = new GridBagConstraints();
		boardConstraints.gridx = 0;
		boardConstraints.gridy = 0;
		boardConstraints.gridheight = 2;
		boardConstraints.gridwidth = 1;
		boardConstraints.weightx = 0;
		boardConstraints.weighty = 0;
		boardConstraints.insets = new Insets(2, 2, 5, 5);
		add (boardPanel, boardConstraints);
		
		thinkingArea = new JTextArea(10,40);
		thinkingArea.setEditable(false);
		thinkingArea.setBorder(BorderFactory.createLoweredBevelBorder());
		thinkingArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		thinkingArea.setForeground(new Color(0, 200, 200));
		thinkingArea.setBackground(new Color(40, 40, 40));
		JScrollPane thinkingPane = new JScrollPane(thinkingArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		GridBagConstraints thinkingConstraints = new GridBagConstraints();
		thinkingConstraints.gridx = 0;
		thinkingConstraints.gridy = 2;
		thinkingConstraints.gridheight = 1;
		thinkingConstraints.gridwidth = 2;
		thinkingConstraints.weightx = 100;
		thinkingConstraints.weighty = 100;
		thinkingConstraints.insets = new Insets(0, 2, 2, 2);
		thinkingConstraints.fill = GridBagConstraints.BOTH;
		add (thinkingPane, thinkingConstraints);
		
		String titleText = AnimatsChess.player[Resources.WHITE].name
						 + " vs "
						 + AnimatsChess.player[Resources.BLACK].name;
		JLabel titleLabel = new JLabel(titleText);
		titleLabel.setMinimumSize(new Dimension(200, 35));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setPreferredSize(titleLabel.getMinimumSize());
		titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
		GridBagConstraints titleConstraints = new GridBagConstraints();
		titleConstraints.gridx = 1;
		titleConstraints.gridy = 0;
		titleConstraints.gridheight = 1;
		titleConstraints.gridwidth = 1;
		titleConstraints.weightx = 100;
		titleConstraints.weighty = 0;
		titleConstraints.insets = new Insets(10, 5, 5, 5);
		titleConstraints.anchor = GridBagConstraints.CENTER;
		add (titleLabel, titleConstraints);
		
		scoreSheet = new ScoreSheet(20, 12, theBoard);
		JScrollPane scoreSheetPane = new JScrollPane(scoreSheet, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		GridBagConstraints scoreConstraints = new GridBagConstraints();
		scoreConstraints.gridx = 1;
		scoreConstraints.gridy = 1;
		scoreConstraints.insets = new Insets(10, 0, 5, 2);
		scoreConstraints.gridheight = 1;
		scoreConstraints.gridwidth = 1;
		scoreConstraints.weightx = 100;
		scoreConstraints.weighty = 0;
		scoreConstraints.fill = GridBagConstraints.BOTH;
		add (scoreSheetPane, scoreConstraints);

		// Create the top-level menu bar.
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		//Add the options menu.
		JMenu gameMenu = new JMenu("Game");
		menuBar.add(gameMenu);
		
		//Add the new game menu item
		JMenuItem newGameItem = gameMenu.add("New");
		newGameItem.addActionListener(new
			ActionListener() {
				public void actionPerformed(ActionEvent _event) {
					if (thinking == false) {
						engine.Reset();
						scoreSheet.setText(null);
						boardPanel.sourceSquare = null;
						boardPanel.highlightedSquare = null;
						UpdateDisplay(engine.GetCurrentPosition());
					}
				}
			});
		
		//Add the undo move menu item
		undoItem = gameMenu.add("Undo move");
		undoItem.setEnabled(false);
		undoItem.addActionListener(new
			ActionListener() {
				public void actionPerformed(ActionEvent _event) {
					if (thinking == false) {
						if (AnimatsChess.player[theBoard.OtherPlayer()].computer) {
							theBoard.UndoMove();
						}
						theBoard.UndoMove();
						
						if (theBoard.getScoreSheet().size() == 0) {
							undoItem.setEnabled(false);
						}
						engine.DetermineImmediateMoves();
						boardPanel.sourceSquare = null;
						boardPanel.highlightedSquare = null;
						scoreSheet.SynchronizeDisplay();
						UpdateDisplay(engine.GetCurrentPosition());
					}
				}
			});
		
		//Add the hint menu item
		JMenuItem hintItem = gameMenu.add("Hint");
		hintItem.addActionListener(new
			ActionListener() {
				public void actionPerformed(ActionEvent _event) {
					thinking = true;
					thinkingArea.setText(null);
					engine.StartThinking(true);
				}
			});

		//Add the quit game menu item
		JMenuItem quitItem = gameMenu.add("Quit");
		quitItem.addActionListener(new
			ActionListener() {
				public void actionPerformed(ActionEvent _event) {
					SwingInterface.this.Close();
				}
			});
		// Makes sure the engine thread terminates on program close
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				SwingInterface.this.Close();
			}
		});
		
		//Add the options menu.
		JMenu optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);
		
		//Add the configure menu item.
		JMenuItem configureItem = optionsMenu.add("Configure");
		configureItem.addActionListener(new
				ActionListener() {
					public void actionPerformed(ActionEvent _event) {
						ConfigureFrame configureFrame = new ConfigureFrame(SwingInterface.this, "Configure");
						configureFrame.setVisible(true);
					}
				});
		
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
		
		JMenuItem aboutItem = helpMenu.add("About");
		aboutItem.addActionListener(new 
			ActionListener() {
				public void actionPerformed(ActionEvent _event) {
					JOptionPane.showMessageDialog(SwingInterface.this, "ANIMATS CHESS by Stuart Allen 2005-09", "About", JOptionPane.PLAIN_MESSAGE);
				}
			});
		
		JMenuItem warrantyItem = helpMenu.add("Warranty");
		warrantyItem.addActionListener(new 
			ActionListener() {
				public void actionPerformed(ActionEvent _event) {
					JOptionPane.showMessageDialog(SwingInterface.this,
					"This program is distributed in the hope that it will be useful,\n" +
    				"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
    				"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
   					"GNU General Public License for more details.",
   					"Warranty", JOptionPane.PLAIN_MESSAGE);
				}
			});
		
		JMenuItem copyingItem = helpMenu.add("Copying");
		copyingItem.addActionListener(new 
			ActionListener() {
				public void actionPerformed(ActionEvent _event) {
					JOptionPane.showMessageDialog(SwingInterface.this,
					"This program is free software; you can redistribute it and/or modify\n" +
    				"it under the terms of the GNU General Public License as published by\n" +
    				"the Free Software Foundation; either version 2 of the License, or\n" +
    				"(at your option) any later version.",
   					"Copying", JOptionPane.PLAIN_MESSAGE);
				}
			});
		
		validate();
		pack();
	}
	
	private class ConfigureFrame extends JDialog {
		private static final long serialVersionUID = 1;

		private ConfigureFrame(Frame owner, String title) {
			super(owner, title);
			setLayout(new GridBagLayout());
			
			JLabel titleLabel = new JLabel("Engine Options");
			titleLabel.setMinimumSize(new Dimension(200, 35));
			GridBagConstraints titleConstraints = new GridBagConstraints();
			titleConstraints.gridx = 0;
			titleConstraints.gridy = 0;
			titleConstraints.gridheight = 1;
			titleConstraints.gridwidth = 1;
			titleConstraints.weightx = 0;
			titleConstraints.weighty = 0;
			titleConstraints.insets = new Insets(2, 2, 4, 4);
			add(titleLabel, titleConstraints);
			
			JCheckBox randomBox = new JCheckBox("Random", engine.random);
			randomBox.setMnemonic(KeyEvent.VK_R);
			randomBox.addItemListener(new
				ItemListener() {
					public void itemStateChanged(ItemEvent _event) {
						engine.random = !engine.random;
					}
			});
			GridBagConstraints randBoxConstraints = new GridBagConstraints();
			randBoxConstraints.gridx = 0;
			randBoxConstraints.gridy = 1;
			randBoxConstraints.gridheight = 1;
			randBoxConstraints.gridwidth = 1;
			randBoxConstraints.weightx = 0;
			randBoxConstraints.weighty = 0;
			randBoxConstraints.insets = new Insets(2, 2, 4, 4);
			add(randomBox, randBoxConstraints);
			
			validate();
			pack();
			setLocationRelativeTo(owner);
		}
	}
}
