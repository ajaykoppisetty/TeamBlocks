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

	private Bitmap fieldBackground = null, previewBackground = null;
	private BlockBitmaps blocks = null;

	@Inject
	BitmapManager() { }


	public void load(Resources resources) {
		fieldBackground = BitmapFactory.decodeResource(resources, R.drawable.blocks_background);
		previewBackground = BitmapFactory.decodeResource(resources, R.drawable.blocks_preview);
		blocks = BlockBitmaps.load(resources);
	}


	public Bitmap getFieldBackground() {
		return fieldBackground;
	}


	public Bitmap getPreviewBackground() {
		return previewBackground;
	}


	public BlockBitmaps getBlocks() {
		return blocks;
	}

}
