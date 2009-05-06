package net.animats.chess;

// Animats Chess Engine
// started 8 August 2005
// played its first game 9 September 2005

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
