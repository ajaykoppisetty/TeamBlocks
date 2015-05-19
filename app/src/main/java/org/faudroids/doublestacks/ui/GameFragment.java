package org.faudroids.doublestacks.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import org.faudroids.doublestacks.R;
import org.faudroids.doublestacks.core.GameManager;
import org.faudroids.doublestacks.core.GameUpdateListener;
import org.faudroids.doublestacks.google.ConnectionManager;

import java.io.Serializable;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import timber.log.Timber;

public class GameFragment extends AbstractFragment implements
		ConnectionManager.ConnectionListener,
		SurfaceHolder.Callback,
		GameUpdateListener {

	@InjectView(R.id.button_home) private ImageButton homeButton;
	@InjectView(R.id.button_left) private ImageButton leftButton;
	@InjectView(R.id.button_right) private ImageButton rightButton;
	@InjectView(R.id.button_turn) private ImageButton rotateButton;
	@InjectView(R.id.button_down_one) private ImageButton downOneButton;
	@InjectView(R.id.button_down_all) private ImageButton downAllButton;

	@InjectView(R.id.text_score) private TextView scoreView;

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
				showDialog(new AlertDialog.Builder(getActivity())
						.setTitle(R.string.quit_game_title)
						.setMessage(R.string.quit_game_message)
						.setPositiveButton(R.string.quit_game_action_quit, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								stopGame();
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.create());
			}
		});
		leftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gameManager.onLeftClicked();
			}
		});
		rightButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gameManager.onRightClicked();
			}
		});
		rotateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gameManager.onRotateClicked();
			}
		});
		downOneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gameManager.onOneDownClicked();
			}
		});
		downAllButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gameManager.onAllDownClicked();
			}
		});

		// setup drawing area
		SurfaceHolder holder = surfaceView.getHolder();
		holder.addCallback(this);
		surfaceView.setZOrderOnTop(true);
		holder.setFormat(PixelFormat.TRANSPARENT);

		// start game
		gameManager.startGame(this);
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
	public void onFieldChanged() {
		if (graphicsManager != null) graphicsManager.redrawGraphics();
	}


	@Override
	public void onScoreChanged() {
		scoreView.setText(String.valueOf(gameManager.getCurrentScore()));
	}


	@Override
	public void onGameOver() {
		showDialog(new AlertDialog.Builder(getActivity())
				.setTitle(R.string.game_over_title)
				.setMessage(getString(R.string.game_over_message, gameManager.getCurrentScore()))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						connectionManager.leaveRoom();
						actionListener.onGameStopped();
					}
				})
				.setCancelable(false)
				.create());
	}


	private void stopGame() {
		connectionManager.leaveRoom();
		actionListener.onGameStopped();
		gameManager.stopGame();
	}


	private void showDialog(Dialog dialog) {
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		dialog.show();
		dialog.getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility());
		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}

}
