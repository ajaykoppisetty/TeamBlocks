package org.faudroids.doublestacks.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.faudroids.doublestacks.R;
import org.faudroids.doublestacks.google.ConnectionManager;

import javax.inject.Inject;

import roboguice.inject.InjectView;

public class GameFragment extends AbstractFragment implements ConnectionManager.ConnectionListener {

	@InjectView(R.id.button_stop_game) Button stopGameButton;
	@Inject ConnectionManager connectionManager;


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


	private void stopGame() {
		connectionManager.leaveRoom();
		actionListener.onGameStopped();
	}

}
