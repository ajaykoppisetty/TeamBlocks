package org.faudroids.doublestacks.core;


import android.os.Handler;
import android.os.Looper;

import org.faudroids.doublestacks.google.MessageSender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Handles the core game logic.
 */
public class GameManager implements GameTickListener {

	// TODO put some awesome game logic here

	private final MessageSender messageSender;

	private final Block[][] field = new Block[Constants.BLOCKS_COUNT_X][Constants.BLOCKS_COUNT_Y];
	private final GameTickRunnable tickRunnable = new GameTickRunnable();

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
