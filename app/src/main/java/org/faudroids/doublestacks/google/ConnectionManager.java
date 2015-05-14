package org.faudroids.doublestacks.google;


import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
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
	private final CustomRoomStatusUpdateListener roomStatusUpdateListener = new CustomRoomStatusUpdateListener();
	private final CustomMessageSentCallback messageSentCallback = new CustomMessageSentCallback();

	private final GoogleApiClient googleApiClient;

	private Room connectedRoom = null;
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
		Games.RealTimeMultiplayer.leave(googleApiClient, roomUpdateListener, connectedRoom.getRoomId());
	}


	public Room getConnectedRoom() {
		return connectedRoom;
	}


	public void sendMessage(String msg, boolean reliable) {
		ArrayList<String> participantsIds = connectedRoom.getParticipantIds();
		String myId = connectedRoom.getParticipantId(Games.Players.getCurrentPlayerId(googleApiClient));
		for (String id : participantsIds) {
			if (myId.equals(id)) continue;
			if (reliable) {
				Games.RealTimeMultiplayer.sendReliableMessage(
						googleApiClient,
						messageSentCallback,
						msg.getBytes(),
						connectedRoom.getRoomId(),
						id);
			} else {
				Games.RealTimeMultiplayer.sendUnreliableMessage(
						googleApiClient,
						msg.getBytes(),
						connectedRoom.getRoomId(),
						id);
			}

		}
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
		void onReliableMsgSendError();
		void onReliableMsg(String msg);
		void onUnreliableMsg(String msg);
	}


	private class CustomMessageSentCallback implements RealTimeMultiplayer.ReliableMessageSentCallback {

		@Override
		public void onRealTimeMessageSent(int statusCode, int tokenId, String participantId) {
			if (statusCode == GamesStatusCodes.STATUS_OK) return;
			if (statusCode == GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED) connectionListener.onReliableMsgSendError();
			if (statusCode == GamesStatusCodes.STATUS_REAL_TIME_ROOM_NOT_JOINED) Timber.w("trying to send msg to unregister player");
		}

	}


	private class CustomRoomUpdateListener implements RoomUpdateListener {

		@Override
		public void onRoomCreated(int statusCode, Room room) {
			connectedRoom = room;
			if (!onRoomUpdate("onRoomCreated", statusCode)) return;
			showWaitingRoom();
		}


		@Override
		public void onJoinedRoom(int statusCode, Room room) {
			connectedRoom = room;
			if (!onRoomUpdate("onJoinedRoom", statusCode)) return;
			showWaitingRoom();
		}


		@Override
		public void onLeftRoom(int statusCode, String s) {
			connectedRoom = null;
			onRoomUpdate("onLeftRoom", statusCode);
		}


		@Override
		public void onRoomConnected(int statusCode, Room room) {
			connectedRoom = room;
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
			String msg = new String(realTimeMessage.getMessageData());
			if (realTimeMessage.isReliable()) connectionListener.onReliableMsg(msg);
			else connectionListener.onUnreliableMsg(msg);
		}

	}


	private class CustomRoomStatusUpdateListener implements RoomStatusUpdateListener {
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
		public void onPeerJoined(Room room, List<String> paricipantsIds) {
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
		public void onPeersConnected(Room room, List<String> participantIds) {
			Timber.d("onPeersConnected");
		}

		@Override
		public void onPeersDisconnected(Room room, List<String> participantIds) {
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