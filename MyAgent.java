import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyAgent extends Agent {

	int rows = myGame.getRowCount(); // more readable
	int columns = myGame.getColumnCount();
	Random r;

	/**
	 * Constructs a new agent, giving it the game and telling it whether it is
	 * Red or Yellow.
	 * 
	 * @param game The game the agent will be playing.
	 * @param iAmRed True if the agent is Red, False if the agent is Yellow.
	 */
	public MyAgent(Connect4Game game, boolean iAmRed) {
		super(game, iAmRed);
	}

	/**
	 * Check if this is the first move.  This will either be if the bottom row is empty
	 * or the opponent has placed only one token on the bottom row (i.e. they started first)
	 * @return true if this is a new game
	 */
	private boolean isNewGame() {
		int moveCount = 0;
		// check the bottom row of the table
		for (int i = 0; i < columns; i++) {
			// if there is a token add to the count
			if (getLowestEmptyIndex(myGame.getColumn(i)) != rows - 1) {
				moveCount++;
			}

		}
		// the count will be zero if we are to move first and one if they have already had the first move
		if (moveCount <= 1) {
			return true;
		}
		return false;

	}

	/**
	 * This method makes the opening move.  It tries to place in the middle slot of the board.
	 * If the opponent has already taken that slot it will place next to it.  This prevents
	 * the opponent getting three in a row with open slots each side.
	 */
	private void makeOpeningMove() {
		Connect4Column middleColumn = myGame.getColumn(getMiddleColumn());
		Connect4Slot bottomMiddleSlot = middleColumn.getSlot(rows - 1);
		if (!bottomMiddleSlot.getIsFilled()) {
			moveOnColumn(getMiddleColumn());
		} else {
			moveOnColumn(getMiddleColumn() - 1);
		}
	}

	/**
	 * Compares all of the columns in the game for the highest number vertical
	 * matches of the Colour passed in.
	 * @param colour the Colour to find matches of.
	 * @return Match object with the column that has the highest number of Colour matches.
	 */
	private Match getBestVerticalMatch(Colour colour) {
		int numberOfMatches = 0;
		Match bestMatch = new Match(); // Stores the current best Match while iterating through 
										// all of the games columns.

		// check all the columns 
		for (int i = 0; i < columns; i++) {
			Connect4Column column = myGame.getColumn(i);
			// if the column is full then we cant place a token here so just continue on to the next iteration
			if (column.getIsFull()) {
				continue;
			}

			numberOfMatches = calcVerticalMatch(i, colour);

			// prefer vertical match columns that have enough remaining slots to be able to connect 4
			// number of matches + remaining empty slots >= 4
			boolean canWinWithRemainingSlots = numberOfMatches + getLowestEmptyIndex(column) >= 3;

			// this slot has more matches than our current best vertical column, and there are enough
			// slots to get four in a row, then set this column as our current best.
			if (numberOfMatches >= bestMatch.getCount() && canWinWithRemainingSlots) {
				bestMatch.setCount(numberOfMatches);
				bestMatch.setColumn(i);

			}
		}
		return bestMatch;
	}

	/**
	 * Compares all of the columns for the highest number of horizontal matches
	 * of the Colour passed in.
	 * 
	 * @param colour colour to find horizontal matches for.
	 * @return Match bestHorizontalMatch containing the highest matches of
	 * colour and which column that is.
	 */
	private Match getBestHorizontalMatch(Colour colour) {
		int numberOfLeftMatches = 0;
		int numberOfRightMatches = 0;
		Match bestHorizontalMatch = new Match();
		// Iterates over all of the game Columns checking the columns to the left and right of the 
		// Column being tested to determine the Column that has the highest number of matches.
		for (int i = 0; i < columns; i++) {

			numberOfLeftMatches = calcLeftMatch(i, colour); //counts matches to the left.
			numberOfRightMatches = calcRightMatch(i, colour); // counts matches to the right.
			if (numberOfLeftMatches + numberOfRightMatches >= bestHorizontalMatch.getCount()) {
				bestHorizontalMatch.setCount(numberOfRightMatches + numberOfLeftMatches);
				bestHorizontalMatch.setColumn(i);
			}
		}
		return bestHorizontalMatch;
	}

	/**
	 * Determines the Column that has the highest number of diagonal matches for
	 * the Colour passed in.
	 * 
	 * @param colour the Colour to find matches for
	 * @return Match match holding the column and the highest number of matches.
	 */

	private Match getBestDiagonalMatch(Colour colour) {
		Match bestMatch = new Match();

		for (int i = 0; i < columns; i++) { //iterate over each Column in game
			int leftDown = calcLeftDownMatch(i, colour);
			int rightUp = calcRightUpMatch(i, colour);
			int leftUp = calcLeftUpMatch(i, colour);
			int rightDown = calcRightDownMatch(i, colour);

			int a = leftDown + rightUp; // total of one of the two possible diagonal lines.
			int b = leftUp + rightDown; //total of the second of two possible diagonal lines.

			//Compare values to find highest match of all Columns.
			// Set highest number of matches and Column in bestMatch.
			if (a >= bestMatch.getCount()) {
				bestMatch.setCount(a);
				bestMatch.setColumn(i);
			}
			if (b >= bestMatch.getCount()) {
				bestMatch.setCount(b);
				bestMatch.setColumn(i);
			}
		}
		return bestMatch;
	}

	/**
	 * In case the game board has an unexpected number of rows this method uses
	 * the total number of Columns to determine the middle Column.
	 * 
	 * @return int the number of the middle Column
	 */
	private int getMiddleColumn() {
		return (int) Math.floor((double) columns / 2);
	}

	/**
	 * The move method is run every time it is this agent's turn in the game.
	 * You may assume that when move() is called, the game has at least one open
	 * slot for a token, and the game has not already been won.
	 * 
	 * By the end of the move method, the agent should have placed one token
	 * into the game at some point.
	 * 
	 * After the move() method is called, the game engine will check to make
	 * sure the move was valid. A move might be invalid if: - No token was place
	 * into the game. - More than one token was placed into the game. - A
	 * previous token was removed from the game. - The color of a previous token
	 * was changed. - There are empty spaces below where the token was placed.
	 * 
	 * If an invalid move is made, the game engine will announce it and the game
	 * will be ended.
	 * 
	 */
	public void move() {

		Match nextMove = null;
		// Executes opening move strategy.  Should only happen once at beginning of new game.
		if (isNewGame()) {
			makeOpeningMove();
			return;
		}

		Match bestMatchRed = findBestMatch(Colour.RED); //determine our best move.
		Match bestMatchYellow = findBestMatch(Colour.YELLOW); // determine opponents best move.
		// If we can win on this move then make that winning move
		if (nextMove == null && iCanWin(bestMatchRed)) {
			if (validNextMove(bestMatchRed)) {
				nextMove = bestMatchRed;
			}
		}
		// If we cannot win on this move but the opponent could win on their next turn 
		// move to block their winning move.
		if (nextMove == null && iCanWin(bestMatchYellow)) {
			if (validNextMove(bestMatchYellow)) {
				nextMove = bestMatchYellow;
			}
		}
		if (nextMove == null) {
			if (validNextMove(bestMatchRed)) {
				nextMove = bestMatchRed;
			} else {
			}
		}

		moveOnColumn(nextMove.getColumn());

	}

	/**
	 * Checks that the next move will be made in a column within the borders of
	 * the game.
	 * 
	 * @param nextMove The match object to check the column of.
	 * @return true if move possible.
	 */

	private boolean validNextMove(Match nextMove) {

		if (nextMove.getColumn() >= 0 && nextMove.getColumn() <= columns) {
			return true;
		}
		return false;
	}

	/**
	 * Finds the slot that has the highest number of matching tokens nearby for
	 * making connect four.
	 * 
	 * @param colour is colour of token to find best match for.
	 * @return match object with the best highest count.
	 */
	private Match findBestMatch(Colour colour) {

		Match bestVerticalMatch = getBestVerticalMatch(colour);
		Match bestHorizontalMatch = getBestHorizontalMatch(colour);
		Match bestDiagonalMatch = getBestDiagonalMatch(colour);

		//add the different kinds of matches to a list
		List<Match> list = new ArrayList<Match>();
		list.add(bestVerticalMatch);
		list.add(bestHorizontalMatch);
		list.add(bestDiagonalMatch);
		//compare the count of each match to find the highest match
		Match bestMatch = null;
		for (Match match : list) {
			if (bestMatch == null || bestMatch.getCount() < match.getCount()) {
				bestMatch = match;
			}

		}
		return bestMatch;
	}

	/**
	 * Drops a token into a particular column so that it will fall to the bottom
	 * of the column. If the column is already full, nothing will change.
	 * 
	 * @param columnNumber The column into which to drop the token.
	 */
	public void moveOnColumn(int columnNumber) {

		int lowestEmptySlotIndex = getLowestEmptyIndex(myGame.getColumn(columnNumber));
		// Find the top empty slot in the column If the column is full,
		// lowestEmptySlot will be -1
		if (lowestEmptySlotIndex > -1) // if the column is not full
		{
			// Get the slot in this column at this index
			Connect4Slot lowestEmptySlot = myGame.getColumn(columnNumber).getSlot(lowestEmptySlotIndex);
			if (iAmRed) // If the current agent is the Red player...
			{
				lowestEmptySlot.addRed(); // Place a red token into the empty slot

			} else // If the current agent is the Yellow player (not the Red player)
			{
				lowestEmptySlot.addYellow(); // Place a yellow token into the empty slot
			}
		}
	}

	/** Keeps track of the total number of matches.
	 * 
	 * @param colour Colour to find matches of.
	 * @param matches gets incremented if a match is found.
	 * @param slot slot to be tested for a matching token
	 * @return int matches containing current total number of matches.
	 */
	private int incrementMatches(Colour colour, int matches, Connect4Slot slot) {
		if (slot != null) {
			if (slot.getIsFilled()) {
				// If looking for RED tokens and find a RED token.
				if (Colour.RED.equals(colour) && slot.getIsRed()) {
					matches++;
					// if looking for a YELLOW token and find a YELLOW token.
				} else if (Colour.YELLOW.equals(colour) && !slot.getIsRed()) {
					matches++;
				}
			}
		}
		return matches;
	}

	/**
	 * Returns the index of the top empty slot in a column.
	 * 
	 * @param column The column to check.
	 * @return the index of the top empty slot in a particular column; -1 if the column is already full.
	 */
	public int getLowestEmptyIndex(Connect4Column column) {
		int lowestEmptySlot = -1;
		for (int i = 0; i < column.getRowCount(); i++) {
			if (!column.getSlot(i).getIsFilled()) {
				lowestEmptySlot = i;
			}
		}
		return lowestEmptySlot;
	}

	/**
	 * Returns a random valid move. If your agent doesn't know what to do,
	 * making a random move can allow the game to go on anyway.
	 * 
	 * @return a random valid move.
	 */
	public int randomMove() {
		int i = r.nextInt(myGame.getColumnCount());
		while (getLowestEmptyIndex(myGame.getColumn(i)) == -1) {
			i = r.nextInt(myGame.getColumnCount());
		}
		return i;
	}

	/**
	 * Returns the column that would allow the agent to win.
	 * 
	 * You might want your agent to check to see if it has a winning move
	 * available to it so that it can go ahead and make that move. Implement
	 * this method to return what column would allow the agent to win.
	 *
	 * @return the column that would allow the agent to win.
	 */
	public boolean iCanWin(Match match) {
		if (match.getCount() >= 3) {
			return true;
		}
		return false;

	}

	/**
	 * Returns the column that would allow the opponent to win.
	 * 
	 * You might want your agent to check to see if the opponent would have any
	 * winning moves available so your agent can block them. Implement this
	 * method to return what column should be blocked to prevent the opponent
	 * from winning.
	 *
	 * @return the column that would allow the opponent to win.
	 */
	public int theyCanWin() {
		return 0;
	}

	/**
	 * Returns the name of this agent.
	 *
	 * @return the agent's name
	 */
	public String getName() {
		return "Agent Sam";
	}

	/**
	 * Counts the number of VERTICAL matches a column has.
	 * * 0|_|___|___|___|___|___|___| 
	 * 1| R_|_R_|___|___|___|_R_|_Y_| 
	 * 2| Y_|_Y_|_R_|_T_|_R_|_R_|_Y_| T = Test slot 
	 * 3| Y_|_Y_|_R_|_*_|_Y_|_Y_|_R_| * = Potential match slots tested by method 
	 * 4| R_|_R_|_Y_|_*_|_R_|_Y_|_R_| R = Red Token 
	 * 5| Y_|_R_|_R_|_*_|_Y_|_R_|_R_| Y = Yellow Token 
	 *    0 | 1 | 2 | 3 | 4 | 5 | 6 |
	 * 
	 * @param columnIndex the column to be tested.
	 * @param colour the colour to find matches of.
	 * @return int matches holding the current total of matching tokens.
	 */
	public int calcVerticalMatch(int columnIndex, Colour colour) {
		int matches = 0;
		Connect4Column column = myGame.getColumn(columnIndex);
		int row = getLowestEmptyIndex(column);
		if (row != -1) { // make sure there is a slot in the row for another token.

			// checks the slot below the lowest empty index and continues below until
			//a non-matching token is found or no more valid slots to check. 
			for (int j = row + 1; j < rows; j++) {
				Connect4Slot slot = column.getSlot(j);
				matches = incrementMatches(colour, matches, slot);
				if (nonMatchedTokenFound(colour, matches, slot)) {
					break;
				}
			}

		}
		return matches;
	}

	/**
	 * Tests a slot to see if it contains a token that does NOT match
	 * 
	 * @param colour the colour of the token that is NOT matching the tested slot.
	 * @param matches holds the current count of matches. 
	 * @param slot slot being tested for a non matching token.
	 * @return True if a token is found that is NOT a match for the colour parameter.
	 */
	private boolean nonMatchedTokenFound(Colour colour, int matches, Connect4Slot slot) {
		return matches == 0 || !slot.getIsFilled()
				|| (Colour.RED.equals(colour) && (slot != null && slot.getIsFilled() && !slot.getIsRed()))
				|| (Colour.YELLOW.equals(colour) && (slot != null && slot.getIsFilled() && slot.getIsRed()));
	}

	/**
	 * Looks for matches to the 'LEFT' and 'DOWN' of the test slot. If match found
	 * tests next left column and next row down.  Example Board:
	 * 0|__|___|___|___|___|___|___| 
	 * 1| R_|_R_|___|___|___|_R_|_Y_| 
	 * 2| Y_|_Y_|_R_|_T_|_R_|_R_|_Y_| T = Test slot 
	 * 3| Y_|_Y_|_*_|_Y_|_Y_|_Y_|_R_| * = Potential match slots tested by method 
	 * 4| R_|_*_|_Y_|_R_|_R_|_Y_|_R_| R = Red Token 
	 * 5| *_|_R_|_R_|_R_|_Y_|_R_|_R_| Y = Yellow Token 
	 *    0 | 1 | 2 | 3 | 4 | 5 | 6 |
	 * 
	 * @param columnIndex column number to test lowest empty slot of.
	 * @param colour colour of token to seek matches for.
	 * @return int matches holding current total count of matching tokens.
	 */
	public int calcLeftDownMatch(int columnIndex, Colour colour) {
		int matches = 0;
		Connect4Column column = myGame.getColumn(columnIndex);
		int row = getLowestEmptyIndex(column);
		// we don't need to check left on the first column or if the column is full
		if (columnIndex != 0 && !column.getIsFull()) {
			for (int j = columnIndex - 1; j >= 0; j--) {
				// check that the row is not out of bounds
				if (++row == rows) {
					break;
				}
				// Iterates through columns looking for matches 'DOWN' and to the 'LEFT'.
				// When a match is found increments matches to keep track of the total.
				Connect4Column tempColumn = myGame.getColumn(j);
				if (tempColumn != null) {
					Connect4Slot slot = tempColumn.getSlot(row);
					matches = incrementMatches(colour, matches, slot);
					// if encounters an empty slot or a non-matching token stops
					if (nonMatchedTokenFound(colour, matches, slot)) {
						break;
					}
				}
			}
		}
		return matches;
	}

	/**
		 * Looks for matches to the 'UP' and to the 'LEFT' of the test slot. If match found
		 * tests next left column and next row down. Example board:
		 * 0|_*_|___|___|___|___|___|___| 
		 * 1| R_|_*_|___|___|___|_R_|_Y_| 
		 * 2| Y_|_Y_|_*_|___|_R_|_R_|_Y_| T = Test slot 
		 * 3| Y_|_Y_|_R_|_T_|_Y_|_Y_|_R_| * = Potential match slots tested by method 
		 * 4| R_|_R_|_Y_|_R_|_R_|_Y_|_R_| R = Red Token 
		 * 5| Y_|_R_|_R_|_R_|_Y_|_R_|_R_| Y = Yellow Token 
		 *    0 | 1 | 2 | 3 | 4 | 5 | 6 |
		 * 
	 * @param columnIndex Column to test lowest empty slot in.
	 * @param colour of token to seek matches for.
	 * @return int matches holding current total of matching tokens.
	 */
	public int calcLeftUpMatch(int columnIndex, Colour colour) {
		int matches = 0;
		Connect4Column column = myGame.getColumn(columnIndex);
		int row = getLowestEmptyIndex(column);
		// we don't need to check left on the first column or if the column is full
		if (columnIndex != 0 && !column.getIsFull()) {
			for (int j = columnIndex - 1; j >= 0; j--) {
				// check the row is not out of bounds when we test one higher than lowest empty index.
				if (--row < 0) {
					break;
				}
				// Iterates through columns looking for matches 'UP' and to the 'LEFT'.
				// When a match is found increments matches to keep track of the total.
				Connect4Column tempColumn = myGame.getColumn(j);
				if (tempColumn != null) {
					Connect4Slot slot = tempColumn.getSlot(row);
					matches = incrementMatches(colour, matches, slot);
					if (nonMatchedTokenFound(colour, matches, slot)) {
						break;
					}
				}
			}

		}
		return matches;
	}

	/**
	 * Looks for matches to the RIGHT and UP of the test slot. If match found
	 * tests next left column and next row down.  Example Board:
	 * 0|___|___|___|___|___|___|_*_| 
	 * 1| R_|_R_|___|___|___|_*_|_Y_| 
	 * 2| Y_|_Y_|_R_|___|_*_|_R_|_Y_| T = Test slot 
	 * 3| Y_|_Y_|_R_|_T_|_Y_|_Y_|_R_| * = Potential match slots tested by method 
	 * 4| R_|_R_|_Y_|_R_|_R_|_Y_|_R_| R = Red Token 
	 * 5| Y_|_R_|_R_|_R_|_Y_|_R_|_R_| Y = Yellow Token 
	 *    0 | 1 | 2 | 3 | 4 | 5 | 6 |
	 * 
	 * @param columnIndex column number to test lowest empty slot of.
	 * @param colour colour of token to seek matches for.
	 * @return int matches holding current total of matching tokens.
	 */
	public int calcRightUpMatch(int columnIndex, Colour colour) {
		int matches = 0;
		Connect4Column column = myGame.getColumn(columnIndex);
		int row = getLowestEmptyIndex(column);
		// check getting 'UP' and 'RIGHT' match is still a valid position
		if (columnIndex + 1 < columns && !column.getIsFull()) {

			// check columns to the right of our current index
			for (int j = columnIndex + 1; j < columns; j++) {
				if (--row < 0) { //makes column isn't full
					break;
				}
				// Iterates through columns looking for matches 'UP' and to the 'RIGHT'.
				// When a match is found increments matches to keep track of the total.
				Connect4Column tempColumn = myGame.getColumn(j);
				if (tempColumn != null) {
					Connect4Slot slot = tempColumn.getSlot(row);
					matches = incrementMatches(colour, matches, slot);
					if (nonMatchedTokenFound(colour, matches, slot)) {
						break;
					}
				}
			}

		}
		return matches;
	}

	/**
	 * Looks for matches to the 'RIGHT' and 'DOWN' of the test slot. If match found
	 * tests next left column and next row down.  Example Board:
	 * 0|__|___|___|___|___|___|___| 
	 * 1| R_|_R_|___|___|___|_R_|_Y_| 
	 * 2| Y_|_Y_|_R_|___|_R_|_R_|_Y_| T = Test slot 
	 * 3| Y_|_Y_|_R_|_T_|_Y_|_Y_|_R_| * = Potential match slots tested by method 
	 * 4| R_|_R_|_Y_|_R_|_*_|_Y_|_R_| R = Red Token 
	 * 5| Y_|_R_|_R_|_R_|_Y_|_*_|_R_| Y = Yellow Token 
	 *    0 | 1 | 2 | 3 | 4 | 5 | 6 |
	 * 
	 * @param columnIndex column number to test lowest empty slot of.
	 * @param colour colour of token to seek matches for.
	 * @return int matches holding current total of matching tokens.
	 */

	public int calcRightDownMatch(int columnIndex, Colour colour) {
		int matches = 0;
		Connect4Column column = myGame.getColumn(columnIndex);
		int row = getLowestEmptyIndex(column);
		// Check the column is in range and the column is not full
		if (columnIndex + 1 < columns && !column.getIsFull()) {
			for (int j = columnIndex + 1; j < columns; j++) {
				// check our row does not go out of range as we go down
				if (++row == rows) {
					break;
				}
				// Iterates through columns looking for matches 'DOWN' and to the 'RIGHT'.
				// When a match is found increments matches to keep track of the total.
				Connect4Column tempColumn = myGame.getColumn(j);
				if (tempColumn != null) {

					Connect4Slot slot = tempColumn.getSlot(row);
					matches = incrementMatches(colour, matches, slot);
					if (nonMatchedTokenFound(colour, matches, slot)) {
						break;
					}
				}
			}

		}
		return matches;
	}

	/**
	 * Looks for matches to the 'LEFT' of the test slot. If match found
	 * tests the next LEFT column.  Example Board:
	 * 0|__|___|___|___|___|___|___| 
	 * 1| __|___|___|___|___|_R_|_Y_| 
	 * 2| *_|_*_|_*_|_T_|_R_|_R_|_Y_| T = Test slot 
	 * 3| Y_|_y_|_R_|_Y_|_Y_|_Y_|_R_| * = Potential match slots tested by method 
	 * 4| R_|_R_|_Y_|_R_|_R_|_Y_|_R_| R = Red Token 
	 * 5| Y_|_R_|_R_|_R_|_Y_|_Y_|_R_| Y = Yellow Token 
	 *    0 | 1 | 2 | 3 | 4 | 5 | 6 |
	 * 
	 * @param columnIndex column number to test lowest empty slot of.
	 * @param colour colour of token to seek matches for.
	 * @return int matches holding current total of matching tokens.
	 */

	public int calcLeftMatch(int columnIndex, Colour colour) {
		int matches = 0;
		Connect4Column column = myGame.getColumn(columnIndex);

		int row = getLowestEmptyIndex(column);
		// we don't need to check left on the first column or if the column is full
		// Iterates through columns looking for matches to the LEFT.
		if (columnIndex != 0 && !column.getIsFull()) {
			for (int j = columnIndex - 1; j >= 0; j--) { // gets column to left of test column
				Connect4Column tempColumn = myGame.getColumn(j);
				// When a match is found increments matches to keep track of the total.
				if (tempColumn != null) {
					Connect4Slot slot = tempColumn.getSlot(row);
					matches = incrementMatches(colour, matches, slot);
					if (nonMatchedTokenFound(colour, matches, slot)) { // no matching token found.
						break;
					}
				}
			}
		}
		return matches;
	}

	/**
	 * Looks for matches to the 'RIGHT' of the test slot. If match found
	 * tests the next LEFT column.  Example Board:
	 * 0|___|___|___|___|___|___|___| 
	 * 1| __|___|___|___|___|___|___| 
	 * 2| R_|_R_|_Y_|_T_|_*_|_*_|_*_| T = Test slot 
	 * 3| Y_|_y_|_R_|_Y_|_Y_|_Y_|_R_| * = Potential match slots tested by method 
	 * 4| R_|_R_|_Y_|_R_|_R_|_Y_|_R_| R = Red Token 
	 * 5| Y_|_R_|_R_|_R_|_Y_|_Y_|_R_| Y = Yellow Token 
	 *    0 | 1 | 2 | 3 | 4 | 5 | 6 |
	 * 
	 * @param columnIndex column number to test lowest empty slot of.
	 * @param colour colour of token to seek matches for.
	 * @return int matches holding current total of matching tokens.
	 */
	public int calcRightMatch(int columnIndex, Colour colour) {
		int matches = 0;
		Connect4Column column = myGame.getColumn(columnIndex);
		int row = getLowestEmptyIndex(column);
		// check column is in range and column is not full
		if (columnIndex + 1 < columns && !column.getIsFull()) { // No need to check if no columns to the right.
			for (int j = columnIndex + 1; j < columns; j++) {// Gets columns to the right.
				Connect4Column tempColumn = myGame.getColumn(j);
				// If a match is found increments matches to keep track of the total.
				if (tempColumn != null) {
					Connect4Slot slot = tempColumn.getSlot(row);
					matches = incrementMatches(colour, matches, slot);
					if (nonMatchedTokenFound(colour, matches, slot)) {
						break;
					}
				}
			}
		}
		return matches;
	}
}
