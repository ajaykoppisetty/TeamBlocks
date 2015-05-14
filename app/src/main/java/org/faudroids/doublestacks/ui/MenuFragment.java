package org.faudroids.doublestacks.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import org.faudroids.doublestacks.R;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import timber.log.Timber;

public class MenuFragment extends AbstractFragment implements
		GoogleApiClient.ConnectionCallbacks,
		RoomUpdateListener,
		RealTimeMessageReceivedListener,
		OnInvitationReceivedListener {

	private static final int
			REQUEST_INVITE = 43,
			REQUEST_WAITING_ROOM = 44;

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
	@InjectView(R.id.button_invite) Button inviteButton;

	private Room connectedRoom = null;


	public MenuFragment() {
		super(R.layout.fragment_menu);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// setup ui
		inviteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// launch the player selection screen
				Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 1);
				startActivityForResult(intent, REQUEST_INVITE);
			}
		});

		// check for pending invitations
		Invitation invitation = getArguments().getParcelable(EXTRA_INVITATION);
		if (invitation != null) {
			// accept invitation and start game
			RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this)
					.setMessageReceivedListener(this)
					.setRoomStatusUpdateListener(new DummyRoomStatusUpdateListener());
			roomConfigBuilder.setInvitationIdToAccept(invitation.getInvitationId());
			Games.RealTimeMultiplayer.join(googleApiClient, roomConfigBuilder.build());
			actionListener.onGameStarted();
		}
	}


	@Override
	public void onActivityResult(int request, int response, Intent data) {
		switch (request) {
			case REQUEST_INVITE:
				if (response != Activity.RESULT_OK) return;

				// get the invitee list
				ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

				// get auto-match criteria
				int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
				int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

				Bundle autoMatchCriteria = null;
				if (minAutoMatchPlayers > 0) {
					autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
				} else {
					autoMatchCriteria = null;
				}

				// create the room and specify a variant if appropriate
				RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this)
						.setMessageReceivedListener(this)
						.setRoomStatusUpdateListener(new DummyRoomStatusUpdateListener());
				roomConfigBuilder.addPlayersToInvite(invitees);
				if (autoMatchCriteria != null) {
					roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
				}
				RoomConfig roomConfig = roomConfigBuilder.build();
				Games.RealTimeMultiplayer.create(googleApiClient, roomConfig);
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
					Games.RealTimeMultiplayer.leave(googleApiClient, null, connectedRoom.getRoomId());
				}
				break;
		}
	}


	@Override
	public void onConnected(Bundle bundle) {
		// TODO
		Games.Invitations.registerInvitationListener(googleApiClient, this);
	}


	@Override
	public void onConnectionSuspended(int i) {
		// TODO
	}


	@Override
	public void onStart() {
		super.onStart();
		googleApiClient.registerConnectionCallbacks(this);
		googleApiClient.connect();
	}


	@Override
	public void onStop() {
		googleApiClient.disconnect();
		googleApiClient.unregisterConnectionCallbacks(this);
		super.onStop();
	}


	@Override
	public void onRoomCreated(int statusCode, Room room) {
		this.connectedRoom = room;
		if (!onRoomUpdate("onRoomCreated", statusCode)) return;
		// get waiting room intent
		Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(googleApiClient, room, Integer.MAX_VALUE);
		startActivityForResult(intent, REQUEST_WAITING_ROOM);

	}


	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		this.connectedRoom = room;
		if (!onRoomUpdate("onJoinedRoom", statusCode)) return;
		// get waiting room intent
		Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(googleApiClient, room, Integer.MAX_VALUE);
		startActivityForResult(intent, REQUEST_WAITING_ROOM);
	}


	@Override
	public void onLeftRoom(int statusCode, String s) {
		onRoomUpdate("onLeftRoom", statusCode);
	}


	@Override
	public void onRoomConnected(int statusCode, Room room) {
		this.connectedRoom = room;
		onRoomUpdate("onRoomConnected", statusCode);
	}


	public boolean onRoomUpdate(String updateType, int statusCode) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			Timber.e(updateType + " error (" + statusCode + ")");
			return false;
		} else {
			Timber.d(updateType + " success");
			return true;
		}
	}


	@Override
	public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
		Timber.d("onRealTimeMessageReceived");
	}


	@Override
	public void onInvitationReceived(Invitation invitation) {
		Timber.d("Invitation received");
		RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this)
				.setMessageReceivedListener(this)
				.setRoomStatusUpdateListener(new DummyRoomStatusUpdateListener());
		roomConfigBuilder.setInvitationIdToAccept(invitation.getInvitationId());
		Games.RealTimeMultiplayer.join(googleApiClient, roomConfigBuilder.build());
	}


	@Override
	public void onInvitationRemoved(String s) {
		Timber.d("Invitation removed (" + s + ")");
	}


	private class DummyRoomStatusUpdateListener implements RoomStatusUpdateListener {
		@Override
		public void onRoomConnecting(Room room) {
			Timber.d("onRoomConnection");
		}

		@Override
		public void onRoomAutoMatching(Room room) {
			Timber.d("onRoomAutoMatching");
		}

		@Override
		public void onPeerInvitedToRoom(Room room, List<String> list) {
			Timber.d("onPeerInvitedToRoom");
		}

		@Override
		public void onPeerDeclined(Room room, List<String> list) {
			Timber.d("onPeerDeclined");
		}

		@Override
		public void onPeerJoined(Room room, List<String> list) {
			Timber.d("onPeerJoined");
		}

		@Override
		public void onPeerLeft(Room room, List<String> list) {
			Timber.d("onPeerLeft");
		}

		@Override
		public void onConnectedToRoom(Room room) {
			Timber.d("onConnectedToRoom");
		}

		@Override
		public void onDisconnectedFromRoom(Room room) {
			Timber.d("onDisconnectedFromRoom");
		}

		@Override
		public void onPeersConnected(Room room, List<String> list) {
			Timber.d("onPeersConnected");
		}

		@Override
		public void onPeersDisconnected(Room room, List<String> list) {
			Timber.d("onPeersDisconnected");
		}

		@Override
		public void onP2PConnected(String s) {
			Timber.d("onP2PConnected");
		}

		@Override
		public void onP2PDisconnected(String s) {
			Timber.d("onP2PDisconnected");
		}
	}
}
