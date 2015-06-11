package org.faudroids.doublestacks.ui;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.faudroids.doublestacks.R;
import org.faudroids.doublestacks.core.Constants;

/**
 * Bitmaps for various block variations and colors.
 */
public class BlockBitmaps {


	public static BlockBitmaps load(Resources resources) {
		BlockBitmaps b = new BlockBitmaps();
		b.bitmaps[0][0] = BitmapFactory.decodeResource(resources, R.drawable.block_blue_0);
		b.bitmaps[0][1] = BitmapFactory.decodeResource(resources, R.drawable.block_blue_1);
		b.bitmaps[0][2] = BitmapFactory.decodeResource(resources, R.drawable.block_blue_2);
		b.bitmaps[0][3] = BitmapFactory.decodeResource(resources, R.drawable.block_blue_3);
		b.bitmaps[0][4] = BitmapFactory.decodeResource(resources, R.drawable.block_blue_4);
		b.bitmaps[0][5] = BitmapFactory.decodeResource(resources, R.drawable.block_blue_5);
		b.bitmaps[0][6] = BitmapFactory.decodeResource(resources, R.drawable.block_blue_6);
		b.bitmaps[0][7] = BitmapFactory.decodeResource(resources, R.drawable.block_blue_7);
		b.bitmaps[1][0] = BitmapFactory.decodeResource(resources, R.drawable.block_yellow_0);
		b.bitmaps[1][1] = BitmapFactory.decodeResource(resources, R.drawable.block_yellow_1);
		b.bitmaps[1][2] = BitmapFactory.decodeResource(resources, R.drawable.block_yellow_2);
		b.bitmaps[1][3] = BitmapFactory.decodeResource(resources, R.drawable.block_yellow_3);
		b.bitmaps[1][4] = BitmapFactory.decodeResource(resources, R.drawable.block_yellow_4);
		b.bitmaps[1][5] = BitmapFactory.decodeResource(resources, R.drawable.block_yellow_5);
		b.bitmaps[1][6] = BitmapFactory.decodeResource(resources, R.drawable.block_yellow_6);
		b.bitmaps[1][7] = BitmapFactory.decodeResource(resources, R.drawable.block_yellow_7);
		b.bitmaps[2][0] = BitmapFactory.decodeResource(resources, R.drawable.block_red_0);
		b.bitmaps[2][1] = BitmapFactory.decodeResource(resources, R.drawable.block_red_1);
		b.bitmaps[2][2] = BitmapFactory.decodeResource(resources, R.drawable.block_red_2);
		b.bitmaps[2][3] = BitmapFactory.decodeResource(resources, R.drawable.block_red_3);
		b.bitmaps[2][4] = BitmapFactory.decodeResource(resources, R.drawable.block_red_4);
		b.bitmaps[2][5] = BitmapFactory.decodeResource(resources, R.drawable.block_red_5);
		b.bitmaps[2][6] = BitmapFactory.decodeResource(resources, R.drawable.block_red_6);
		b.bitmaps[2][7] = BitmapFactory.decodeResource(resources, R.drawable.block_red_7);
		return b;
	}


	private final Bitmap[][] bitmaps = new Bitmap[3][Constants.BLOCK_TYPE_COUNT]; // types x variations

	private BlockBitmaps() { }

	public Bitmap getBitmap(BlockColor color, int variation) {
		int typePos = -1;
		if (color.equals(BlockColor.BLUE)) typePos = 0;
		else if (color.equals(BlockColor.YELLOW)) typePos = 1;
		else if (color.equals(BlockColor.RED)) typePos = 2;
		return bitmaps[typePos][variation];
	}

}
