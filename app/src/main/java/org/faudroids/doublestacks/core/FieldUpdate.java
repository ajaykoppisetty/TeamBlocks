package org.faudroids.doublestacks.core;


import org.roboguice.shaded.goole.common.base.Objects;

import java.io.Serializable;
import java.util.List;

/**
 * The complete state of a field for one player which can be sent over
 * a network.
 */
public class FieldUpdate implements Serializable {

	private final Block[][] field;
	private final BlockGroup activeGroup;
	private final List<Integer> removedRows;

	private int epoch;
	private int seqNum;

	public FieldUpdate(BlockGroup activeGroup) {
		this(activeGroup, null, null);
	}

	public FieldUpdate(BlockGroup activeGroup, Block[][] field, List<Integer> removedRows) {
		this.activeGroup = activeGroup;
		this.field = field;
		this.removedRows = removedRows;
	}

	public Block[][] getField() {
		return field;
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

	public BlockGroup getActiveGroup() {
		return activeGroup;
	}

	public List<Integer> getRemovedRows() {
		return removedRows;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FieldUpdate that = (FieldUpdate) o;
		return Objects.equal(epoch, that.epoch) &&
				Objects.equal(seqNum, that.seqNum) &&
				Objects.equal(field, that.field) &&
				Objects.equal(activeGroup, that.activeGroup) &&
				Objects.equal(removedRows, that.removedRows);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(field, activeGroup, removedRows, epoch, seqNum);
	}
}
