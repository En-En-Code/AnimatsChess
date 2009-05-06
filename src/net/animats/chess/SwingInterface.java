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
		MessageDialog hintDialog = new MessageDialog(SwingInterface.this, "Hint", "I recommend playing " + _move.Algebraic());
		hintDialog.setVisible(true);
	}
	
	private void UpdateDisplay(Position _position) {
		boardPanel.SynchronizeDisplay();
        scoreSheet.SynchronizeDisplay();
	}

	public void Start() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
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
					engine.Quit();
					// What is the correct way to close the Swing interface???
				}
			});
		//Add the options menu.
		JMenu optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);
		
		//Add the configure menu item.
		JMenuItem configureItem = optionsMenu.add("Configure");
		
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
		
		JMenuItem aboutItem = helpMenu.add("About");
		aboutItem.addActionListener(new 
			ActionListener() {
				public void actionPerformed(ActionEvent _event) {
					MessageDialog aboutDialog = new MessageDialog(SwingInterface.this, "About", "Animats Chess by Stuart Allen 2007");
					aboutDialog.setVisible(true);
				}
			});
		
		validate();
		pack();
	}
}
