package net.animats.chess;
/**
 * This class represents each of the players: black and white.
 */

class Player {
	public String name;
	public int colour;

	boolean computer;

	/** 
	 * This constructor takes the players name as a string and a boolean 
     * indicating whether the engine is to make moves for this player.
	 */
	Player (String _name, boolean _computer) {
		name = _name;
		computer = _computer;
	}

	
}
