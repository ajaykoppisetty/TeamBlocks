package org.faudroids.doublestacks.ui;


import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

import org.faudroids.doublestacks.R;
import org.faudroids.doublestacks.core.GameManager;
import org.faudroids.doublestacks.core.GameTickListener;
import org.faudroids.doublestacks.google.ConnectionManager;

import java.io.Serializable;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import timber.log.Timber;

public class GameFragment extends AbstractFragment implements
		ConnectionManager.ConnectionListener,
		SurfaceHolder.Callback,
		GameTickListener {

	@InjectView(R.id.button_home) private ImageButton homeButton;

	@InjectView(R.id.surface_view) private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;

	@Inject private ConnectionManager connectionManager;
	@Inject private GameManager gameManager;
	@Inject private BitmapManager bitmapManager;
	private GraphicsManager graphicsManager = null;


	public GameFragment() {
		super(R.layout.fragment_game);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// setup ui
		homeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopGame();
			}
		});

		// setup drawing area
		SurfaceHolder holder = surfaceView.getHolder();
		holder.addCallback(this);
		surfaceView.setZOrderOnTop(true);
		holder.setFormat(PixelFormat.TRANSPARENT);

		// start game
		gameManager.registerGameTickListener(this);
		gameManager.startGame();
	}


	@Override
	public void onResume() {
		super.onResume();
		connectionManager.registerConnectionListener(this);
	}


	@Override
	public void onPause() {
		connectionManager.unregisterConnectionListener();
		super.onPause();
	}


	@Override
	public void showWaitingRoom(Intent waitingRoomIntent) {
		// nothing to do here ...
	}


	@Override
	public void onConnectionLost() {
		stopGame();
	}


	@Override
	public void onReliableMsgSendError() {
		Timber.e("failed to send reliable msgs");
	}


	@Override
	public void onMsg(Serializable data, boolean isReliable) {
		gameManager.onMsg(data, isReliable);
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.surfaceHolder = holder;
		this.graphicsManager = new GraphicsManager(surfaceHolder, gameManager, bitmapManager, getResources());
		this.graphicsManager.redrawGraphics();
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {  }


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.surfaceHolder = null;
		this.graphicsManager = null;
	}


	@Override
	public void onGameTick() {
		if (graphicsManager != null) graphicsManager.redrawGraphics();
	}


	private void stopGame() {
		connectionManager.leaveRoom();
		actionListener.onGameStopped();
		gameManager.stopGame();
	}
}
