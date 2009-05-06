package net.animats.chess;
// Animats Chess Engine

import java.util.*;

// This class represents a game position. It is contains an 8 x 8 
// two-dimensional array of Piece objects. Each element object holds a 
// pointer to the piece on it, or null if it is vacant.

class Position {
	// Store a reference to a rook, knight, bishop and king for the purpose 
    // of determining if a player is in check.
	private Piece[] moveablePiece = new Piece[4];

	// This is the array that represents the board.
	private Piece[][] squares = new Piece[8][8];

	// This is a list of all the moves that lead to this position.
	private MoveStack scoreSheet;
	
	// This object is a convenience reference to the move that directly 
    // lead to this position.
	private Move move;

	// This is set to either Resources.BLACK or Resources.WHITE to indicate 
    // whose turn it is to move from this position.
	private int whoseTurn;

	private int movesSinceLastTake = 0;

	boolean[] castled = new boolean[2];

	// Store the current position of each king to make determining whether 
    // either player is in check faster as this is done regularly.
	public int kingRank[] = new int[2];
	public int kingFile[] = new int[2];

	// Store the total value of each player's pieces for each given position 
    // to allow the position to be evaluated.
	int totalMaterial[] = new int[2];
	
	// This modifier is used to multiply any penalties to make them push the 
    // evaluation in the right direction: up for black (to white's advantage), 
    // down for white (to black's advantage).
	// This array is indexed by Resources.BLACK and Resources.WHITE
	private static final int penaltyModifier[] = { 1, -1 };
	private static final int bonusModifier[] = { -1, 1 };
	
	private int lastDestinationRank = 42;
	private int lastDestinationFile = 42;
	
	void SetLastDestination(Move _move) {
		lastDestinationRank = _move.newRank;
		lastDestinationFile = _move.newFile;
	}
	
	public MoveStack getScoreSheet() {
		return scoreSheet;
	}
	
	public int getWhoseTurn() {
		return whoseTurn;
	}

	public void setWhoseTurn(int _side) {
		whoseTurn = _side;
	}
	
	public int getMovesSinceLastTake() {
		return movesSinceLastTake;
	}
	
	public Piece getPieceAt(int _rank, int _file) {
		return squares[_rank][_file];
	}
	
	boolean IsLastDestination(int _rank, int _file) {
		if (_rank == lastDestinationRank && _file == lastDestinationFile)
			return true;
		else
			return false;
	}

	// Return the game state of this position.
	Move.GameState getGameState() {
		if (move != null)
			return move.stateAfterMove;
		else
			return Move.GameState.IN_PROGRESS;
	}
	
	int CurrentPlayer() {
		return whoseTurn;
	}

	int OtherPlayer() {
		if (whoseTurn == Resources.BLACK)
			return (Resources.WHITE);
		else
			return (Resources.BLACK);
	}

	private void UpdateResult() {
		if (InCheck(whoseTurn) == true) {
			if (CanMove() == false) {
				if (whoseTurn == Resources.BLACK)
					move.stateAfterMove = Move.GameState.BLACK_CHECKMATED;
				else
					move.stateAfterMove = Move.GameState.WHITE_CHECKMATED;
			} else {
				if (whoseTurn == Resources.BLACK)
					move.stateAfterMove = Move.GameState.BLACK_IN_CHECK;
				else
					move.stateAfterMove = Move.GameState.WHITE_IN_CHECK;
			}
		} else {
			if (CanMove() == false) {
				if (whoseTurn == Resources.BLACK)
					move.stateAfterMove = Move.GameState.BLACK_STALEMATED;
				else
					move.stateAfterMove = Move.GameState.WHITE_STALEMATED;
			} else
				move.stateAfterMove = Move.GameState.IN_PROGRESS;
		}
	}
		
	/**
	 * Returns true if a legal move can be made from this position, false if not.
	 */
	private boolean CanMove() {
		// This method uses a skeleton version of the code from DetermineLegalMoves to find if the
		// player has any legal moves they can make.
		for (int rank = 0; rank < 8; rank++) {
			for (int file = 0; file < 8; file++) {
				if (squares[rank][file] != null && squares[rank][file].colour == whoseTurn) {
					ArrayList<Move> singlePieceMoves = squares[rank][file].Moves(this, rank, file);
					if (singlePieceMoves.size() != 0) {
						return true;
					}
				}
			}
		}

		return false;
	}

	// Determines all the legal moves that the player to move can make from this position.
	public ArrayList<Move> DetermineLegalMoves() {
		ArrayList<Move> legalMoves = new ArrayList<Move>();

		Piece currentPiece;
		for (int rank = 0; rank < 8; rank++) {
			for (int file = 0; file < 8; file++) {
				currentPiece = squares[rank][file];
				if (currentPiece != null && currentPiece.colour == whoseTurn) {
					ArrayList<Move> singlePieceMoves = currentPiece.Moves(this, rank, file);
					if (singlePieceMoves.size() != 0) {
						legalMoves.addAll(singlePieceMoves);
					}
				}
			}
		}

		// Loop through all the moves looking for ambiguous references.
		for (Move currentMove : legalMoves) {
			if (!(currentMove.pieceMoved instanceof King) && 
				!(currentMove.pieceMoved instanceof Pawn)) {
				for (Move innerMove : legalMoves) {
					if (innerMove != currentMove &&
						innerMove.newRank == currentMove.newRank &&
						innerMove.newFile == currentMove.newFile &&
						innerMove.pieceMoved.type == currentMove.pieceMoved.type) {
							// This move is an ambiguous move.
							if (innerMove.oldFile != currentMove.oldFile) {
								currentMove.disambiguationOnFile = true;
							} else {
								currentMove.disambiguationOnRank = true;
							}
					}
				}
			}
		}

		return legalMoves;
	}

