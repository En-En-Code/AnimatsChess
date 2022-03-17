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

class AnimatsChess {
	// Create the player objects.
	static final Player[] player = new Player[2];

	// Create a Engine to interface with.
	static final Engine engine = new Engine("AnimatsChessEngine");
	
	// The class used to interpret all player input;
	static IOInterface userInterface;

	public static void main(String[] _arguments) {
		player[Resources.BLACK] = new Player("Computer", true);
		player[Resources.WHITE] = new Player("Human", false);

		if (_arguments.length == 0)
			userInterface = new ConsoleInterface(engine);
		else if (_arguments[0].equals("xboard"))
			userInterface = new XBoardInterface(engine);
		else if (_arguments[0].equals("swing"))
			userInterface = new SwingInterface(engine);
		else 
			System.out.println("unknown interface: " + _arguments[0]);

		// Start the engine (creating its own thread of execution)
		engine.start();
                
		// Control will return straight away for this thread to run the user interface/
		userInterface.Start();
	}
}
