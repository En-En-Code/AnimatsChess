package net.animats.chess;
// Animats Chess Engine
// started 8 August 2005
// played its first game 9 September 2005

import java.io.*;

/**
 * This class implements the <code>InputInterface</code> for use when the game is played
 * from a text console such as a shell.
 */
class XBoardInterface implements IOInterface {

	private Engine engine;
	private Position theBoard;
	private boolean thinking = false;
	private boolean exiting = false;

	public void Finished(double _timeTaken, int _movesCalculated, Move _move, int _nodesPerSecond) {
		thinking = false;
		System.out.println("move " + _move.AsCommand());
	}

	public XBoardInterface (Engine _engine) {
		engine = _engine;
		theBoard = engine.theBoard;
	}
	
	public void Start() {

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		try {
			// Strip off the leading 'xboard' as this has already been handled 
			// by the command line argument.
			String inputLine = stdin.readLine();
			if (inputLine.equals("xboard")) {
				inputLine = stdin.readLine();
				if (inputLine.equals("protover 2")) {
					System.out.println("feature reuse=0 sigint=0 sigterm=0 draw=0 myname=\"Animats\" done=1");
				}
			} else {
				exiting = true;
			}

			System.out.println("Animats Chess Engine by Stuart Allen.");

			while (exiting == false) {
				if (AnimatsChess.player[theBoard.CurrentPlayer()].computer && thinking == false && engine.theBoard.getGameState().ordinal() < Move.GameState.EXITING.ordinal()) {
					thinking = true;
					engine.StartThinking(false);
				} else { 
					inputLine = stdin.readLine();
					if (inputLine.equals("quit")) {
						engine.Quit();
						exiting = true;
					} else if (inputLine.equals("off")) {
						engine.debug = 0;
					} else if (inputLine.equals("terse")) {
						engine.debug = Engine.TERSE;
					} else if (inputLine.equals("normal")) {
						engine.debug = Engine.NORMAL;
					} else if (inputLine.equals("verbose")) {
						engine.debug = Engine.VERBOSE;
					} else if (inputLine.equals("force")) {
						AnimatsChess.player[Resources.BLACK].computer = false;
						AnimatsChess.player[Resources.WHITE].computer = false;
					} else if (inputLine.equals("norandom")) {
						engine.random = false;
					} else if (inputLine.equals("random")) {
						engine.random = true;
					} else if (thinking == true || engine.theBoard.getGameState().ordinal() > Move.GameState.UNKNOWN.ordinal()) {
					// NO COMMAND AFTER THIS LINE CAN BE PERFORMED WHILE THE ENGINE IS THINKING
					} else if (inputLine.equals("go")) {
						// Set the engine to start playing for the player whose turn it is.
						if (engine.theBoard.getWhoseTurn() == Resources.WHITE) {
							AnimatsChess.player[Resources.WHITE].computer = true;
							AnimatsChess.player[Resources.BLACK].computer = false;
						} else {
							AnimatsChess.player[Resources.BLACK].computer = true;
							AnimatsChess.player[Resources.WHITE].computer = false;
						}
					} else if (inputLine.equals("white")) {
						engine.theBoard.setWhoseTurn(Resources.WHITE);
						AnimatsChess.player[Resources.BLACK].computer = true;
						AnimatsChess.player[Resources.WHITE].computer = false;
					} else if (inputLine.equals("black")) {
						engine.theBoard.setWhoseTurn(Resources.BLACK);
						AnimatsChess.player[Resources.WHITE].computer = true;
						AnimatsChess.player[Resources.BLACK].computer = false;
					} else if (inputLine.equals("restart")) {
						engine.Reset();
					} else if (engine.IsLegalMove(inputLine) != null)
						engine.HumanMove(inputLine);
				}
			}

			// Wake up the engine so it can exit too.
			synchronized (engine.engineLock) {
				engine.engineLock.notifyAll();
			}
		
		} catch ( IOException error ) {
        		System.err.println( "error reading stdin: " + error );
    		}
	}
	/**
	 * This method is called when an informational text string
	 * regarding the engine's thinking is to be displayed.
	 */
	public void Thinking(int _ply, int _evaluation, double _time, int _nodes, String _line){
		System.out.println("" + _ply + " " + _evaluation + " " + (long) _time + " " + _nodes + " " + _line);
	}

	/**
	 * This method is called when an informational text string
	 * is to be displayed.
	 */
	public void Message(String _message){
		System.out.println("telluser " + _message);
	}

	/**
	 * This method is called when the board has changed and
	 * the display needs to be updated to relfect the change.
	 * It takes a single parameter that is a reference to a
	 * <code>Position</code> object representing the new
	 * state of the board.
	 */
	public void MoveMade(Move _move) {
	}

	public void SuggestedMove(Move _move) {
	}
}



