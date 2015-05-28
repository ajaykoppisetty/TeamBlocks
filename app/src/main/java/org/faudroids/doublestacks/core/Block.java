package org.faudroids.doublestacks.core;


import java.io.Serializable;

/**
 * A single block in the playing field.
 */
public class Block implements Serializable {

	private int bitmapType;

	public Block(int bitmapType) {
		this.bitmapType = bitmapType;
	}

	public int getBitmapType() {
		return bitmapType;
	}

	public void setBitmapType(int bitmapType) {
		this.bitmapType = bitmapType;
	}

}
