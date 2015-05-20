package org.faudroids.doublestacks.ui;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import org.faudroids.doublestacks.core.Block;
import org.faudroids.doublestacks.core.BlockGroup;
import org.faudroids.doublestacks.core.GameManager;

class GraphicsManager {

	private static final Paint BITMAP_PAINT = new Paint(Paint.FILTER_BITMAP_FLAG);

	private final SurfaceHolder fieldSurfaceHolder, previewSurfaceHolder;
	private final GameManager gameManager;
	private final BitmapManager bitmapManager;

	public GraphicsManager(
			SurfaceHolder fieldSurfaceHolder,
			SurfaceHolder previewSurfaceHolder,
			GameManager gameManager,
			BitmapManager bitmapManager,
			Resources resources) {

		this.fieldSurfaceHolder = fieldSurfaceHolder;
		this.previewSurfaceHolder = previewSurfaceHolder;
		this.gameManager = gameManager;
		this.bitmapManager = bitmapManager;
		bitmapManager.load(resources);
	}


	public void redrawGraphics() {
		Canvas fieldCanvas = fieldSurfaceHolder.lockCanvas();
		fieldCanvas.drawColor(0, PorterDuff.Mode.CLEAR); // clear screen
		drawField(fieldCanvas);
		fieldSurfaceHolder.unlockCanvasAndPost(fieldCanvas);

		Canvas previewCanvas = previewSurfaceHolder.lockCanvas();
		previewCanvas.drawColor(0, PorterDuff.Mode.CLEAR); // clear screen
		drawPreview(previewCanvas);
		previewSurfaceHolder.unlockCanvasAndPost(previewCanvas);
	}

	private void drawField(Canvas canvas) {
		// update background
		canvas.drawBitmap(bitmapManager.getFieldBackground(), 0, 0, BITMAP_PAINT);

		// update field
		Block[][] field = gameManager.getField();
		Block[][] partnerField = gameManager.getPartnerField();
		BlockGroup activeGroup = gameManager.getActiveGroup();
		BlockGroup partnerActiveGroup = gameManager.getPartnerActiveGroup();

		int xCount = field.length;
		int yCount = field[0].length;
		int xSize = canvas.getWidth() / xCount;
		int ySize = canvas.getHeight() / yCount;

		for (int xPos = 0; xPos < xCount; ++xPos) {
			for (int yPos = 0; yPos < yCount; ++yPos) {
				Block block;
				if (activeGroup != null) block = getNonNull(field[xPos][yPos], activeGroup.getBlockFromAbsolutePosition(xPos, yPos));
				else block = field[xPos][yPos];

				Block partnerBlock;
				if (partnerActiveGroup != null) partnerBlock = getNonNull(partnerField[xPos][yPos], partnerActiveGroup.getBlockFromAbsolutePosition(xPos, yPos));
				else partnerBlock = partnerField[xPos][yPos];

				BlockColor color;
				if (block == null && partnerBlock == null) continue;
				if (block != null && partnerBlock != null) {
					color = BlockColor.RED;
				} else if (block == null) {
					color = BlockColor.YELLOW;
				} else {
					color = BlockColor.BLUE;
				}
				canvas.drawBitmap(
						bitmapManager.getBlocks().getBitmap(color, getNonNull(block, partnerBlock).getBitmapType()),
						null,
						new Rect(
								xPos * xSize,
								(yCount - yPos - 1) * ySize,
								(xPos + 1) * xSize,
								(yCount - yPos) * ySize),
						BITMAP_PAINT);
			}
		}
	}


	private void drawPreview(Canvas canvas) {
		// update background
		canvas.drawBitmap(bitmapManager.getFieldBackground(), 0, 0, BITMAP_PAINT);

		BlockGroup group = gameManager.getNextGroup();
		int xCount = 3;
		int yCount = 4;
		int xSize = canvas.getWidth() / xCount;
		int ySize = canvas.getHeight() / yCount;
		int xOffset = (xCount - group.getXSize()) / 2;
		int yOffset = (yCount - group.getYSize()) / 2;

		drawBlockGroup(canvas, gameManager.getNextGroup(), xOffset, yOffset, xSize, ySize, yCount);
	}


	private void drawBlockGroup(Canvas canvas, BlockGroup group, int xOffset, int yOffset, int xSize, int ySize, int yCount) {
		for (int x = 0; x < group.getXSize(); ++x) {
			for (int y = 0; y < group.getYSize(); ++y) {
				Block block = group.getBlock(x, y);
				if (block == null) continue;
				int xPos = x + xOffset;
				int yPos = y + yOffset;

				canvas.drawBitmap(
						bitmapManager.getBlocks().getBitmap(BlockColor.BLUE, block.getBitmapType()),
						null,
						new Rect(
								xPos * xSize,
								(yCount - yPos - 1) * ySize,
								(xPos + 1) * xSize,
								(yCount - yPos) * ySize),
						BITMAP_PAINT);
			}
		}

	}


	private <T> T getNonNull(T value1, T value2) {
		if (value1 != null) return value1;
		else return value2;
	}

}
