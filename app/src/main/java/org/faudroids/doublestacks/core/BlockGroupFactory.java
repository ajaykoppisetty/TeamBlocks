package org.faudroids.doublestacks.core;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Creates semi random {@link BlockGroup} instances.
 */
public class BlockGroupFactory {

	private final List<BlockGroup> randomList = new ArrayList<>();

	@Inject
	BlockGroupFactory() {  }


	public BlockGroup createRandom() {
		if (randomList.isEmpty()) fillRandomList();
		BlockGroup group = randomList.get((int) (Math.random() * randomList.size()));
		randomList.remove(group);
		return group;
	}


	private void fillRandomList() {
		for (int i = 0; i < 3; ++i) {
			randomList.add(BlockGroup.createBox());
			randomList.add(BlockGroup.createL());
			randomList.add(BlockGroup.createLMirrored());
			randomList.add(BlockGroup.createI());
			randomList.add(BlockGroup.createS());
			randomList.add(BlockGroup.createZ());
			randomList.add(BlockGroup.createT());
		}
	}

}
