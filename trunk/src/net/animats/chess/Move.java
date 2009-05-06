package net.animats.chess;
/** 
 * This class represents a move that a player can make from a given position. It contains all the information
 * needed to update the position with this move and then undo it if required.
 */
class Move implements Comparable<Move> {
	static final char[] fileLetter = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
	static final char[] rankNumber = { '1', '2', '3', '4', '5', '6', '7', '8' };

	static final int NOT_CASTLING = 0;
	static final int QUEEN_SIDE = 1;
	static final int KING_SIDE = 2;
	
                
	GameState stateAfterMove = GameState.UNKNOWN;
        
	// Possible values for stateAfterMove.
	enum GameState { IN_PROGRESS, WHITE_IN_CHECK, BLACK_IN_CHECK, UNKNOWN, 
					 EXITING, BLACK_CHECKMATED, WHITE_CHECKMATED, 
					 BLACK_STALEMATED, WHITE_STALEMATED, DRAW_BY_REPETITION, 
					 FIFTY_MOVE_DRAW, WHITE_RESIGNED, BLACK_RESIGNED }
	
	int oldRank;
	int oldFile;
	int newRank;
	int newFile;

	// This information is set if another piece of the same type can move
	// into the same square as this move.
	boolean disambiguationOnRank;
	boolean disambiguationOnFile;

	Position.Piece pieceTaken;
	Position.Piece pieceMoved;

	boolean promotedPawn = false;
	boolean enPassant = false;

	SearchResult result;
	
	int moveNumber;
	int madeBy;

	int castling = NOT_CASTLING;

	public String toString() {
		if (pieceTaken == null)
			return (Algebraic() + " (" + Resources.englishColour[pieceMoved.colour] + " " + pieceMoved.FullName() + " on " + fileLetter[oldFile] + rankNumber[oldRank] + " to " + fileLetter[newFile] + rankNumber[newRank] + ")");
		else
			return (Algebraic() + " (" + Resources.englishColour[pieceMoved.colour] + " " + pieceMoved.FullName() + " on " + fileLetter[oldFile] + rankNumber[oldRank] + " takes " + fileLetter[newFile] + rankNumber[newRank] + ")");
	}

	Move (int _oldRank, int _oldFile, int _newRank, int _newFile, Position _theBoard) {
		oldRank = _oldRank;
		oldFile = _oldFile;
		newRank = _newRank;
		newFile = _newFile;
		pieceMoved = _theBoard.getPieceAt(oldRank, oldFile);
		pieceTaken = _theBoard.getPieceAt(newRank, newFile);
		moveNumber = _theBoard.getScoreSheet().size() + 1;
		madeBy = _theBoard.getWhoseTurn();
	
		if (pieceMoved instanceof Position.Pawn) {
			if (oldFile != newFile && pieceTaken == null) {
				// Must be an en passant take, set the 
				// pieceTaken to the pawn to be taken.
				pieceTaken = _theBoard.getPieceAt(_oldRank, _newFile);
				enPassant = true;
			}
		}
	}

	public String ScoreSheetAlgebraic() {
		StringBuilder moveString = new StringBuilder();
		if (madeBy == Resources.BLACK) {
			moveString.append(Algebraic());
		}else {
			moveString.append(MoveNumber());
			moveString.append(".");
			moveString.append(Algebraic());
		}
		
		return moveString.toString();
	}
	
	public String NumberedAlgebraic() {
		StringBuilder moveString = new StringBuilder();
		moveString.append(MoveNumber());
		if (madeBy == Resources.BLACK) {
			moveString.append(". ... ");
			moveString.append(Algebraic());
		}else {
			moveString.append(". ");
			moveString.append(Algebraic());
		}
		
		return moveString.toString();
	}
	
	public String Algebraic() {
		StringBuilder move = new StringBuilder();

		if (castling == KING_SIDE)
			return ("O-O");
		else if (castling == QUEEN_SIDE)
			return ("O-O-O");
		else if (!(pieceMoved instanceof Position.Pawn)) {
			if (pieceMoved instanceof Position.Rook)
				move.append("R");
			else if (pieceMoved instanceof Position.Knight)
				move.append("N");
			else if (pieceMoved instanceof Position.Bishop)
				move.append("B");
			else if (pieceMoved instanceof Position.King)
				move.append("K");
			else 
				move.append("Q");

			if (disambiguationOnRank) {
				move.append(rankNumber[oldRank]);
			}

			if (disambiguationOnFile) {
				move.append(fileLetter[oldFile]);
			}

		} else if (pieceTaken != null) {
			// This is a pawn move that resulting in a piece being taken.
			move.append(fileLetter[oldFile]);
		}

		if (pieceTaken != null) {
			// This move resulting in a piece being taken.
			move.append("x");
		}

		// Display the destination location.
		move.append(fileLetter[newFile]);
		move.append(rankNumber[newRank]);

		if (stateAfterMove == GameState.WHITE_CHECKMATED || stateAfterMove == GameState.BLACK_CHECKMATED)
			move.append("#");
		else if (stateAfterMove == GameState.WHITE_IN_CHECK || stateAfterMove == GameState.BLACK_IN_CHECK)
			move.append("+");
		
		return (move.toString());
	}

	public String AsCommand() {
		return ("" + fileLetter[oldFile] + rankNumber[oldRank] + fileLetter[newFile] + rankNumber[newRank]);
	}

	/**
	 * This method is part of the comparable interface and is used to sort the legalMove vector.
	 */
	public int compareTo(Move _move) {
		return (result.evaluation - ((Move) _move).result.evaluation);
	}
	
	/* Returns a displayable version of the current move number
	 * 
	 */
	public int MoveNumber() {
		if (moveNumber % 2 == 0)
			return (moveNumber / 2);
		else
			return ((moveNumber / 2) + 1);
	}
}
