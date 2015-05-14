package org.faudroids.doublestacks.ui;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;

import org.faudroids.doublestacks.core.Block;
import org.faudroids.doublestacks.core.GameManager;

class GraphicsManager {

	private final SurfaceHolder surfaceHolder;
	private final GameManager gameManager;

	private final Paint dummyPaint = new Paint();

	public GraphicsManager(SurfaceHolder surfaceHolder, GameManager gameManager) {
		this.surfaceHolder = surfaceHolder;
		this.gameManager = gameManager;
		this.dummyPaint.setColor(Color.BLUE);
	}


	public void redrawGraphis() {
		Canvas canvas = surfaceHolder.lockCanvas();
		canvas.drawColor(0, PorterDuff.Mode.CLEAR); // clear screen

		// update graphics
		Block[][] field = gameManager.getField();
		int xCount = field.length;
		int yCount = field[0].length;
		int xSize = canvas.getWidth() / xCount;
		int ySize = canvas.getHeight() / yCount;

		for (int xPos = 0; xPos < xCount; ++xPos) {
			for (int yPos = 0; yPos < yCount; ++yPos) {
				Block block = field[xPos][yPos];
				if (block == null) continue;
				canvas.drawRect(
						xPos * xSize,
						(yCount - yPos) * ySize,
						(xPos + 1) * xSize ,
						(yCount - yPos - 1) * ySize,
						dummyPaint);
			}
		}

		surfaceHolder.unlockCanvasAndPost(canvas);
	}

}