	// Below how many moves do we apply the evaluations for the opening phase of the game
	static final int END_OF_OPENING = 10;
	
	// These are the constants used by the Position.Evaluate method as penalties.
	static final int DOUBLED_PAWN = 25;
	static final int ISOLATED_PAWN = 25;
	static final int MINOR_PIECE_NOT_MOVED = 25;

	// These are the constants used by the Position.Evaluate method as bonuses.
	static final int CASTLED = 50;
	static final int CENTRE_SQUARE_OCCUPIED = 50;

	/**
	 * This method evaluates the Position and sets its evaluation field accordingly.
	 */

	public int Evaluate() {
		// Start by setting the evaluation to the difference in material.
		int evaluation = totalMaterial[Resources.WHITE] - totalMaterial[Resources.BLACK];
		
		// These fields store the number of doubled and tripled pawns each colour has in this position.
		int multiplePawns[] = { 0, 0 };
		int minorPiecesNotMoved[] = { 0, 0 };

		int pawnsInFile[][] = new int[2][8];

		// Do a scan of the board and store a bit of information...
		Piece currentPiece;
		for (int file = 0; file < 8; file++) {
			for (int rank = 0; rank < 8; rank++) {
				currentPiece = squares[rank][file];	
				if (currentPiece != null) {
					// Count the number of pawns in each file.
					if (currentPiece instanceof Pawn) {
						pawnsInFile[currentPiece.colour][file]++;
						if (pawnsInFile[currentPiece.colour][file] > 1)
							multiplePawns[currentPiece.colour]++;
					}
					
					// Count how many minor pieces each colour has that haven't moved.
					if (currentPiece instanceof Bishop || currentPiece instanceof Knight) {
						if (currentPiece.moveCount == 0)
							minorPiecesNotMoved[currentPiece.colour]++;
					}
				}
			}
		}
		
		// Modify the evaluation to reflected the appropriate penalties.
		int penalty = 0;
		int bonus = 0;
		
		for (int colour = 0; colour < 2; colour++) {
			// Calculate the penalties for doubled and tripled pawns.
			penalty = multiplePawns[colour] * DOUBLED_PAWN;

			// Determine if any pawns are isolated.
			for (int file = 0; file < 8; file++) {
				if (pawnsInFile[colour][file] != 0) {
					// This colour has pawns in this file, does it have any neighbours?
					if (((file == 0) || (pawnsInFile[colour][file - 1] == 0 ))
							&& ((file == 7) || (pawnsInFile[colour][file + 1] == 0))) {
						// This file has at least one isolated pawn.
						penalty += ISOLATED_PAWN;
					}
				}		
			}
				
			// If a player hasn't moved each of their pieces by the end of the opening, apply a penalty.
			if (scoreSheet.size() < END_OF_OPENING) 
				penalty += minorPiecesNotMoved[colour] * MINOR_PIECE_NOT_MOVED;

			if (castled[colour] == false && squares[kingRank[colour]][kingFile[colour]].moveCount != 0)
				penalty += CASTLED;
			
			// The direction the evaluation should move (+ or -) is 
			penalty *= penaltyModifier[colour];
			evaluation += penalty;

			// Modify the evaluation to reflect the appropriate bonuses.
			
			// Castling is a good thing...
			if (castled[colour])
				bonus = CASTLED;
				
			// Occupation of the centre squares is a good thing.
			if (squares[3][3] != null && squares[3][3].colour == colour) {
				bonus += CENTRE_SQUARE_OCCUPIED;
			}
			if (squares[3][4] != null && squares[3][4].colour == colour) {
				bonus += CENTRE_SQUARE_OCCUPIED;
			}
			if (squares[4][3] != null && squares[4][3].colour == colour) {
				bonus += CENTRE_SQUARE_OCCUPIED;
			}
			if (squares[4][4] != null && squares[4][4].colour == colour) {
				bonus += CENTRE_SQUARE_OCCUPIED;
			}

			// The direction the evaluation should move (+ or -) is 
			bonus *= bonusModifier[colour];
			evaluation += bonus;
		}

		return evaluation;
	}
	
