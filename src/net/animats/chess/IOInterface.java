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
