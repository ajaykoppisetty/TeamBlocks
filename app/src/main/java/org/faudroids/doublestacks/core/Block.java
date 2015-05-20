package org.faudroids.doublestacks.core;


import java.io.Serializable;

/**
 * A single block in the playing field.
 */
public class Block implements Serializable {

	// TODO put some awesome properties here

	private final int bitmapType;

	public Block(int bitmapType) {
		this.bitmapType = bitmapType;
	}

	public int getBitmapType() {
		return bitmapType;
	}

}
