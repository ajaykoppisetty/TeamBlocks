package org.faudroids.doublestacks.core;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A group of blocks which falls together.
 */
public class BlockGroup implements Serializable {


	public static BlockGroup createL() {
		Block[][] blocks = new Block[2][3];
		blocks[0][0] = createRandomBlock();
		blocks[0][1] = createRandomBlock();
		blocks[0][2] = createRandomBlock();
		blocks[1][0] = createRandomBlock();
		return new BlockGroup(blocks, BlockType.L);
	}


	public static BlockGroup createLMirrored() {
		Block[][] blocks = new Block[2][3];
		blocks[0][0] = createRandomBlock();
		blocks[1][0] = createRandomBlock();
		blocks[1][1] = createRandomBlock();
		blocks[1][2] = createRandomBlock();
		return new BlockGroup(blocks, BlockType.L_MIRRORED);
	}


	public static BlockGroup createI() {
		Block[][] blocks = new Block[1][4];
		blocks[0][0] = createRandomBlock();
		blocks[0][1] = createRandomBlock();
		blocks[0][2] = createRandomBlock();
		blocks[0][3] = createRandomBlock();
		return new BlockGroup(blocks, BlockType.I);
	}


	public static BlockGroup createBox() {
		Block[][] blocks = new Block[2][2];
		for (int x = 0; x < 2; ++x) {
			for (int y = 0; y < 2; ++y) {
				blocks[x][y] = createRandomBlock();
			}
		}
		return new BlockGroup(blocks, BlockType.BOX);
	}


	public static BlockGroup createS() {
		Block[][] blocks = new Block[3][2];
		blocks[0][0] = createRandomBlock();
		blocks[1][0] = createRandomBlock();
		blocks[1][1] = createRandomBlock();
		blocks[2][1] = createRandomBlock();
		return new BlockGroup(blocks, BlockType.S);
	}


	public static BlockGroup createZ() {
		Block[][] blocks = new Block[3][2];
		blocks[0][1] = createRandomBlock();
		blocks[1][0] = createRandomBlock();
		blocks[1][1] = createRandomBlock();
		blocks[2][0] = createRandomBlock();
		return new BlockGroup(blocks, BlockType.Z);
	}


	public static BlockGroup createT() {
		Block[][] blocks = new Block[3][2];
		blocks[0][0] = createRandomBlock();
		blocks[1][0] = createRandomBlock();
		blocks[1][1] = createRandomBlock();
		blocks[2][0] = createRandomBlock();
		return new BlockGroup(blocks, BlockType.T);
	}


	private static Block createRandomBlock() {
		int bitmapType = (int) (Math.random() * (Constants.BLOCK_TYPE_COUNT - Constants.BLOCK_TYPE_SPECIAL_COUNT));
		return new Block(bitmapType);
	}


	private Block[][] blocks;
	private int xPos, yPos; // left lower position
	private final BlockType type;
	private int rotationCount = 0; // how many times this group has been rotated

	private BlockGroup(Block[][] blocks, BlockType type) {
		this.blocks = blocks;
		this.type = type;
	}

	public Block getBlock(int xPos, int yPos) {
		return blocks[xPos][yPos];
	}

	public Block getBlockFromAbsolutePosition(int xPos, int yPos) {
		if (xPos < getxPos() || xPos >= getxPos() + getXSize()
			|| yPos < getyPos() || yPos >= getyPos() + getYSize()) {
			return null;
		}

		return blocks[xPos - getxPos()][yPos - getyPos()];
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

	public int getRotationCount() {
		return rotationCount;
	}

	public BlockType getType() {
		return type;
	}

	public BlockGroup rotate() {
		++rotationCount;
		Block[][] rotatedBlocks = new Block[getYSize()][getXSize()];
		for (int x = 0; x < getXSize(); ++x) {
			for (int y = 0; y < getYSize(); ++y) {
				rotatedBlocks[y][-x + getXSize() - 1] = blocks[x][y];
			}
		}
		BlockGroup rotatedGroup = new BlockGroup(rotatedBlocks, type);
		rotatedGroup.rotationCount = this.rotationCount;

		int newXPos = xPos;
		int newYPos = yPos;

		// move the I block when rotating (is that a little hackish? yes ...)
		if (rotatedGroup.getXSize() == 1) {
			++newXPos;
			--newYPos;
		} else if (rotatedGroup.getYSize() == 1) {
			--newXPos;
			++newYPos;
		}

		rotatedGroup.setxPos(newXPos);
		rotatedGroup.setyPos(newYPos);
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
