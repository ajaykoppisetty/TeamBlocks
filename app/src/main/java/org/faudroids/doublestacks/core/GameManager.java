package org.faudroids.doublestacks.core;


import javax.inject.Inject;

/**
 * Handles the core game logic.
 */
public class GameManager {

	// TODO put some awesome game logic here

	private final Block[][] field = new Block[10][16];


	@Inject
	GameManager() {
		// some blocks for testing
		field[0][0] = new Block();
		field[1][2] = new Block();
		field[0][2] = new Block();
		field[2][2] = new Block();
		field[2][4] = new Block();
		field[9][15] = new Block();
		field[9][0] = new Block();
		field[0][15] = new Block();
	}


	public Block[][] getField() {
		return field;
	}

}
