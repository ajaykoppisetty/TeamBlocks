package org.faudroids.doublestacks.core;


import javax.inject.Inject;

/**
 * Handles the core game logic.
 */
public class GameManager {

	// TODO put some awesome game logic here

	private final Block[][] field = new Block[Constants.BLOCKS_COUNT_X][Constants.BLOCKS_COUNT_Y];


	@Inject
	GameManager() {
		// some blocks for testing
		for (int x = 0; x < Constants.BLOCKS_COUNT_X; ++x) {
			for (int y = 0; y < Constants.BLOCKS_COUNT_Y - 5; ++y) {
				int bitmapType = (int) (Math.random() * Constants.BLOCK_TYPE_COUNT);
				BlockType blockType = BlockType.COMBINED;
				double random = Math.random();
				if (random < 0.333) blockType = BlockType.PLAYER_1;
				else if (random < 0.666) blockType = BlockType.PLAYER_2;
				field[x][y] = new Block(bitmapType, blockType);
			}
		}
	}


	public Block[][] getField() {
		return field;
	}

}
