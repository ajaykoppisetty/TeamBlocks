package org.faudroids.doublestacks.ui;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import org.faudroids.doublestacks.core.Block;
import org.faudroids.doublestacks.core.GameManager;

class GraphicsManager {

	private static final Paint BITMAP_PAINT = new Paint(Paint.FILTER_BITMAP_FLAG);

	private final SurfaceHolder surfaceHolder;
	private final GameManager gameManager;
	private final BitmapManager bitmapManager;

	public GraphicsManager(
			SurfaceHolder surfaceHolder,
			GameManager gameManager,
			BitmapManager bitmapManager,
			Resources resources) {

		this.surfaceHolder = surfaceHolder;
		this.gameManager = gameManager;
		this.bitmapManager = bitmapManager;
		bitmapManager.load(resources);
	}


	public void redrawGraphics() {
		Canvas canvas = surfaceHolder.lockCanvas();
		canvas.drawColor(0, PorterDuff.Mode.CLEAR); // clear screen

		// update background
		canvas.drawBitmap(bitmapManager.getBlocksBackground(), 0, 0, BITMAP_PAINT);

		// update blocks
		Block[][] field = gameManager.getField();
		int xCount = field.length;
		int yCount = field[0].length;
		int xSize = canvas.getWidth() / xCount;
		int ySize = canvas.getHeight() / yCount;

		for (int xPos = 0; xPos < xCount; ++xPos) {
			for (int yPos = 0; yPos < yCount; ++yPos) {
				Block block = field[xPos][yPos];
				if (block == null) continue;
				canvas.drawBitmap(
						bitmapManager.getBlocks().getBitmap(block.getBlockType(), block.getBitmapType()),
						null,
						new Rect(
								xPos * xSize,
								(yCount - yPos - 1) * ySize,
								(xPos + 1) * xSize,
								(yCount - yPos) * ySize),
						BITMAP_PAINT);
			}
		}

		surfaceHolder.unlockCanvasAndPost(canvas);
	}

}
