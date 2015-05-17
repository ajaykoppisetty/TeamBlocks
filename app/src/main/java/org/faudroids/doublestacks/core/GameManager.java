package org.faudroids.doublestacks.core;


import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Handles the core game logic.
 */
public class GameManager implements GameTickListener {

	// TODO put some awesome game logic here

	private final Block[][] field = new Block[Constants.BLOCKS_COUNT_X][Constants.BLOCKS_COUNT_Y];
	private final GameTickRunnable tickRunnable = new GameTickRunnable();

	// TODO this should probably be a group at some point
	private Block activeBlock = null;
	private int activeBlockXPos, activeBlockYPos;


	@Inject
	GameManager() {
		// TODO remove at some point
		/*
		// some blocks for testing
		for (int x = 0; x < Constants.BLOCKS_COUNT_X; ++x) {
			for (int y = 0; y < Constants.BLOCKS_COUNT_Y - 5; ++y) {
				int bitmapType = (int) (Math.random() * Constants.BLOCK_TYPE_COUNT);
				BlockType blockType = BlockType.COMBINED;
				double random = Math.random();
				if (random < 0.333) blockType = BlockType.PLAYER_1;
				else if (random < 0.666) blockType = BlockType.PLAYER_2;
				field[x][y] = new Block(bitmapType, blockType);
			}
		}
		*/
	}


	public Block[][] getField() {
		return field;
	}


	public void registerGameTickListener(GameTickListener listener) {
		tickRunnable.registerGameTickListener(listener);
	}


	public void startGame() {
		Timber.d("starting game");
		tickRunnable.registerGameTickListener(this);
		new Thread(tickRunnable).start();
	}


	public void stopGame() {
		Timber.d("stopping game");
		tickRunnable.stop();
	}


	/**
	 * Called when sufficient time has passed that blocks should fall down.
	 */
	@Override
	public void onGameTick() {
		if (activeBlock == null) {
			// create new block
			activeBlock = createRandomBlock();
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
	}


	private Block createRandomBlock() {
		int bitmapType = (int) (Math.random() * Constants.BLOCK_TYPE_COUNT);
		BlockType blockType = BlockType.COMBINED;
		double random = Math.random();
		if (random < 0.333) blockType = BlockType.PLAYER_1;
		else if (random < 0.666) blockType = BlockType.PLAYER_2;
		return new Block(bitmapType, blockType);
	}


	/**
	 * Alerts the {@link GameManager} about new time ticks.
	 */
	private static final class GameTickRunnable implements Runnable {

		private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
		private final List<GameTickListener> listenerList = new ArrayList<>();
		private volatile boolean running = true;

		public void registerGameTickListener(GameTickListener listener) {
			listenerList.add(listener);
		}

		public void stop() {
			this.running = false;
			this.listenerList.clear();
		}

		@Override
		public void run() {
			try {
				while (running) {
					Timber.d("game tick");
					mainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							for (GameTickListener listener : listenerList) listener.onGameTick();
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
