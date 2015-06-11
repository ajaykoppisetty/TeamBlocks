package org.faudroids.doublestacks.core;

/**
 * Keeps track of much a {@link BlockGroup} was "shaken"
 * and resets the counter when necessary.
 */
public class ShakeMeasurement {

	private static final long MAX_UPDATE_DELTA = 400; // ms
	private static final int MIN_SHAKE_COUNT = 15;

	private int shakeCount = 0;
	private long lastUpdateTimestamp = 0; // ms
	private MovementType lastMovement = MovementType.NONE;


	/**
	 * Call this method when block group was moved left.
	 */
	public void onShakeLeft() {
		onShake(MovementType.LEFT);
	}


	/**
	 * Call this method when block group was moved right.
	 */
	public void onSkakeRight() {
		onShake(MovementType.RIGHT);
	}


	private void onShake(MovementType movement) {
		long currentTimestamp = System.currentTimeMillis();
		if (lastMovement.equals(movement) || (currentTimestamp - lastUpdateTimestamp) > MAX_UPDATE_DELTA) {
			shakeCount = 0;
		} else {
			++shakeCount;
		}
		lastUpdateTimestamp = currentTimestamp;
		lastMovement = movement;
	}


	/**
	 *  Returns true if when group was moved left + right often enough
	 *  to consider "shaking". This will reset once a certain
	 *  amount of time has passed.
	 */
	public boolean isShaking() {
		return System.currentTimeMillis() - lastUpdateTimestamp <= MAX_UPDATE_DELTA && shakeCount >= MIN_SHAKE_COUNT;
	}


	private enum MovementType {

		LEFT,
		RIGHT,
		NONE

	}


}
