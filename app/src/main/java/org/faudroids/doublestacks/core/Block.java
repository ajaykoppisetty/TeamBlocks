package org.faudroids.doublestacks.core;


/**
 * A single block in the playing field.
 */
public class Block {

	// TODO put some awesome properties here

	private final int bitmapType;
	private final BlockType blockType;

	public Block(int bitmapType, BlockType blockType) {
		this.bitmapType = bitmapType;
		this.blockType = blockType;
	}

	public int getBitmapType() {
		return bitmapType;
	}

	public BlockType getBlockType() {
		return blockType;
	}

}
