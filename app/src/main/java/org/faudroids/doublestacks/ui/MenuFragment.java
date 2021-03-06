package org.faudroids.doublestacks.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;

import org.faudroids.doublestacks.R;
import org.faudroids.doublestacks.google.AchievementManager;
import org.faudroids.doublestacks.google.ConnectionManager;

import java.io.Serializable;
import java.util.ArrayList;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import timber.log.Timber;

public class MenuFragment extends AbstractFragment implements
		ConnectionManager.ConnectionListener {

	private static final int
			REQUEST_INVITE = 43,
			REQUEST_WAITING_ROOM = 44,
			REQUEST_VIEW_INVITATIONS = 45,
			REQUEST_LEADERBOARD = 46;

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


	@Inject private GoogleApiClient googleApiClient;
	@Inject private ConnectionManager connectionManager;
	@Inject private AchievementManager achievementManager;

	@InjectView(R.id.button_quick_game) private Button quickGameButton;
	@InjectView(R.id.button_invite) private Button inviteButton;
	@InjectView(R.id.button_view_invitations) private Button viewInvitationsButton;
	@InjectView(R.id.button_highscore) private Button highScoreButton;
	@InjectView(R.id.button_settings) private ImageButton settingsButton;
	@InjectView(R.id.button_share) private ImageButton shareButton;
	@InjectView(R.id.button_exit) private ImageButton exitButton;


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
				spinnerUtils.showSpinner();
				connectionManager.autoMatchPlayer();
			}
		});
		inviteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				spinnerUtils.showSpinner();
				// launch the player selection screen
				Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 1);
				startActivityForResult(intent, REQUEST_INVITE);
			}
		});
		viewInvitationsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				spinnerUtils.showSpinner();
				showInvitations();
			}
		});
		highScoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(achievementManager.getHighScoreIntent(), REQUEST_LEADERBOARD);
			}
		});
		exitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				String shareBody = getString(R.string.share_message);
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_title)));
			}
		});
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actionListener.onSettingsClicked();
			}
		});

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
		if (spinnerUtils.isSpinnerVisible()) spinnerUtils.hideSpinner();
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
					connectionManager.leaveRoom();
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

			case REQUEST_LEADERBOARD:
				// nothing to do
				break;
		}
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

	@Override
	public void onNewInvitation() {
		showInvitations();
	}


	private void showInvitations() {
		Intent intent = Games.Invitations.getInvitationInboxIntent(googleApiClient);
		startActivityForResult(intent, REQUEST_VIEW_INVITATIONS);
	}

}