	void Reset () {
		int currentColour = 0;
		int rank = 0;
		int file = 0;

		// Set the following fields back to their initial values.
		whoseTurn = Resources.WHITE;

		movesSinceLastTake = 0;

		scoreSheet = new MoveStack();

		castled[Resources.WHITE] = false;
		castled[Resources.BLACK] = false;
		
		totalMaterial[Resources.WHITE] = 0;
		totalMaterial[Resources.BLACK] = 0;
		
		// Store the current position of both kings for faster determination of
		// whether a move puts the player in check or not.
		kingRank[Resources.WHITE] = 0;
		kingFile[Resources.WHITE] = 4;
		kingRank[Resources.BLACK] = 7;
		kingFile[Resources.BLACK] = 4;
		
		// Set up the pieces on the board.
		for (rank = 0; rank < 8; rank++) {
			// Set the colour for the pieces to be created.
			if (rank == 0 || rank == 1)
				currentColour = Resources.WHITE;
			else if (rank == 6 || rank == 7)
				currentColour = Resources.BLACK;

			for (file = 0; file < 8; file++) {
				// Set as an empty square by default, then add the pieces...
				squares[rank][file] = null;

				// Put all the pieces on the first and eigth ranks. Store a pointer to a 
				// rook, bishop, knight and king for the purpose of determining if the
				// player is in check.
				if (rank == 0 || rank == 7) {
					if (file == 0 || file == 7)
						squares[rank][file] = new Rook(currentColour);

					if (file == 1 || file == 6)
						squares[rank][file] = new Knight(currentColour);

					if (file == 2 || file == 5)
						squares[rank][file] = new Bishop(currentColour);

					if (file == 3)
						squares[rank][file] = new Queen(currentColour);

					if (file == 4) 
						squares[rank][file] = new King(currentColour);
				}

				// Put all the pawns on the second and seventh ranks.
				if (rank == 1 || rank == 6)
					squares[rank][file] = new Pawn(currentColour);
				
				// Increase the total value of the pieces for each player
				if (squares[rank][file] != null && !(squares[rank][file] instanceof King))
					totalMaterial[currentColour] += squares[rank][file].value;
			}
		}
	}
	
	// This method updates the current Position with the supplied move.
	public void MakeMove(Move _move) {
		scoreSheet.push(_move);

		// If the piece being moved is the king, update the cached location.
		if (_move.pieceMoved instanceof King) {
			kingRank[_move.pieceMoved.colour] = _move.newRank;
			kingFile[_move.pieceMoved.colour] = _move.newFile;
		}

		// If the move is castling, move the rook too.
		if (_move.castling == Move.QUEEN_SIDE) {
			squares[_move.newRank][3] = squares[_move.newRank][0];
			squares[_move.newRank][3].moveCount++;
			squares[_move.newRank][0] = null;
			castled[_move.pieceMoved.colour] = true;
		} else if (_move.castling == Move.KING_SIDE) {
			squares[_move.newRank][5] = squares[_move.newRank][7];
			squares[_move.newRank][5].moveCount++;
			squares[_move.newRank][7] = null;
			castled[_move.pieceMoved.colour] = true;
		}

		// If the move is an en passant take, remove the pawn that is taken.
		if (_move.enPassant) {
			squares[_move.oldRank][_move.newFile] = null;
		}

		// Promote the pawn if required and increase the moving players material.
		if (_move.pieceMoved instanceof Pawn && (_move.newRank == 0 || _move.newRank == 7)) {
			squares[_move.newRank][_move.newFile] = new Queen(_move.pieceMoved.colour);	
			totalMaterial[_move.pieceMoved.colour] += 800;
		} else {
			// Move the piece from its old square to the new square.
			squares[_move.newRank][_move.newFile] = _move.pieceMoved;
		}

		// Remove it from it's old square.
		squares[_move.oldRank][_move.oldFile] = null;
		
		// Reduce the opponents totalMaterial tally if a piece is taken.
		// Also record that a piece was taken for adjusting the search depth on takes.
		if (_move.pieceTaken != null)
			totalMaterial[_move.pieceTaken.colour] -= _move.pieceTaken.value;		

		// Set the piece to record the fact that it has now moved at least once.
		_move.pieceMoved.moveCount++;
		
		// Swap the whoseTurn field must be swapped to the other player
		if (whoseTurn == Resources.WHITE) 
			whoseTurn = Resources.BLACK;
		else
			whoseTurn = Resources.WHITE;

		// Set the move that lead to this position.
		move = _move;
		
		// Set the stateAfterMove field of the move object to reflect the game state after this move is made.
		UpdateResult();
	}

