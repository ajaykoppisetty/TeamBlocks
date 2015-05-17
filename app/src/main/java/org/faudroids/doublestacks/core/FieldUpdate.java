package org.faudroids.doublestacks.core;


import org.roboguice.shaded.goole.common.base.Objects;

import java.io.Serializable;

/**
 * The complete state of a field for one player which can be sent over
 * a network.
 */
public class FieldUpdate implements Serializable {

	private final boolean[][] field = new boolean[Constants.BLOCKS_COUNT_X][Constants.BLOCKS_COUNT_Y];

	private int epoch;
	private int seqNum;

	public boolean hasBlock(int xPos, int yPos) {
		return field[xPos][yPos];
	}

	public void setBlock(int xPos, int yPos) {
		field[xPos][yPos] = true;
	}

	public int getEpoch() {
		return epoch;
	}

	public void setEpoch(int epoch) {
		this.epoch = epoch;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FieldUpdate that = (FieldUpdate) o;
		return Objects.equal(epoch, that.epoch) &&
				Objects.equal(seqNum, that.seqNum) &&
				Objects.equal(field, that.field);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(field, epoch, seqNum);
	}
}
