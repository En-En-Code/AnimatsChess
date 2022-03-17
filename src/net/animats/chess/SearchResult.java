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