	// This method updates the current Position by undoing the last move made.
	public void UndoMove() {
		Move lastMove = scoreSheet.pop();
		
		// If the piece that was moved is the king, update the cached location.
		if (lastMove.pieceMoved instanceof King) {
			kingRank[lastMove.pieceMoved.colour] = lastMove.oldRank;
			kingFile[lastMove.pieceMoved.colour] = lastMove.oldFile;
		}

		// If the move was castling, move the rook back too.
		if (move.castling == Move.QUEEN_SIDE) {
			squares[lastMove.newRank][0] = squares[lastMove.newRank][3];
			squares[lastMove.newRank][0].moveCount--;
			squares[lastMove.newRank][3] = null;
			castled[lastMove.pieceMoved.colour] = false;
		} else if (move.castling == Move.KING_SIDE) {
			squares[lastMove.newRank][7] = squares[lastMove.newRank][5];
			squares[lastMove.newRank][7].moveCount--;
			squares[lastMove.newRank][5] = null;
			castled[lastMove.pieceMoved.colour] = false;
		}
		
		// Demote the pawn if required and decrease the moving players material.
		if (lastMove.pieceMoved instanceof Pawn && (lastMove.newRank == 0 || lastMove.newRank == 7)) {
			totalMaterial[lastMove.pieceMoved.colour] -= 800;
		} 

		// Move the piece from its old square to the new square.
		squares[lastMove.oldRank][lastMove.oldFile] = lastMove.pieceMoved;

		// Replace any taken piece in it's old square.
		if (lastMove.enPassant) {
			squares[lastMove.oldRank][lastMove.newFile] = lastMove.pieceTaken;
			squares[lastMove.newRank][lastMove.newFile] = null;
		} else
			squares[lastMove.newRank][lastMove.newFile] = lastMove.pieceTaken;
		
		// Increase the opponents totalMaterial tally as the piece is replaced.
		if (lastMove.pieceTaken != null) {
			totalMaterial[lastMove.pieceTaken.colour] += lastMove.pieceTaken.value;		
		}

		// Reduce the pieces move count.
		lastMove.pieceMoved.moveCount--;
	
		// Swap the whoseTurn field must be swapped to the other player
		if (whoseTurn == Resources.WHITE) 
			whoseTurn = Resources.BLACK;
		else
			whoseTurn = Resources.WHITE;
		
		// Set the move that lead to this position.
		if (scoreSheet.empty() == false)
			move = scoreSheet.peek();
		else
			move = null;
	}

	// This method determines if the specified move puts the player in check.
	public boolean MoveIntoCheck(Move _move) {
		// Skeleton code from the make move method...
		
		// If the piece being moved is the king, update the cached location.
		if (_move.pieceMoved instanceof King) {
			kingRank[_move.pieceMoved.colour] = _move.newRank;
			kingFile[_move.pieceMoved.colour] = _move.newFile;
		}

		// If the move is castling, move the rook too.
		if (_move.castling == Move.QUEEN_SIDE) {
			squares[_move.newRank][3] = squares[_move.newRank][0];
			squares[_move.newRank][0] = null;
		} else if (_move.castling == Move.KING_SIDE) {
			squares[_move.newRank][5] = squares[_move.newRank][7];
			squares[_move.newRank][7] = null;
		}

		// If the move is an en passant take, remove the pawn that is taken.
		if (_move.enPassant) {
			squares[_move.oldRank][_move.newFile] = null;
		}

		squares[_move.newRank][_move.newFile] = _move.pieceMoved;

		// Remove it from it's old square.
		squares[_move.oldRank][_move.oldFile] = null;
		
		if (SquareAttacked(kingRank[whoseTurn], kingFile[whoseTurn], whoseTurn)) {
			PartialUndoMove(_move);
			return true;
		} else {
			PartialUndoMove(_move);
			return false;
		}
	}

	// This method updates the current Position with the supplied move.
	public void PartialUndoMove(Move _move) {
		// If the piece that was moved is the king, update the cached location.
		if (_move.pieceMoved instanceof King) {
			kingRank[_move.pieceMoved.colour] = _move.oldRank;
			kingFile[_move.pieceMoved.colour] = _move.oldFile;
		}

		// If the move was castling, move the rook back too.
		if (_move.castling == Move.QUEEN_SIDE) {
			squares[_move.newRank][0] = squares[_move.newRank][3];
			squares[_move.newRank][3] = null;
		} else if (_move.castling == Move.KING_SIDE) {
			squares[_move.newRank][7] = squares[_move.newRank][5];
			squares[_move.newRank][5] = null;
		}
		
		// Move the piece from its new square back to the old square.
		squares[_move.oldRank][_move.oldFile] = _move.pieceMoved;

		// Replace any taken piece in it's old square.
		if (_move.enPassant) {
			squares[_move.oldRank][_move.newFile] = _move.pieceTaken;
			squares[_move.newRank][_move.newFile] = null;
		} else
			squares[_move.newRank][_move.newFile] = _move.pieceTaken;
	}

	
	// This method determines if the specified player is currently in check.
	public boolean InCheck(int _colour) {
		return SquareAttacked(kingRank[_colour], kingFile[_colour], _colour);
	}

