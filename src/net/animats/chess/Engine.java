package net.animats.chess;

import java.util.*;

class Engine extends Thread {

	// Constants to specify the search depth.
	private final int LOOK_AHEAD_PLY = 5;
	private final int MAXIMUM_DEPTH = 6;

	private int requestedDepth;
	private int maxSearchDepth;

	// This Position object represents the board in its current state.
	Position theBoard = new Position();
	
	// Debugging levels.
	static final int OFF = 0;
	static final int TERSE = 1;
	static final int NORMAL = 2;
	static final int VERBOSE = 3;

	// Debugging mode.
	int debug = TERSE;
	
	// This boolean is used to stop the engine during the analysis of a position.
	private boolean interrupted = false;
	private boolean exiting = false;

	// This boolean indicates whether the moves from the starting position should be shuffled before
	// being evaluated.
	boolean random = true;

	private boolean thinking = false;

	// This is set to TRUE if the engine is only providing a hint, 
    // not making a move.
	private boolean analysis_only = false;

	Object engineLock = new Object();

	// Store some statistics about the analysis.
	private int movesCalculated;
	private int rootMoveTotal;
	private double startTime;
	private double movesPerSecond = 0;
		
	// This field represents the moves to analyse...
	private ArrayList<Move> immediateMoves;
	
	// ...and the resulting best move.
	private SearchResult bestMove;
	
	// Wakes up the Engine thread to do the processing.
	public void StartThinking(Boolean _analysis_only) {
		synchronized (engineLock) {
			thinking = true;
			analysis_only = _analysis_only;
			engineLock.notifyAll();
		}
	}

	public void Quit() {
		interrupted = true;
		exiting = true;
	}
	
	// Called when the superclass 'Thread' has its start() method invoked.
    public void run() {
		try {
			synchronized (engineLock) {
				while (theBoard.getGameState().ordinal() < Move.GameState.EXITING.ordinal() && exiting == false) {
					if (thinking) {
						double timeTaken = Analyse();

						Move chosenMove = ComputerMove();

						AnimatsChess.userInterface.Finished(timeTaken, movesCalculated, chosenMove, (int) movesPerSecond);

						if (analysis_only) {
							// Notify the interface of the suggested move.
							AnimatsChess.userInterface.SuggestedMove(chosenMove);
						} else {
							// If a move was made (not just analysis, notify the interface)
							AnimatsChess.userInterface.MoveMade(chosenMove);
						}

						thinking = false;
					}
					engineLock.wait();
				}
			}
		} catch (InterruptedException _exception) {
			System.out.println("interrupted");
		}
	}

	// Attempts to update the current position with the supplied move. 
    // Returns FALSE if move not possible.
	public Move HumanMove(String _move) {
		Move move = IsLegalMove(_move);
		if (move != null) {
			theBoard.MakeMove(move);
			theBoard.SetLastDestination(move);
			AnimatsChess.userInterface.MoveMade(move);
		}

		// If the game isn't over, calculate the possible moves the opponent can make.
		if (theBoard.getGameState().ordinal() < Move.GameState.EXITING.ordinal())
			immediateMoves = theBoard.DetermineLegalMoves();

		return move;
	}

	public Move ComputerMove() {
		// Choose one of the moves and make it.
		Move chosenMove = null;
		for (Move move : immediateMoves) {
			if (move.result.evaluation == bestMove.evaluation) {
				if (analysis_only == false) {
					theBoard.MakeMove(move);
					theBoard.SetLastDestination(move);
				}

				chosenMove = move;
				break;
			}
		}
	
		if (analysis_only == false) {
			// If the game isn't over, calculate the possible moves the 
			// opponent can make. If analysis_only == false, the turn hasn't 
			// changed, so skip this step.
			if (theBoard.getGameState().ordinal() < Move.GameState.EXITING.ordinal()) 
				immediateMoves = theBoard.DetermineLegalMoves();
		}

		return chosenMove;
	}

