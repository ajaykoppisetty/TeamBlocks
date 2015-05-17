package org.faudroids.doublestacks.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;

import org.faudroids.doublestacks.R;
import org.faudroids.doublestacks.google.ConnectionManager;

import java.io.Serializable;
import java.util.ArrayList;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import timber.log.Timber;

public class MenuFragment extends AbstractFragment implements
		OnInvitationReceivedListener,
		ConnectionManager.ConnectionListener {

	private static final int
			REQUEST_INVITE = 43,
			REQUEST_WAITING_ROOM = 44,
			REQUEST_VIEW_INVITATIONS = 45;

	private static final String EXTRA_INVITATION = "EXTRA_INVITATION";

	/**
	 * @param invitation a pending invitation to join a game. Can be null if none is present.
	 */
	public static MenuFragment createInstance(Invitation invitation) {
		Bundle extras = new Bundle();
		extras.putParcelable(EXTRA_INVITATION, invitation);
		MenuFragment fragment = new MenuFragment();
		fragment.setArguments(extras);
		return fragment;
	}


	@Inject GoogleApiClient googleApiClient;
	@Inject ConnectionManager connectionManager;

	@InjectView(R.id.button_quick_game) Button quickGameButton;
	@InjectView(R.id.button_invite) Button inviteButton;
	@InjectView(R.id.button_view_invitations) Button viewInvitationsButton;


	public MenuFragment() {
		super(R.layout.fragment_menu);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// setup ui
		quickGameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSpinner();
				connectionManager.autoMatchPlayer();
			}
		});
		inviteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSpinner();
				// launch the player selection screen
				Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 1);
				startActivityForResult(intent, REQUEST_INVITE);
			}
		});
		viewInvitationsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSpinner();
				showInvitations();
			}
		});

		// listen for incoming invitations
		Games.Invitations.registerInvitationListener(googleApiClient, this);

		// check for pending invitations
		Invitation invitation = getArguments().getParcelable(EXTRA_INVITATION);
		if (invitation != null) {
			connectionManager.acceptInvitation(invitation);
		}
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
	public void onActivityResult(int request, int response, Intent data) {
		hideSpinner();
		switch (request) {
			case REQUEST_INVITE:
				if (response != Activity.RESULT_OK) return;

				// get the invitee list
				ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

				// get auto-match criteria
				int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
				int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

				connectionManager.invitePlayers(invitees, minAutoMatchPlayers, maxAutoMatchPlayers);
				break;

			case REQUEST_WAITING_ROOM:
				if (response == Activity.RESULT_OK) {
					Timber.d("all players connected, starting game ...");
					actionListener.onGameStarted();
					return;
				}

				if (response == Activity.RESULT_CANCELED || response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
					Timber.d("leaving room ...");
					// leave room
					Games.RealTimeMultiplayer.leave(googleApiClient, null, connectionManager.getConnectedRoom().getRoomId());
				}
				break;

			case REQUEST_VIEW_INVITATIONS:
				// canceled
				if (response != Activity.RESULT_OK) return;

				// get and accept the selected invitation
				Bundle extras = data.getExtras();
				Invitation invitation = extras.getParcelable(Multiplayer.EXTRA_INVITATION);
				connectionManager.acceptInvitation(invitation);
				break;
		}
	}


	@Override
	public void onInvitationReceived(Invitation invitation) {
		Timber.d("Invitation received");
		showInvitations();
	}


	@Override
	public void onInvitationRemoved(String s) {
		Timber.d("Invitation removed (" + s + ")");
	}


	@Override
	public void showWaitingRoom(Intent waitingRoomIntent) {
		startActivityForResult(waitingRoomIntent, REQUEST_WAITING_ROOM);
	}


	@Override
	public void onConnectionLost() {
		// nothing to do here ...
	}


	@Override
	public void onReliableMsgSendError() {
		// nothing to do here ...
	}


	@Override
	public void onMsg(Serializable data, boolean isReliable) {
		// nothing to do here ...
	}


	private void showInvitations() {
		Intent intent = Games.Invitations.getInvitationInboxIntent(googleApiClient);
		startActivityForResult(intent, REQUEST_VIEW_INVITATIONS);
	}

}