	// This method determines if the specified square is attacked by the opposite side
	// to the colour specified. What? That's fucked. Well, it kind of makes sense. It's a long story.
	public boolean SquareAttacked(int _rank, int _file, int _colour) {
		int workingRank;
		int workingFile;

		// Loop through the three stored Piece objects: Bishop, Rook and Knight.
		// These three pieces cover all of the moves that can be made by the rest
		// of the pieces (pawns are handled separately.)
		for (Piece testPiece : moveablePiece) {
			// Loop through all of the standard moves this piece can make.
			for (Piece.PieceMove testMove : testPiece.standardMoves) {
				workingRank = _rank;
				workingFile = _file;

				do {
					// Updating the working position with the offsets.
					workingRank += testMove.rankOffset;
					workingFile += testMove.fileOffset;

					// Discard this direction as it has reached the edge of the board.
					if (workingRank < 0 || workingRank > 7 || workingFile < 0 || workingFile > 7)
						break;

					// Check if the destination square has a piece on it.
					if (squares[workingRank][workingFile] != null) {
						if (squares[workingRank][workingFile].colour != _colour) {
							// A piece of the opposite colour has been encountered first
							// in this direction. Determine if the piece is a threat to
							// the square.
							if (testPiece instanceof King
								 && squares[workingRank][workingFile] instanceof King) {
								// The square is attacked by opposite king.
								return true;
							}
							if (testPiece instanceof Knight
								 && squares[workingRank][workingFile] instanceof Knight) {
								// The square is attacked by a knight.
								return true;
							}
							if (testPiece instanceof Bishop &&
								(squares[workingRank][workingFile] instanceof Bishop
								||  squares[workingRank][workingFile] instanceof Queen)) {
									// The square is attacked by a bishop or queen.
									return true;
							}
							if (testPiece instanceof Rook &&
								(squares[workingRank][workingFile] instanceof Rook
								||  squares[workingRank][workingFile] instanceof Queen)) {
									// The square is attacked by a rook or queen.
									return true;
							} else
								break;


						} else {
							// A piece of the same colour is arrived at first when looking
							// in this direction. This direction is therefore not a danger so
							// stop repeating.
							break;
						}
					}	
				} 
				// Keeping repeating if a piece or the edge of the board has not been reached.
				while (testMove.repeatable);
			}
		}
		
		// Pawns must be handled separately as they cannot move backwards 
		// and take differently to the way they move.
		if (_colour == Resources.WHITE) {
			workingRank = _rank + 1;
			workingFile = _file + 1;
			if (workingRank > -1 && workingRank < 8 && workingFile > -1 && workingFile < 8) {
				// If the king is white, the pawn must be black and at [+1,-1] or [+1,+1]
				if (squares[workingRank][workingFile] != null
				&& squares[workingRank][workingFile].colour == Resources.BLACK
				&&  squares[workingRank][workingFile] instanceof Pawn)
					return true;
			}
			workingFile -= 2;
			if (workingRank > -1 && workingRank < 8 && workingFile > -1 && workingFile < 8) {
				if (squares[workingRank][workingFile] != null
				&& squares[workingRank][workingFile].colour == Resources.BLACK
				&&  squares[workingRank][workingFile] instanceof Pawn)
					return true;
			}
		} else {
			workingRank = _rank - 1;
			workingFile = _file + 1;
			if (workingRank > -1 && workingRank < 8 && workingFile > -1 && workingFile < 8) {
				// If the king is black, the pawn must be black and at [-1,-1] or [-1,+1]
				if (squares[workingRank][workingFile] != null
				&& squares[workingRank][workingFile].colour == Resources.WHITE
				&&  squares[workingRank][workingFile] instanceof Pawn)
				return true;
			}
			workingFile -= 2;

			if (workingRank > -1 && workingRank < 8 && workingFile > -1 && workingFile < 8) {
				if (squares[workingRank][workingFile] != null 
				&& squares[workingRank][workingFile].colour == Resources.WHITE
				&&  squares[workingRank][workingFile] instanceof Pawn)
					return true;
			}
		}
		
		// No piece has been found that puts the specified king in check.
		return false;
	}

	public Position() {
		// These pieces are used by Positions to check if a square is being attacked.
		// Create them when the first engine is instantiated.
		moveablePiece[0] = new Rook(Resources.WHITE);
		moveablePiece[1] = new Knight(Resources.WHITE);
		moveablePiece[2] = new Bishop(Resources.WHITE);
		moveablePiece[3] = new King(Resources.WHITE);

		// Setup the initial position of the board.
		Reset();
	}

//	 This abstract class represents the common attributes of all pieces. It is implemented by each
//	 of the concrete piece classes: King, Queen, Rook, Knight, Bishop and Pawn.
	abstract class Piece implements Cloneable {
		public static final int PAWN = 1;
		public static final int KNIGHT = 2;
		public static final int BISHOP = 3;
		public static final int ROOK = 4;
		public static final int QUEEN = 5;
		public static final int KING = 6;

		public final int value;
		public final int colour;
		public final int type;
		public final String fullName;
		public String icon;

		public int moveCount = 0;

		public PieceMove[] standardMoves;
		public boolean moveRepeatable;

		// This abstract method is implented differently by each specific type of piece.
		// It returns a vector of all the pieces possible standard and special moves.
		abstract ArrayList<Move> Moves(Position _theBoard, int _rank, int _file);

		public String toString() {
			return (Resources.englishColour[colour] + " " + fullName);
		}

		public String FullName() {
			return fullName;
		}

