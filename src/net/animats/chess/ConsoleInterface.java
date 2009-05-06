package net.animats.chess;
// Animats Chess Engine
// started 8 August 2005
// played its first game 9 September 2005

import java.io.*;

/**
 * This class implements the <code>InputInterface</code> for use when the game is played
 * from a text console such as a shell.
 */
class ConsoleInterface implements IOInterface {

	private Engine engine;
	private Position theBoard;
	private boolean thinking = false;
	private boolean exiting = false;

	public void Finished(double _timeTaken, int _movesCalculated, Move _move, int _nodesPerSecond) {
		thinking = false;
		System.out.printf ("total time = %.0f, total nodes = %d (%d nodes/second)\n", _timeTaken, _movesCalculated, _nodesPerSecond);
	}
	
	public ConsoleInterface (Engine _engine) {
		engine = _engine;
		theBoard = engine.theBoard;
	}
	
	public void Start() {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("ANIMATS CHESS ENGINE");
		System.out.println("(run with the command-line argument 'swing' for a graphical interface)\n");

		UpdateDisplay(engine.GetCurrentPosition());
		
		do {
			if (AnimatsChess.player[theBoard.CurrentPlayer()].computer && thinking == false) {
				// The last command typed was a legal human move, it is now the computers
				// turn to start thinking and make a move.
				thinking = true;
				// This flag is set to indicate that the engine has started thinking.
				// Other commands can now me issued while the engine is thinking without
				// triggering the thinking process to start again.
				System.out.println("\nanalysing position...");
				engine.StartThinking(false);
			} else { 
				try {
					if (!thinking) {
						System.out.print(Resources.englishColour[theBoard.CurrentPlayer()] + " to move (? for help) >");
					}
					String inputLine = stdin.readLine();
					if (inputLine.equals("quit") || inputLine.equals("resign")) {
						engine.Quit();
						exiting = true;
					} else if (inputLine.equals("off")) {
						System.out.println("debugging turned off\n");
						engine.debug = 0;
					} else if (inputLine.equals("terse")) {
						System.out.println("debugging set to terse\n");
						engine.debug = Engine.TERSE;
					} else if (inputLine.equals("normal")) {
						System.out.println("debugging set to normal\n");
						engine.debug = Engine.NORMAL;
					} else if (inputLine.equals("verbose")) {
						System.out.println("debugging set to verbose\n");
						engine.debug = Engine.VERBOSE;
					} else if (inputLine.equals("go")) {
						// Set the engine to start playing for the player whose turn it is.
						if (engine.theBoard.getWhoseTurn() == Resources.WHITE) {
							AnimatsChess.player[Resources.WHITE].computer = true;
							AnimatsChess.player[Resources.BLACK].computer = false;
						} else {
							AnimatsChess.player[Resources.BLACK].computer = true;
							AnimatsChess.player[Resources.WHITE].computer = false;
						}
					} else if (inputLine.equals("hint")) {
						thinking = true;
						// This flag is set to indicate that the engine has started thinking.
						// Other commands can now me issued while the engine is thinking without
						// triggering the thinking process to start again.
						System.out.println("\nanalysing position...");
						// Pass 'true' to indicated that the move should not be made
						// after analysis is complete
						engine.StartThinking(true);
					} else if (inputLine.equals("white")) {
						engine.theBoard.setWhoseTurn(Resources.WHITE);
						AnimatsChess.player[Resources.BLACK].computer = true;
						AnimatsChess.player[Resources.WHITE].computer = false;
					} else if (inputLine.equals("black")) {
						engine.theBoard.setWhoseTurn(Resources.BLACK);
						AnimatsChess.player[Resources.WHITE].computer = true;
						AnimatsChess.player[Resources.BLACK].computer = false;
					} else if (inputLine.equals("force")) {
						System.out.println("force mode on\n");
						AnimatsChess.player[Resources.BLACK].computer = false;
						AnimatsChess.player[Resources.WHITE].computer = false;
					} else if (inputLine.equals("norandom")) {
						System.out.println("moves will not be shuffled before searching\n");
						engine.random = false;
					} else if (inputLine.equals("random")) {
						System.out.println("moves now shuffled before searching\n");
						engine.random = true;
					} else if (inputLine.equals("result")) {
						Message(inputLine);
					} else if (thinking == true) {
						// NO COMMAND AFTER THIS LINE CAN BE PERFORMED WHILE THE ENGINE IS THINKING
						System.out.println("can only quit while engine is thinking\n");
					} else if (inputLine.equals("moves")) 
						DisplayLegalMoves();
					else if (inputLine.equals("material")) {
						System.out.println("white material: " + engine.GetCurrentPosition().totalMaterial[Resources.WHITE] + ", black matierial: " + engine.GetCurrentPosition().totalMaterial[Resources.BLACK]);
						} else if (inputLine.equals("board")) 
						UpdateDisplay(engine.GetCurrentPosition());
					else if (inputLine.equals("restart") || inputLine.equals("new")) {
						engine.Reset();
						UpdateDisplay(engine.GetCurrentPosition());
					} else if (inputLine.equals("sheet") || inputLine.equals("scoresheet")) {
        				for (Move move : theBoard.getScoreSheet().getArrayList()) {
        					//   Display the move that was played.
        					if (move.madeBy == Resources.WHITE)
								System.out.print(move.ScoreSheetAlgebraic());
							else {
								System.out.print("\t");
								System.out.print(move.ScoreSheetAlgebraic());
								System.out.print("\n");
							}
						}
						System.out.print("\n");
					} else if (inputLine.equals("?") || inputLine.equals("help")) {
						System.out.println("moves     to display legal moves");
						System.out.println("board     to display the board");
						System.out.println("sheet     to display the scoresheet");
						System.out.println("material  to display the material tally");
						System.out.println("terse     to display level one debugging");
						System.out.println("normal    to display level two debugging");
						System.out.println("verbose   to display level three debugging");
						System.out.println("off       to turn off debugging");
						System.out.println("restart   to restart the game");
						System.out.println("quit      to exit the program");
						System.out.println("\nmoves are entered by typing the rank and file to move from followed by the rank and file to move to, eg: e2e4\n");
					} else if (engine.IsLegalMove(inputLine) != null) {
						engine.HumanMove(inputLine);
					} else {
						if (!inputLine.equals(""))
							System.out.println(inputLine + " is not a legal command or move\n");
					}
    				} catch ( IOException error ) {
        				System.err.println( "error reading stdin: " + error );
    				}
			}
		// Check if the last command typed has ended the game either by resigning or winning
		// and keeping looping for more user input if the game is still active.
		} while (engine.theBoard.getGameState().ordinal() < Move.GameState.EXITING.ordinal() && exiting == false);

		// Wake the engine up so it can shutdown too.
		synchronized (engine.engineLock) {
			engine.engineLock.notifyAll();
		}
	}

