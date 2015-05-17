package org.faudroids.doublestacks.core;


import android.os.Handler;
import android.os.Looper;

import org.faudroids.doublestacks.google.MessageSender;

import java.io.Serializable;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Handles the core game logic.
 */
public class GameManager {

	// TODO put some awesome game logic here

	private final MessageSender messageSender;

	private GameUpdateListener gameUpdateListener = null;

	private Block[][] field = null;
	private GameTickRunnable tickRunnable = null;

	// TODO this should probably be a group at some point
	private Block activeBlock = null;
	private int activeBlockXPos, activeBlockYPos;


	@Inject
	GameManager(MessageSender messageSender) {
		this.messageSender = messageSender;
	}


	public Block[][] getField() {
		return field;
	}


	public void startGame(GameUpdateListener gameUpdateListener) {
		Timber.d("starting game");
		this.gameUpdateListener = gameUpdateListener;
		this.field = new Block[Constants.BLOCKS_COUNT_X][Constants.BLOCKS_COUNT_Y];
		this.tickRunnable = new GameTickRunnable();
		new Thread(tickRunnable).start();
	}


	public void stopGame() {
		Timber.d("stopping game");
		tickRunnable.stop();
		this.gameUpdateListener = null;
		this.field = null;
		this.tickRunnable = null;
	}


	public void onLeftClicked() {
		field[activeBlockXPos][activeBlockYPos] = null;
		activeBlockXPos = Math.max(0, activeBlockXPos -1);
		field[activeBlockXPos][activeBlockYPos] = activeBlock;
		gameUpdateListener.onRedrawGraphics();
	}


	public void onRightClicked() {
		field[activeBlockXPos][activeBlockYPos] = null;
		activeBlockXPos = Math.min(Constants.BLOCKS_COUNT_X - 1, activeBlockXPos + 1);
		field[activeBlockXPos][activeBlockYPos] = activeBlock;
		gameUpdateListener.onRedrawGraphics();
	}


	public void onRotateClicked() {
		// TODO awesome stuff goes here
	}


	public void onOneDownClicked() {
		// TODO awesome stuff goes here
	}


	public void onAllDownClicked() {
		// TODO awesome stuff goes here
	}


	/**
	 * Called when sufficient time has passed that blocks should fall down.
	 */
	private void onGameTick() {
		// update blocks
		if (activeBlock == null) {
			// create new block
			activeBlock = createRandomBlock(true);
			activeBlockXPos = (int) (Math.random() * Constants.BLOCKS_COUNT_X);
			activeBlockYPos = Constants.BLOCKS_COUNT_Y - 1;
			field[activeBlockXPos][activeBlockYPos] = activeBlock;

		} else {
			// move existing block
			field[activeBlockXPos][activeBlockYPos] = null;
			--activeBlockYPos;
			field[activeBlockXPos][activeBlockYPos] = activeBlock;
			if (activeBlockYPos == 0) activeBlock = null;
		}

		// send full field update
		FullFieldUpdate update = new FullFieldUpdate();
		for (int x = 0; x < Constants.BLOCKS_COUNT_X; ++x) {
			for (int y = 0; y < Constants.BLOCKS_COUNT_Y; ++y) {
				Block block = field[x][y];
				if (block == null) continue;
				if (block.getBlockType().equals(BlockType.PLAYER_1) || block.getBlockType().equals(BlockType.COMBINED)) {
					update.setBlock(x, y);
				}
			}
		}
		messageSender.sendMessage(update, true);

		// update listeners
		gameUpdateListener.onRedrawGraphics();
	}


	public void onMsg(Serializable data, boolean isReliable) {
		if (isReliable) {
			// update complete player 2 field
			FullFieldUpdate update = (FullFieldUpdate) data;
			for (int x = 0; x < Constants.BLOCKS_COUNT_X; ++x) {
				for (int y = 0; y < Constants.BLOCKS_COUNT_Y; ++y) {
					Block block = field[x][y];
					if (update.hasBlock(x, y)) {
						field[x][y] = createRandomBlock(false);
					} else if (block != null && block.getBlockType().equals(BlockType.PLAYER_2)) {
						field[x][y] = null;
					}
				}
			}
		}
	}


	private Block createRandomBlock(boolean player1) {
		int bitmapType = (int) (Math.random() * Constants.BLOCK_TYPE_COUNT);
		if (player1) return new Block(bitmapType, BlockType.PLAYER_1);
		else return new Block(bitmapType, BlockType.PLAYER_2);
	}


	/**
	 * Alerts the {@link GameManager} about new time ticks.
	 */
	private final class GameTickRunnable implements Runnable {

		private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
		private volatile boolean running = true;

		public void stop() {
			this.running = false;
		}

		@Override
		public void run() {
			try {
				while (running) {
					mainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							onGameTick();
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// nothing to do
					}
				}
			} catch (Throwable t) {
				Timber.e(t, "error in tick runnable");
			}
		}

	}

}