		// This method returns a vector of a piece's possible standard moves.
		protected ArrayList<Move> GetStandardMoves(Position _theBoard, int _rank, int _file) {
			ArrayList<Move> possibleStandardMoves = new ArrayList<Move>();

			// Loop through all of the standard moves this piece can make.
			for (PieceMove standardMove : standardMoves) {
				int workingRank = _rank;
				int workingFile = _file;

				boolean keepRepeating = true;

				do {
					// Updating the working position with the offsets.
					workingRank += standardMove.rankOffset;
					workingFile += standardMove.fileOffset;

					// Discard the move and any further repititions if it takes the piece off the board.
					if (workingRank < 0 || workingRank > 7 || workingFile < 0 || workingFile > 7)
						break;

					// Check if the destination square has a piece on it.
					if (_theBoard.squares[workingRank][workingFile] != null) {
						if (_theBoard.squares[workingRank][workingFile].colour == this.colour) {
							// Discard this move and any further repititions as the destination
							// sqaure contains a piece of the same colour.
							break;
						} else {
							// Accept the current move and ignore any further repititions if 
							// this move takes an opposing piece.
							keepRepeating = false;
						}
					}
					
					// Create a new Move represent the this move
					Move newMove = new Move(_rank, _file, workingRank, workingFile, _theBoard);

					// Discard this move and any further repetitions if the move results in the
					// player's king being in check.
					if (_theBoard.MoveIntoCheck(newMove) == false)
						possibleStandardMoves.add(newMove);					

				} 
				// Keeping repeating if the piece can move more than one square and has not yet hit
				// another piece.
				while (standardMove.repeatable && keepRepeating);
				 
			}
			return (possibleStandardMoves);
		}

		public Object clone () throws CloneNotSupportedException {
			return super.clone();
		}

		Piece (int _colour, int _value, int _type, String _fullName) {
			colour = _colour;
	        value = _value;

			type = _type;
			fullName = _fullName;

			if (colour == Resources.BLACK)
				icon = "b";
			else
				icon = "w";
		}

		protected class PieceMove {
			// This class represents the standard way in which a piece can move.
			int rankOffset;
			int fileOffset;
			
			// This flag indicates whether the piece can move multiple times in any 
			// given direction.
			boolean repeatable;

			PieceMove (int _rankOffset, int _fileOffset, boolean _repeatable) {
				rankOffset = _rankOffset;
				fileOffset = _fileOffset;
				repeatable = _repeatable;
			}
		}
	}

	class Pawn extends Piece implements Cloneable {
		Pawn (int _colour) {
			super(_colour, 100, Piece.PAWN, "pawn");
			icon += "pawn.png";
			// Pawns do not have any standard moves.
		}

		// Returns a vector of Move objects that represents all the moves this piece can make
		// from the current position.
		public ArrayList<Move> Moves(Position _theBoard, int _rank, int _file) {
			ArrayList<Move> possibleMoves = new ArrayList<Move>();
			
			int rankOffset;
			int initialRankOffset;

			Move lastMove = null;
			
			// This is needed to determine if an en passant take is possible.
			if (!_theBoard.scoreSheet.empty())
				lastMove = (Move) _theBoard.scoreSheet.peek();
			
			// Determine which way the pawn moves based on its colour. Note that this
			// is not affected by the orientation of the board when it is displayed.
			if (colour == Resources.WHITE) {
				rankOffset = +1;
				initialRankOffset = +2;
			} else {
				rankOffset = -1;
				initialRankOffset = -2;
			}
			
			if (_theBoard.squares[_rank + rankOffset][_file] == null) {	
				// Create a new Move represent the this move
				Move newMove = new Move(_rank, _file, _rank + rankOffset, _file, _theBoard);

				// Discard this move and any further repetitions if the move results in the
				// player's king being in check.
				if (_theBoard.MoveIntoCheck(newMove) == false)
					possibleMoves.add(newMove);					
			}

			// If the two squares 'ahead' are empty and the pawn hasn't yet moved, try moving there.
			if (moveCount == 0) {
				if (_theBoard.squares[_rank + rankOffset][_file] == null && _theBoard.squares[_rank + initialRankOffset][_file] == null) {	
					// Create a new Move represent the this move
					Move newMove = new Move(_rank, _file, _rank + initialRankOffset, _file, _theBoard);
				
					// Discard this move and any further repetitions if the move results in the
					// player's king being in check.
					if (_theBoard.MoveIntoCheck(newMove) == false)
						possibleMoves.add(newMove);					
				}
			}
			
			// If there is a piece of the opposite colour -1 and ahead, try moving there.
			if (_file - 1 > -1) {
				if (_theBoard.squares[_rank + rankOffset][_file - 1] != null &&  _theBoard.squares[_rank + rankOffset][_file - 1].colour != colour) {
					// Create a new Move represent the this move
					Move newMove = new Move(_rank, _file, _rank + rankOffset, _file - 1, _theBoard);
				
					// Discard this move and any further repetitions if the move results in the
					// player's king being in check.
					if (_theBoard.MoveIntoCheck(newMove) == false)
						possibleMoves.add(newMove);					
				}
				if (_theBoard.squares[_rank][_file - 1] != null 
				    && lastMove != null
				    && _theBoard.squares[_rank][_file - 1] instanceof Pawn
				    && _theBoard.squares[_rank][_file - 1].colour != colour
				    && _theBoard.squares[_rank][_file - 1] == lastMove.pieceMoved
				    && Math.abs(lastMove.oldRank - lastMove.newRank) == 2) {
					// Create a new Move represent the this move
					Move newMove = new Move (_rank, _file, _rank + rankOffset, _file - 1, _theBoard);
					
					// Discard this move and any further repetitions if the move results in the
					// player's king being in check.
					if (_theBoard.MoveIntoCheck(newMove) == false)
						possibleMoves.add(newMove);					
				}
			}

			// If there is a piece of the opposite colour +1 and ahead, try moving there.
			if (_file + 1 < 8) {
				if (_theBoard.squares[_rank + rankOffset][_file + 1] != null &&  _theBoard.squares[_rank + rankOffset][_file + 1].colour != colour) {
					// Create a new Move represent the this move
					Move newMove = new Move(_rank, _file, _rank + rankOffset, _file + 1, _theBoard);
				
					// Discard this move and any further repetitions if the move results in the
					// player's king being in check.
					if (_theBoard.MoveIntoCheck(newMove) == false)
						possibleMoves.add(newMove);					
				}
				if (_theBoard.squares[_rank][_file + 1] != null 
				    && lastMove != null
				    && _theBoard.squares[_rank][_file + 1] instanceof Pawn
				    && _theBoard.squares[_rank][_file + 1].colour != colour
				    && _theBoard.squares[_rank][_file + 1] == lastMove.pieceMoved
				    && Math.abs(lastMove.oldRank - lastMove.newRank) == 2) {
					// Create a new Move represent the this move
					Move newMove = new Move (_rank, _file, _rank + rankOffset, _file + 1, _theBoard);
					
					// Discard this move and any further repetitions if the move results in the
					// player's king being in check.
					if (_theBoard.MoveIntoCheck(newMove) == false)
						possibleMoves.add(newMove);					
				}
			}

			return possibleMoves;
		}

