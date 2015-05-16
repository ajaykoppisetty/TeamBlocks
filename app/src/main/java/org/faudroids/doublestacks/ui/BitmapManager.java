package org.faudroids.doublestacks.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.faudroids.doublestacks.R;

import javax.inject.Inject;

/**
 * Loads and holds bitmaps.
 */
public class BitmapManager {

	private Bitmap blocksBackground = null;
	private BlockBitmaps blocks = null;

	@Inject
	BitmapManager() { }


	public void load(Resources resources) {
		blocksBackground = BitmapFactory.decodeResource(resources, R.drawable.blocks_background);
		blocks = BlockBitmaps.load(resources);
	}


	public Bitmap getBlocksBackground() {
		return blocksBackground;
	}


	public BlockBitmaps getBlocks() {
		return blocks;
	}

}
