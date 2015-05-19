package org.faudroids.doublestacks.core;


import android.os.Handler;
import android.os.Looper;

import java.io.Serializable;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Handles the core game logic.
 */
public class GameManager {

	// TODO put some awesome game logic here

	private final MessageManager messageManager;

	private GameUpdateListener gameUpdateListener = null;

	private Block[][] field = null;
	private int currentScore = 0;
	private GameTickRunnable tickRunnable = null;

	private BlockGroup activeGroup = null;


	@Inject
	GameManager(MessageManager messageManager) {
		this.messageManager = messageManager;
	}


	public Block[][] getField() {
		return field;
	}


	public BlockGroup getActiveGroup() {
		return activeGroup;
	}


	public int getCurrentScore() {
		return currentScore;
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
		if (activeGroup == null) return;

		// check wall
		if (activeGroup.getxPos() == 0) return;

		// check field
		for (Location blockLocation : activeGroup.getAbsoluteLocations()) {
			Block fieldBlock = field[blockLocation.x - 1][blockLocation.y];
			if (fieldBlock != null) return;
		}

		// update group
		activeGroup.setxPos(activeGroup.getxPos() - 1);
		sendUpdate(false);
		gameUpdateListener.onFieldChanged();
	}


	public void onRightClicked() {
		if (activeGroup == null) return;

		// check wall
		if (activeGroup.getxPos() + activeGroup.getXSize() == Constants.BLOCKS_COUNT_X) return;

		// check field
		for (Location blockLocation : activeGroup.getAbsoluteLocations()) {
			Block fieldBlock = field[blockLocation.x + 1][blockLocation.y];
			if (fieldBlock != null) return;
		}

		// update group
		activeGroup.setxPos(activeGroup.getxPos() + 1);
		sendUpdate(false);
		gameUpdateListener.onFieldChanged();
	}


	public void onRotateClicked() {
		// TODO awesome stuff goes here
	}


	public void onOneDownClicked() {
		if (activeGroup == null) return;
		moveActiveGroupDown();
		gameUpdateListener.onFieldChanged();
	}


	public void onAllDownClicked() {
		// TODO awesome stuff goes here
	}


	/**
	 * Called when sufficient time has passed that blocks should fall down.
	 */
	private void onGameTick() {
		if (activeGroup == null) {
			Timber.d("Creating new group");
			// create new group
			activeGroup = createRandomGroup();
			activeGroup.setyPos(Constants.BLOCKS_COUNT_Y - 1 - activeGroup.getYSize());
			int xPos = (Constants.BLOCKS_COUNT_X - activeGroup.getXSize()) / 2;
			activeGroup.setxPos(xPos);

		} else {
			moveActiveGroupDown();
		}

		// send full field update
		sendUpdate(true);

		// TODO remove this at some point
		++currentScore;

		// update listeners
		gameUpdateListener.onFieldChanged();
		gameUpdateListener.onScoreChanged();
	}


	public void onMsg(Serializable data, boolean isReliable) {
		/*
		FieldUpdate update = (FieldUpdate) data;
		if (!messageManager.receiveMessage(update, isReliable)) {
			Timber.d("dropping msg (" + update.getEpoch() + ", " + update.getSeqNum() + ")");
			return;
		}

		// update complete player 2 field
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

		gameUpdateListener.onFieldChanged();

		// TODO remove full lines here BUT only is message is reliable!!
		*/
	}


	private void moveActiveGroupDown() {
		boolean touchdown = false;
		if (activeGroup.getyPos() == 0) {
			touchdown = true;
		}

		if (!touchdown) {
			for (Location blockLocation : activeGroup.getAbsoluteLocations()) {
				Block fieldBlock = field[blockLocation.x][blockLocation.y - 1];
				if (fieldBlock != null) {
					touchdown = true;
					break;
				}
			}
		}

		if (touchdown) {
			// merge with field and release group
			for (int x = 0; x < activeGroup.getXSize(); ++x) {
				for (int y = 0; y < activeGroup.getYSize(); ++y) {
					Block block = activeGroup.getBlock(x, y);
					if (block == null) continue;
					field[activeGroup.getxPos() + x][activeGroup.getyPos() + y] = block;
				}
			}
			activeGroup = null;

		} else {
			// move group down
			activeGroup.setyPos(activeGroup.getyPos() - 1);
		}
	}


	private void sendUpdate(boolean isReliable) {
		/*
		FieldUpdate update = new FieldUpdate();
		for (int x = 0; x < Constants.BLOCKS_COUNT_X; ++x) {
			for (int y = 0; y < Constants.BLOCKS_COUNT_Y; ++y) {
				Block block = field[x][y];
				if (block == null) continue;
				if (block.getBlockType().equals(BlockType.PLAYER_1) || block.getBlockType().equals(BlockType.COMBINED)) {
					update.setBlock(x, y);
				}
			}
		}
		messageManager.sendMessage(update, isReliable);
		*/
	}


	private BlockGroup createRandomGroup() {
		// TODO random!!
		return BlockGroup.createBox();
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