		public Object clone () throws CloneNotSupportedException {
			return super.clone();
		}
	}

	class Knight extends Piece implements Cloneable {
		Knight (int _colour) {
			super(_colour, 300, Piece.KNIGHT, "knight");
			icon += "knight.png";
			
			// These are the standard moves a knight can make. 
		    standardMoves = new PieceMove[8];
			standardMoves[0] = new PieceMove(1, -2, false);
			standardMoves[1] = new PieceMove(2, -1, false);
			standardMoves[2] = new PieceMove(2, 1, false);
			standardMoves[3] = new PieceMove(1, 2, false);
			standardMoves[4] = new PieceMove(-1, 2, false);
			standardMoves[5] = new PieceMove(-2, 1, false);
			standardMoves[6] = new PieceMove(-2, -1, false);
			standardMoves[7] = new PieceMove(-1, -2, false);
		}

		public ArrayList<Move> Moves(Position _theBoard, int _rank, int _file) {
			// This piece has no special moves so just return its list of standard moves as generated by Piece.
			return GetStandardMoves(_theBoard, _rank, _file);
		}

		public Object clone () throws CloneNotSupportedException {
			return super.clone();
		}
	}

	class Bishop extends Piece implements Cloneable {
		Bishop (int _colour) {
			super(_colour, 325, Piece.BISHOP, "bishop");
			icon += "bishop.png";
			
			// These are the standard moves a bishop can make. 
		    standardMoves = new PieceMove[4];
			standardMoves[0] = new PieceMove(-1, -1, true);
			standardMoves[1] = new PieceMove(-1, 1, true);
			standardMoves[2] = new PieceMove(1, -1, true);
			standardMoves[3] = new PieceMove(1, 1, true);
		}

		public ArrayList<Move> Moves(Position _theBoard, int _rank, int _file) {
			// This piece has no special moves so just return its list of standard moves as generated by Piece.
			return GetStandardMoves(_theBoard, _rank, _file);
		}

		public Object clone () throws CloneNotSupportedException {
			return super.clone();
		}
	}

	class Rook extends Piece implements Cloneable {
		Rook (int _colour) {
			super(_colour, 500, Piece.ROOK, "rook");
			icon += "rook.png";
			
			// These are the standard moves a rook can make. 
		    standardMoves = new PieceMove[4];
			standardMoves[0] = new PieceMove(-1, 0, true);
			standardMoves[1] = new PieceMove(1, 0, true);
			standardMoves[2] = new PieceMove(0, -1, true);
			standardMoves[3] = new PieceMove(0, 1, true);
		}

		public ArrayList<Move> Moves(Position _theBoard, int _rank, int _file) {
			// This piece has no special moves so just return its list of standard moves as generated by Piece.
			return GetStandardMoves(_theBoard, _rank, _file);
		}

		public Object clone () throws CloneNotSupportedException {
			return super.clone();
		}
	}

	class Queen extends Piece implements Cloneable {
		Queen (int _colour) {
			super(_colour, 900, Piece.QUEEN, "queen");
			icon += "queen.png";
			
			// These are the standard moves a queen can make. 
			standardMoves = new PieceMove[8];
			standardMoves[0] = new PieceMove(-1, -1, true);
			standardMoves[1] = new PieceMove(-1, 0, true);
			standardMoves[2] = new PieceMove(-1, 1, true);
			standardMoves[3] = new PieceMove(0, -1, true);
			standardMoves[4] = new PieceMove(0, 1, true);
			standardMoves[5] = new PieceMove(1, -1, true);
			standardMoves[6] = new PieceMove(1, 0, true);
			standardMoves[7] = new PieceMove(1, 1, true);
		}

