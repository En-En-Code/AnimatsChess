package net.animats.chess;
/** 
 * This class is the type returned by the BuildTree search method.
 */

public class SearchResult {
	int evaluation;
	int searchDepth;

	// The immediate move that ultimately leads to the position that this is 
	// the evaluation of.
	Move leadsTo = null;

	SearchResult (int _evaluation, int _searchDepth) {
		evaluation = _evaluation;
		searchDepth = _searchDepth;
	}
}

