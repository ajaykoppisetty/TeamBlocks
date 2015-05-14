package org.faudroids.doublestacks.google;


import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.ContextSingleton;
import timber.log.Timber;

@ContextSingleton
public class ConnectionManager {

	private final CustomRoomUpdateListener roomUpdateListener = new CustomRoomUpdateListener();
	private final CustomMessageListeners messageListeners = new CustomMessageListeners();
	private final DummyRoomStatusUpdateListener roomStatusUpdateListener = new DummyRoomStatusUpdateListener();

	private final GoogleApiClient googleApiClient;

	private ConnectionListener connectionListener = null;

	@Inject
	ConnectionManager(GoogleApiClient googleApiClient) {
		this.googleApiClient = googleApiClient;
	}


	public void acceptInvitation(Invitation invitation) {
		// accept invitation and start game
		RoomConfig.Builder roomConfigBuilder = createDefaultRoomConfig();
		roomConfigBuilder.setInvitationIdToAccept(invitation.getInvitationId());
		Games.RealTimeMultiplayer.join(googleApiClient, roomConfigBuilder.build());
	}


	public void invitePlayers(ArrayList<String> playerIds, int minAutoMatchPlayers, int maxAutoMatchPlayers) {
		// create the room
		Bundle autoMatchCriteria = null;
		if (minAutoMatchPlayers > 0) {
			autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
		}

		RoomConfig.Builder roomConfigBuilder = createDefaultRoomConfig();
		roomConfigBuilder.addPlayersToInvite(playerIds);
		if (autoMatchCriteria != null) {
			roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
		}
		RoomConfig roomConfig = roomConfigBuilder.build();
		Games.RealTimeMultiplayer.create(googleApiClient, roomConfig);
	}


	public void leaveRoom() {
		Games.RealTimeMultiplayer.leave(googleApiClient, roomUpdateListener, roomUpdateListener.connectedRoom.getRoomId());
	}


	public Room getConnectedRoom() {
		return roomUpdateListener.connectedRoom;
	}


	public void registerConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}


	public void unregisterConnectionListener() {
		this.connectionListener = null;
	}


	private RoomConfig.Builder createDefaultRoomConfig() {
		return RoomConfig
				.builder(roomUpdateListener)
				.setRoomStatusUpdateListener(roomStatusUpdateListener)
				.setMessageReceivedListener(messageListeners);
	}


	public interface ConnectionListener {

		void showWaitingRoom(Intent waitingRoomIntent);
		void onConnectionLost();

	}


	private class CustomRoomUpdateListener implements RoomUpdateListener {

		private Room connectedRoom = null;

		@Override
		public void onRoomCreated(int statusCode, Room room) {
			this.connectedRoom = room;
			if (!onRoomUpdate("onRoomCreated", statusCode)) return;
			showWaitingRoom();
		}


		@Override
		public void onJoinedRoom(int statusCode, Room room) {
			this.connectedRoom = room;
			if (!onRoomUpdate("onJoinedRoom", statusCode)) return;
			showWaitingRoom();
		}


		@Override
		public void onLeftRoom(int statusCode, String s) {
			this.connectedRoom = null;
			onRoomUpdate("onLeftRoom", statusCode);
		}


		@Override
		public void onRoomConnected(int statusCode, Room room) {
			this.connectedRoom = room;
			onRoomUpdate("onRoomConnected", statusCode);
		}


		private boolean onRoomUpdate(String updateType, int statusCode) {
			if (statusCode != GamesStatusCodes.STATUS_OK) {
				Timber.e(updateType + " error (" + statusCode + ")");
				return false;
			} else {
				Timber.d(updateType + " success");
				return true;
			}
		}


		private void showWaitingRoom() {
			// get waiting room intent
			Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(googleApiClient, connectedRoom, Integer.MAX_VALUE);
			connectionListener.showWaitingRoom(intent);
		}

	}


	private class CustomMessageListeners implements RealTimeMessageReceivedListener {

		@Override
		public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
			Timber.d("on real time message");
		}

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
			connectionListener.onConnectionLost();
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