		public ArrayList<Move> Moves(Position _theBoard, int _rank, int _file) {
			// This piece has no special moves so just return its list of standard moves as generated by Piece.
			return GetStandardMoves(_theBoard, _rank, _file);
		}

		public Object clone () throws CloneNotSupportedException {
			return super.clone();
		}
	}

	class King extends Piece implements Cloneable {
		King (int _colour) {
			super(_colour, Resources.INFINITY, Piece.KING, "king");
			icon += "king.png";
			
			// These are the standard moves a king can make. 
		        standardMoves = new PieceMove[8];
			standardMoves[0] = new PieceMove(-1, -1, false);
			standardMoves[1] = new PieceMove(-1, 0, false);
			standardMoves[2] = new PieceMove(-1, 1, false);
			standardMoves[3] = new PieceMove(0, -1, false);
			standardMoves[4] = new PieceMove(0, 1, false);
			standardMoves[5] = new PieceMove(1, -1, false);
			standardMoves[6] = new PieceMove(1, 0, false);
			standardMoves[7] = new PieceMove(1, 1, false);
		}

		public ArrayList<Move> Moves(Position _theBoard, int _rank, int _file) {
			ArrayList<Move> possibleMoves = GetStandardMoves(_theBoard, _rank, _file);
			
			// Has the king moved?
			if (moveCount == 0 && !_theBoard.InCheck(colour)) {
				if (colour == Resources.BLACK) {
					// Is the queen-side rook still there unmoved?
					if (_theBoard.squares[7][0] != null && _theBoard.squares[7][0].moveCount == 0) {
						// Are the squares in between empty.
						if (_theBoard.squares[7][1] == null && _theBoard.squares[7][2] == null && _theBoard.squares[7][3] == null) {
							// Are any other those squares being attacked by the other player?
							if (!_theBoard.SquareAttacked(7, 1, colour) &&
								!_theBoard.SquareAttacked(7, 2, colour) &&
								!_theBoard.SquareAttacked(7, 3, colour)) {
								
								// Queenside castling is okay so create a Position for the move
								Move newMove = new Move(7, 4, 7, 2, _theBoard);
								newMove.castling = Move.QUEEN_SIDE;

								// Add the move to the list
								possibleMoves.add(newMove);
							}
						}
					}
					// Is the king-side rook still there unmoved?
					if (_theBoard.squares[7][7] != null && _theBoard.squares[7][7].moveCount == 0) {
						// Are the squares in between empty.
						if (_theBoard.squares[7][5] == null && _theBoard.squares[7][6] == null) {
							// Are any other those squares being attacked by the other player?
							if (	!_theBoard.SquareAttacked(7, 5, colour) &&
								!_theBoard.SquareAttacked(7, 6, colour)) {
								
								// King-side castling is okay so create a Position for the move
								Move newMove = new Move(7, 4, 7, 6, _theBoard);
								newMove.castling = Move.KING_SIDE;

								// Add the move to the list
								possibleMoves.add(newMove);
							}
						}
					}
				} else {
					// Is the queen-side rook still there unmoved?
					if (_theBoard.squares[0][0] != null && _theBoard.squares[0][0].moveCount == 0) {
						// Are the squares in between empty.
						if (_theBoard.squares[0][1] == null && _theBoard.squares[0][2] == null && _theBoard.squares[0][3] == null) {
							// Are any other those squares being attacked by the other player?
							if (!_theBoard.SquareAttacked(0, 1, colour) &&
								!_theBoard.SquareAttacked(0, 2, colour) &&
								!_theBoard.SquareAttacked(0, 3, colour)) {
								
								// Queen-side castling is okay so create a Position for the move
								Move newMove = new Move(0, 4, 0, 2, _theBoard);
								newMove.castling = Move.QUEEN_SIDE;

								// Add the move to the list
								possibleMoves.add(newMove);
							}
						}
					}
					// Is the king-side rook still there unmoved?
					if (_theBoard.squares[0][7] != null && _theBoard.squares[0][7].moveCount == 0) {
						// Are the squares in between empty.
						if (_theBoard.squares[0][5] == null && _theBoard.squares[0][6] == null) {
							// Are any other those squares being attacked by the other player?
							if (!_theBoard.SquareAttacked(0, 5, colour) &&
								!_theBoard.SquareAttacked(0, 6, colour)) {
								
								// King-side castling is okay so create a Position for the move
								Move newMove = new Move(0, 4, 0, 6, _theBoard);
								newMove.castling = Move.KING_SIDE;

								// Add the move to the list
								possibleMoves.add(newMove);
							}
						}
					}
				}
			}
			
			return possibleMoves;
		}

		public Object clone () throws CloneNotSupportedException {
			return super.clone();
		}
	}

}
