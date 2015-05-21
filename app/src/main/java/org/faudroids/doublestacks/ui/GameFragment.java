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
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.InjectView;

public class GameFragment extends AbstractFragment implements
		ConnectionManager.ConnectionListener,
		GameUpdateListener {

	@InjectView(R.id.button_home) private ImageButton homeButton;
	@InjectView(R.id.button_left) private ImageButton leftButton;
	@InjectView(R.id.button_right) private ImageButton rightButton;
	@InjectView(R.id.button_turn) private ImageButton rotateButton;
	@InjectView(R.id.button_down_one) private ImageButton downOneButton;
	@InjectView(R.id.button_down_all) private ImageButton downAllButton;

	@InjectView(R.id.text_score) private TextView scoreView;

	@InjectView(R.id.surface_view_field) private SurfaceView fieldSurfaceView;
	@InjectView(R.id.surface_view_preview) private SurfaceView previewSurfaceView;

	@Inject private ConnectionManager connectionManager;
	@Inject private GameManager gameManager;
	@Inject private BitmapManager bitmapManager;
	private GraphicsManager graphicsManager = null;

	private SurfaceHolder fieldSurfaceHolder, previewSurfaceHolder;


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
								gameManager.stopGame();
								connectionManager.leaveRoom();
								actionListener.onGameStopped();
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
		SurfaceHolder fieldHolder = fieldSurfaceView.getHolder();
		fieldHolder.addCallback(new MultiSurfaceHolderCallback(true));
		fieldSurfaceView.setZOrderOnTop(true);
		fieldHolder.setFormat(PixelFormat.TRANSPARENT);

		SurfaceHolder previewHolder = previewSurfaceView.getHolder();
		previewHolder.addCallback(new MultiSurfaceHolderCallback(false));
		previewSurfaceView.setZOrderOnTop(true);
		previewHolder.setFormat(PixelFormat.TRANSPARENT);

		// start game
		String myId = connectionManager.getCurrentPlayerId();
		List<String> allIds = connectionManager.getPlayerIds();
		allIds.remove(myId);
		String partnerId = allIds.get(0);
		boolean isLeftPlayer = myId.compareTo(partnerId) < 0;
		gameManager.startGame(this, isLeftPlayer);
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
		if (!gameManager.isGameRunning()) return;
		gameManager.stopGame();
		connectionManager.leaveRoom();
		showDialog(new AlertDialog.Builder(getActivity())
				.setTitle(R.string.error_connection_lost_title)
				.setMessage(R.string.error_connection_lost_message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						actionListener.onGameStopped();
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						actionListener.onGameStopped();
					}
				})
				.create());
	}


	@Override
	public void onReliableMsgSendError() {
		gameManager.stopGame();
		connectionManager.leaveRoom();
		showDialog(new AlertDialog.Builder(getActivity())
				.setTitle(R.string.error_msg_not_delivered_title)
				.setMessage(R.string.error_msg_not_delivered_message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						actionListener.onGameStopped();
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						actionListener.onGameStopped();
					}
				})
				.create());
	}


	@Override
	public void onMsg(Serializable data, boolean isReliable) {
		gameManager.onMsg(data, isReliable);
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


	private void showDialog(Dialog dialog) {
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		dialog.show();
		dialog.getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility());
		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
	}


	/**
	 * Waits for all canvases to become ready before starting the {@link GraphicsManager}.
	 */
	private class MultiSurfaceHolderCallback implements SurfaceHolder.Callback {

		private final boolean isFieldCallback;

		public MultiSurfaceHolderCallback(boolean isFieldCallback) {
			this.isFieldCallback =isFieldCallback;

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if (isFieldCallback) fieldSurfaceHolder = holder;
			else previewSurfaceHolder = holder;

			if (fieldSurfaceHolder != null && previewSurfaceHolder != null) {
				graphicsManager = new GraphicsManager(fieldSurfaceHolder, previewSurfaceHolder, gameManager, bitmapManager, getResources());
				graphicsManager.redrawGraphics();
			}
		}


		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {  }


		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (isFieldCallback) fieldSurfaceHolder = null;
			else previewSurfaceHolder = null;
			graphicsManager = null;
		}

	}

}
