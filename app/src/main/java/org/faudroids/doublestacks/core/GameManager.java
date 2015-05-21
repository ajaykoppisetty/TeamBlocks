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

	private static final int SCORE_PER_ROW = 10;

	// TODO put some awesome game logic here

	private final MessageManager messageManager;

	private GameUpdateListener gameUpdateListener = null;

	private int currentScore = 0;
	private GameTickRunnable tickRunnable = null;

	private Block[][] field = null;

	private BlockGroup activeGroup = null;
	private BlockGroup nextGroup = null;

	private Block[][] partnerField = null;
	private BlockGroup partnerActiveGroup = null;


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


	public BlockGroup getNextGroup() {
		return nextGroup;
	}


	public BlockGroup getPartnerActiveGroup() {
		return partnerActiveGroup;
	}


	public Block[][] getPartnerField() {
		return partnerField;
	}


	public int getCurrentScore() {
		return currentScore;
	}


	public void startGame(GameUpdateListener gameUpdateListener) {
		Timber.d("starting game");
		this.gameUpdateListener = gameUpdateListener;
		this.field = new Block[Constants.BLOCKS_COUNT_X][Constants.BLOCKS_COUNT_Y];
		this.partnerField = new Block[Constants.BLOCKS_COUNT_X][Constants.BLOCKS_COUNT_Y];
		this.tickRunnable = new GameTickRunnable();
		this.nextGroup = BlockGroup.createRandom();
		new Thread(tickRunnable).start();
	}


	public void stopGame() {
		Timber.d("stopping game");
		this.tickRunnable.stop();
		this.tickRunnable = null;
		this.gameUpdateListener = null;
		this.field = null;
		this.partnerField = null;
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
		sendUpdate();
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
		sendUpdate();
		gameUpdateListener.onFieldChanged();
	}


	public void onRotateClicked() {
		if (activeGroup == null) return;

		// check field
		BlockGroup rotatedGroup = activeGroup.rotate();
		for (Location blockLocation : rotatedGroup.getAbsoluteLocations()) {
			if (blockLocation.x < 0 || blockLocation.x >= Constants.BLOCKS_COUNT_X ||
					blockLocation.y < 0 || blockLocation.y >= Constants.BLOCKS_COUNT_Y) {
				return;
			}

			Block block = field[blockLocation.x][blockLocation.y];
			if (block != null) return;
		}

		// update group
		activeGroup = rotatedGroup;
		sendUpdate();
		gameUpdateListener.onFieldChanged();
	}


	public void onOneDownClicked() {
		if (activeGroup == null) return;

		boolean rowRemoved= !moveActiveGroupDown();
		if (rowRemoved) {
			sendUpdate(true);
			checkAndRemoveCompletedLines();
		} else {
			sendUpdate(false);
		}
		gameUpdateListener.onFieldChanged();
	}


	public void onAllDownClicked() {
		if (activeGroup == null) return;
		while (moveActiveGroupDown()); // move all the way down
		sendUpdate(true);
		checkAndRemoveCompletedLines();
		gameUpdateListener.onFieldChanged();
	}


	/**
	 * Called when sufficient time has passed that blocks should fall down.
	 */
	private void onGameTick() {
		// create / move active group
		if (activeGroup == null) {
			Timber.d("Creating new group");
			// create new group
			activeGroup = nextGroup;
			nextGroup = BlockGroup.createRandom();
			activeGroup.setyPos(Constants.BLOCKS_COUNT_Y - activeGroup.getYSize());
			int xPos = (Constants.BLOCKS_COUNT_X - activeGroup.getXSize()) / 2;
			activeGroup.setxPos(xPos);

			// check for game end
			for (Location blockLocation : activeGroup.getAbsoluteLocations()) {
				if (field[blockLocation.x][blockLocation.y] != null) {
					gameUpdateListener.onGameOver();
					stopGame();
					return;
				}
			}

			sendUpdate(true);

		} else {
			boolean removedRow = !moveActiveGroupDown();
			sendUpdate(true);
			if (removedRow) checkAndRemoveCompletedLines();
		}

		// update listeners
		gameUpdateListener.onFieldChanged();
	}


	/**
	 * @return true if the block has moved, false otherwise (touchdown!)
	 */
	private boolean moveActiveGroupDown() {
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
			return false;

		} else {
			// move group down
			activeGroup.setyPos(activeGroup.getyPos() - 1);
			return true;
		}
	}


	private void checkAndRemoveCompletedLines() {
		int removedRows = 0;

		rowLabel : for (int y = 0; y < Constants.BLOCKS_COUNT_Y; ++y) {
			for (int x = 0; x < Constants.BLOCKS_COUNT_X; ++x) {
				if (field[x][y] == null || partnerField[x][y] == null) continue rowLabel;
			}

			removeRow(y);
			++removedRows;
			--y;
		}

		// update score
		if (removedRows > 0) {
			currentScore += removedRows * SCORE_PER_ROW;
			gameUpdateListener.onScoreChanged();
		}
	}


	private void removeRow(int row) {
		// remove row
		for (int x = 0; x < Constants.BLOCKS_COUNT_X; ++x) {
			field[x][row] = null;
			partnerField[x][row] = null;
		}

		// move top down
		int aboveRow = row + 1;
		while (aboveRow < Constants.BLOCKS_COUNT_Y) {
			for (int x = 0; x < Constants.BLOCKS_COUNT_X; ++x) {
				field[x][aboveRow - 1] = field[x][aboveRow];
				partnerField[x][aboveRow - 1] = partnerField[x][aboveRow];
			}
			++aboveRow;
		}
	}


	public void onMsg(Serializable data, boolean isReliable) {
		// if game not running ignore msg
		if (tickRunnable == null) return;

		// parse and check msg
		FieldUpdate update = (FieldUpdate) data;
		if (!messageManager.receiveMessage(update, isReliable)) {
			Timber.d("dropping msg (" + update.getEpoch() + ", " + update.getSeqNum() + ")");
			return;
		}

		// update complete partner field
		if (isReliable) {
			partnerField = update.getField();
			checkAndRemoveCompletedLines();
		}

		// update active group
		partnerActiveGroup = update.getActiveGroup();

		gameUpdateListener.onFieldChanged();
	}


	private void sendUpdate(boolean isReliable) {
		FieldUpdate update;
		if (isReliable) update = new FieldUpdate(activeGroup, field);
		else update = new FieldUpdate(activeGroup);
		messageManager.sendMessage(update, isReliable);
	}


	private void sendUpdate() {
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
