package org.faudroids.doublestacks.ui;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.faudroids.doublestacks.R;

import javax.inject.Inject;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import timber.log.Timber;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity implements
		ActionListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private static final int REQUEST_LOGIN = 42;

	private boolean resolvingConnectionFailure = false;
	private boolean autoStartLoginFlow = true;
	private boolean loginClicked = false;

	@Inject private GoogleApiClient googleApiClient;


	@Override
	public void onStart() {
		super.onStart();
		Timber.d("connecting to google api client");
		// prevent lost connection when screen sleeps
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		googleApiClient.registerConnectionCallbacks(this);
		googleApiClient.registerConnectionFailedListener(this);
		googleApiClient.connect();
	}


	@Override
	public void onStop() {
		Timber.d("disconnecting from google api client");
		googleApiClient.disconnect();
		googleApiClient.unregisterConnectionCallbacks(this);
		googleApiClient.unregisterConnectionFailedListener(this);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onStop();
	}


	@Override
	public void onConnected(Bundle connectionHint) {
		Timber.d("Google login successful");

		// check if pending invitation
		Invitation invitation = null;
		if (connectionHint != null) {
			invitation = connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);
		}
		showFragment(MenuFragment.createInstance(invitation), true);
	}


	@Override
	public void onConnectionSuspended(int i) {
		// nothing to do for now
		Timber.d("Connection suspended (" + i + ")");
	}


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Timber.d("onConnectionFailed (" + connectionResult.getErrorCode() + ")");

		// already resolving?
		if (resolvingConnectionFailure) return;

		// check for if login should be handled
		if (loginClicked || autoStartLoginFlow) {
			autoStartLoginFlow = false;
			loginClicked = false;
			resolvingConnectionFailure = true;

			// Attempt to resolve the connection failure using BaseGameUtils.
			if (!BaseGameUtils.resolveConnectionFailure(
					this,
					googleApiClient,
					connectionResult,
					REQUEST_LOGIN,
					getString(R.string.error_login_other))) {

				resolvingConnectionFailure = false;
			}
		}

		showFragment(new LoginFragment(), false);
	}


	@Override
	public void onActivityResult(int request, int response, Intent data) {
		switch (request) {
			case REQUEST_LOGIN:
				if (response != RESULT_OK) {
					Timber.d("failed to resolve login error");
				} else {
					googleApiClient.connect();
				}
		}
	}


	@Override
	public void onLoginClicked() {
		loginClicked = true;
		googleApiClient.connect();
	}


	@Override
	public void onGameStarted() {
		showFragment(new GameFragment(), true);
	}


	public void onGameStopped() {
		getFragmentManager().popBackStack();
	}


	private void showFragment(Fragment fragment, boolean addToBackStack) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, fragment);
		if (addToBackStack) transaction.addToBackStack(null);
		transaction.commit();
	}

}