	// This function analyses this position to the specified depth.
	private double Analyse() {
		// Clear the results of any previous analysis.
		double endTime = 0;
		movesCalculated = 0;

		startTime = (double) System.currentTimeMillis();
		movesPerSecond = 0;

		if (random)
			Shuffle();

		// This loop implements iterative deepening. The facility to look 
		// further ahead  if the last move was a take is disabled until the 
		// final search depth is reached.
		for (int index = LOOK_AHEAD_PLY - 1; index > 0; index--) {
			requestedDepth = LOOK_AHEAD_PLY - index;
			maxSearchDepth = requestedDepth;
			bestMove = BuildTree(requestedDepth, -Resources.INFINITY - 1, +Resources.INFINITY + 1);
			SortMoves();
		}

		// For the last search, enable the extra look-ahead based on moves 
		// that take a piece.
		requestedDepth = LOOK_AHEAD_PLY;
		maxSearchDepth = MAXIMUM_DEPTH;
		bestMove = BuildTree(requestedDepth, -Resources.INFINITY - 1, +Resources.INFINITY + 1);

		endTime = (double) System.currentTimeMillis();
		endTime -= startTime;
		if ( endTime / (double) 1000 != 0)
			movesPerSecond = movesCalculated / (endTime / (double) 1000);
		else 
			movesPerSecond = (double) movesCalculated;

		return endTime;
	}

	// This function builds a tree of legal moves to the depth of the specified ply. It returns the
	// position evaluations on its way back down the tree after constructing it.
	private SearchResult BuildTree(int _ply, int _alpha, int _beta) {
		// If the interface is exiting, stop thinking.
		if (interrupted)
			return new SearchResult(0, 0);

		if (theBoard.getGameState() == Move.GameState.BLACK_CHECKMATED) 
			return new SearchResult(+Resources.INFINITY, 0);
		if (theBoard.getGameState() == Move.GameState.WHITE_CHECKMATED)
			return new SearchResult(-Resources.INFINITY, 0);
		if (theBoard.getGameState() == Move.GameState.WHITE_STALEMATED || theBoard.getGameState() == Move.GameState.BLACK_STALEMATED)
			return new SearchResult(0, 0);

		if ((_ply < 1 && ((Move) theBoard.getScoreSheet().peek()).pieceTaken == null) || _ply <= (requestedDepth - maxSearchDepth)) {
			// This is a leaf node and the last move didn't involve a take so set the evaluation to 
			// the figure arrived at by the Evaluate function.
			return new SearchResult(theBoard.Evaluate(), 0);
		}
		
		// This is not a leaf node, so loop through all the possible moves from this position.
		_ply--;

		ArrayList<Move> legalMoves;

		if (_ply == requestedDepth - 1)
			legalMoves = immediateMoves;
		else
			legalMoves = theBoard.DetermineLegalMoves();

		movesCalculated += legalMoves.size();
		rootMoveTotal += legalMoves.size();
		
		SearchResult result = null;

		if (theBoard.getWhoseTurn() == Resources.WHITE) {
			result = new SearchResult(_alpha, 0);
			for (Move move : legalMoves) {
				if (_ply == requestedDepth - 1) rootMoveTotal = 0;
				theBoard.MakeMove(move);
				move.result = BuildTree(_ply, _alpha, _beta);

				if (move.result.evaluation > _alpha) {
					// This is a new best move.
					_alpha = move.result.evaluation;
					result.evaluation = _alpha;
					result.searchDepth = move.result.searchDepth + 1;
					result.leadsTo = move;
					
					if (_ply == requestedDepth - 1 && debug != 0) {
						// This is the top level, so make this the new best move the 
						// player can make so far.
						double endTime = (double) System.currentTimeMillis();
						endTime -= startTime;
						AnimatsChess.userInterface.Thinking(result.searchDepth, _alpha, endTime , rootMoveTotal, Thinking(move));
					}
				} else if (_ply == requestedDepth - 1 && debug == NORMAL) {
					double endTime = (double) System.currentTimeMillis();
					endTime -= startTime;
					AnimatsChess.userInterface.Thinking(result.searchDepth, move.result.evaluation, endTime , rootMoveTotal, Thinking(move));
				}
				
				theBoard.UndoMove();
				
				if (_alpha >= _beta) 
					break;
			}
			return result;
		} else {
			result = new SearchResult(_beta, 0);
			for (Move move : legalMoves) {
				if (_ply == requestedDepth - 1) rootMoveTotal = 0;
				theBoard.MakeMove(move);
				move.result = BuildTree(_ply, _alpha, _beta);

				if (move.result.evaluation < _beta) {
					// This is a new best move.
					_beta = move.result.evaluation;
					result.evaluation = _beta;
					result.searchDepth = move.result.searchDepth + 1;
					result.leadsTo = move;

					if (_ply == requestedDepth - 1 && debug != 0) {
						// This is the top level, so make this the new best move the 
						// player can make so far.
						double endTime = (double) System.currentTimeMillis();
						endTime -= startTime;
						AnimatsChess.userInterface.Thinking(result.searchDepth, _beta, endTime , rootMoveTotal, Thinking(move));
					}

				} else if (_ply == requestedDepth - 1 && debug == NORMAL) {
					double endTime = (double) System.currentTimeMillis();
					endTime -= startTime;
					AnimatsChess.userInterface.Thinking(result.searchDepth, move.result.evaluation, endTime , rootMoveTotal, Thinking(move));
				}
				
				theBoard.UndoMove();
				if (_alpha >= _beta)
					break;
			}
			return result;
		}
	}

