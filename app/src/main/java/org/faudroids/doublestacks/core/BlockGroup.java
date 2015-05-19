package org.faudroids.doublestacks.core;


/**
 * A group of blocks which falls together.
 */
public class BlockGroup {

	private Block[][] blocks;
	private int xPos, yPos; // left lower position


	public static BlockGroup createL() {
		return null;
	}


	public static BlockGroup createLMirrored() {
		return null;
	}


	public static BlockGroup createI() {
		return null;
	}


	public static BlockGroup createBox() {
		Block[][] blocks = new Block[2][2];
		for (int x = 0; x < 2; ++x) {
			for (int y = 0; y < 2; ++y) {
				blocks[x][y] = createRandomBlock();
			}
		}
		return new BlockGroup(blocks);
	}


	private static Block createRandomBlock() {
		int bitmapType = (int) (Math.random() * Constants.BLOCK_TYPE_COUNT);
		return new Block(bitmapType, BlockType.PLAYER_1);
	}


	private BlockGroup(Block[][] blocks) {
		this.blocks = blocks;
	}


	public void rotate() {
		// TODO
	}

	public Block getBlock(int xPos, int yPos) {
		return blocks[xPos][yPos];
	}

	public int getxPos() {
		return xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public void setxPos(int xPos) {
		this.xPos = xPos;
	}

	public void setyPos(int yPos) {
		this.yPos = yPos;
	}

	public int getXSize() {
		return blocks.length;
	}

	public int getYSize() {
		return blocks[0].length;
	}

}
