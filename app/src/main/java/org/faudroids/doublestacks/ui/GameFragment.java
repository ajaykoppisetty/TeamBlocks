package org.faudroids.doublestacks.ui;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.faudroids.doublestacks.R;
import org.faudroids.doublestacks.google.ConnectionManager;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import timber.log.Timber;

public class GameFragment extends AbstractFragment implements ConnectionManager.ConnectionListener {

	@InjectView(R.id.button_stop_game) private Button stopGameButton;
	@InjectView(R.id.msgs_reliable) private TextView reliableMsgsView;
	@InjectView(R.id.msgs_unreliable) private TextView unreliableMsgsView;

	@Inject private ConnectionManager connectionManager;

	private boolean sendingMsgs = true;


	public GameFragment() {
		super(R.layout.fragment_game);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// setup ui
		stopGameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopGame();
			}
		});

		// periodic msg sending (for testing)
		final Handler handler = new Handler(getActivity().getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				connectionManager.sendMessage("Hello " + Math.random(), false);
				connectionManager.sendMessage("World " + Math.random(), true);
				if (sendingMsgs) handler.postDelayed(this, 1000);
			}
		}, 1000);
	}


	@Override
	public void onResume() {
		super.onResume();
		connectionManager.registerConnectionListener(this);
	}


	@Override
	public void onPause() {
		connectionManager.unregisterConnectionListener();
		sendingMsgs = false;
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
	public void onReliableMsg(String msg) {
		reliableMsgsView.setText(msg);
	}


	@Override
	public void onUnreliableMsg(String msg) {
		unreliableMsgsView.setText(msg);
	}


	private void stopGame() {
		connectionManager.leaveRoom();
		actionListener.onGameStopped();
	}

}