	private String Thinking(Move _firstMove) {
		StringBuilder principalVariation = new StringBuilder();
		
		if (_firstMove.madeBy == Resources.BLACK)
			principalVariation.append(Integer.toString(_firstMove.moveNumber) + "...");
		
		Move move = _firstMove;
		
		do {
			if (move.madeBy == Resources.WHITE) {
				principalVariation.append(" ");
				principalVariation.append(Integer.toString(move.moveNumber));
				principalVariation.append(". ");
			} else
				principalVariation.append(" ");
			
			principalVariation.append(move.Algebraic());
			
			move = move.result.leadsTo;
		} while (move != null);
		
		return principalVariation.toString();
		
	}
	private void Shuffle() {
		Collections.shuffle(immediateMoves);
	}

	/**
	 * This method sorts the immediate moves that can be makefrom this position into an order based on their 
	 * evaluations. This is done to speed up alpha-beta pruning.
	 */
	private void SortMoves() {
		if (immediateMoves != null) {
			Collections.sort(immediateMoves);
			
			if (debug == VERBOSE) {
				System.out.println("----------------------------");
				
				for (Move move : immediateMoves)
					System.out.println(move.Algebraic() + " " + move.result.evaluation);
				
				System.out.println("----------------------------");
			}
			// For the white player, higher evaluations are better so reverse the list resulting
			// in a highest to lowest sort order.
			if (theBoard.getWhoseTurn() == Resources.WHITE)
				Collections.reverse(immediateMoves);
		} else
			System.out.println("no immediate moves to sort");

	}
		
	// This method returns true if the supplied move can be legally made from the current position.
	public Move IsLegalMove(String _move) {
		for (Move move : immediateMoves) {
			if (_move.equals(move.AsCommand()))
				return move;
		}

		return null;
	}

	// This method just passes the call on to the Position object.
	public void Reset() {
		theBoard.Reset();
		DetermineImmediateMoves();
	}

	public ArrayList<Move> getImmediateMoves() {
		return immediateMoves;
	}
	
	public void DetermineImmediateMoves() {
		immediateMoves = theBoard.DetermineLegalMoves();
	}
	
	public Position GetCurrentPosition() {
		return theBoard;
	}

	public Engine(String _name) {
		super(_name);
		DetermineImmediateMoves();
	}

}