	/**
	 * This method is called when an informational text string
	 * about the engine's thinking is to be displayed.
	 */
	public void Thinking(int _ply, int _evaluation, double _time, int _nodes, String _line){
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
		System.out.println(output.toString());
	}

	private void ColumnPadding(StringBuffer _buffer, int _column) {
		while (_buffer.length() < _column)
			_buffer.append(' ');
	}

	/**
	 * This method is called when an informational text string
	 * is to be displayed.
	 */
	public void Message(String _message){
		System.out.println(_message);
	}

	public void DisplayResult() {
		if (engine.theBoard.getGameState() == Move.GameState.WHITE_CHECKMATED)
			System.out.println("checkmate: 0-1");
		if (engine.theBoard.getGameState() == Move.GameState.BLACK_CHECKMATED)
			System.out.println("checkmate: 1-0");
		if (engine.theBoard.getGameState() == Move.GameState.WHITE_STALEMATED || engine.theBoard.getGameState() == Move.GameState.BLACK_STALEMATED)
			System.out.println("stalemate: 1/2 - 1/2");
	}

	final static String frame = "    +-+-+-+-+-+-+-+-+";
	
	/**
	 * This method displays a list of legal moves that can be made from 
	 * the current <code>Position</code> in the game.
	 */
	private void DisplayLegalMoves() {
		System.out.println("legal moves for " + Resources.englishColour[engine.theBoard.getWhoseTurn()] + ":"); 
		for (Move move : engine.getImmediateMoves())
			System.out.println("   " + move);
		System.out.println();
	}

	public void SuggestedMove (Move _move) {
		// Display the move that is being suggested.
		System.out.println("\nsuggest playing " + _move.Algebraic() + "\n");
	}
	
	public void MoveMade (Move _move) {
		// Display the move that was played.
		System.out.println("\n" + _move.NumberedAlgebraic());
			
		UpdateDisplay(engine.theBoard);
		DisplayResult();
	}
	
	/**
	 * This method takes a <code>Position</code> object and display it
	 * as an ASCII representation of the board.
	 */
	private void UpdateDisplay (Position _theBoard) {
		int rank = 0;
		int file = 0;

		System.out.println(frame);
		for (rank = 7; rank > -1; rank--) {
			System.out.print("  " + Move.rankNumber[rank] + " |");
			for (file = 0; file < 8; file++) {
				if (_theBoard.getPieceAt(rank, file) == null) {
					if ((rank - file) % 2 != 0)
						System.out.print(" ");
					else
						System.out.print("#");
				} else {
					if (_theBoard.getPieceAt(rank, file).colour == Resources.WHITE) {
						if (_theBoard.getPieceAt(rank, file) instanceof Position.Pawn)
							System.out.print("P"); 
						else if (_theBoard.getPieceAt(rank, file) instanceof Position.Rook)
							System.out.print("R"); 
						else if (_theBoard.getPieceAt(rank, file) instanceof Position.Knight)
							System.out.print("N"); 
						else if (_theBoard.getPieceAt(rank, file) instanceof Position.Bishop)
							System.out.print("B"); 
						else if (_theBoard.getPieceAt(rank, file) instanceof Position.Queen)
							System.out.print("Q"); 
						else
							System.out.print("K"); 
					} else {
						if (_theBoard.getPieceAt(rank, file) instanceof Position.Pawn)
							System.out.print("p"); 
						else if (_theBoard.getPieceAt(rank, file) instanceof Position.Rook)
							System.out.print("r"); 
						else if (_theBoard.getPieceAt(rank, file) instanceof Position.Knight)
							System.out.print("n"); 
						else if (_theBoard.getPieceAt(rank, file) instanceof Position.Bishop)
							System.out.print("b"); 
						else if (_theBoard.getPieceAt(rank, file) instanceof Position.Queen)
							System.out.print("q"); 
						else
							System.out.print("k"); 
					}
				}
				System.out.print("|");
			}

			System.out.println("");
			System.out.println(frame);
		}
		System.out.println ("\n     a b c d e f g h \n");
	}	
}
