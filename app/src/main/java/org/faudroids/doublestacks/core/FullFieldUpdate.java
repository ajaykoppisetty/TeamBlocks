package org.faudroids.doublestacks.core;


import org.roboguice.shaded.goole.common.base.Objects;

import java.io.Serializable;

/**
 * The complete state of a field for one player which can be sent over
 * a network.
 */
public class FullFieldUpdate implements Serializable {

	private final boolean[][] field = new boolean[Constants.BLOCKS_COUNT_X][Constants.BLOCKS_COUNT_Y];

	public boolean hasBlock(int xPos, int yPos) {
		return field[xPos][yPos];
	}

	public void setBlock(int xPos, int yPos) {
		field[xPos][yPos] = true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FullFieldUpdate that = (FullFieldUpdate) o;
		return Objects.equal(field, that.field);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(field);
	}

}
