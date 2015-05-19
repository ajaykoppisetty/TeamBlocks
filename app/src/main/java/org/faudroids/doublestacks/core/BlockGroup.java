package org.faudroids.doublestacks.core;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A group of blocks which falls together.
 */
public class BlockGroup {

	private Block[][] blocks;
	private int xPos, yPos; // left lower position


	public static BlockGroup createL() {
		Block[][] blocks = new Block[2][3];
		blocks[0][0] = createRandomBlock();
		blocks[0][1] = createRandomBlock();
		blocks[0][2] = createRandomBlock();
		blocks[1][0] = createRandomBlock();
		return new BlockGroup(blocks);
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

	public BlockGroup roate() {
		Block[][] rotatedBlocks = new Block[getYSize()][getXSize()];
		for (int x = 0; x < getXSize(); ++x) {
			for (int y = 0; y < getYSize(); ++y) {
				rotatedBlocks[-y + getYSize() - 1][x] = blocks[x][y];
			}
		}
		BlockGroup rotatedGroup = new BlockGroup(rotatedBlocks);
		rotatedGroup.setxPos(xPos);
		rotatedGroup.setyPos(yPos);
		return rotatedGroup;
	}

	public Collection<Location> getAbsoluteLocations() {
		List<Location> locations = new ArrayList<>();
		for (int x = 0; x < getXSize(); ++x) {
			for (int y = 0; y < getYSize(); ++y) {
				if (blocks[x][y] == null) continue;
				locations.add(new Location(x + xPos, y + yPos));
			}
		}
		return locations;
	}

}
