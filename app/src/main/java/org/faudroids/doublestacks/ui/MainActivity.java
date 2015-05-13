package org.faudroids.doublestacks.ui;


import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.faudroids.doublestacks.R;

import javax.inject.Inject;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private static final int REQUEST_LOGIN = 42;

	@InjectView(R.id.login_button) private SignInButton loginButton;

	private boolean resolvingConnectionFailure = false;
	private boolean autoStartLoginFlow = true;
	private boolean loginClicked = false;

	@Inject private GoogleApiClient googleApiClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				login();
			}
		});
	}


	@Override
	public void onStart() {
		super.onStart();
		googleApiClient.registerConnectionCallbacks(this);
		googleApiClient.registerConnectionFailedListener(this);
		googleApiClient.connect();
	}


	@Override
	public void onStop() {
		googleApiClient.disconnect();
		googleApiClient.unregisterConnectionCallbacks(this);
		googleApiClient.unregisterConnectionFailedListener(this);
		super.onStop();
	}


	@Override
	public void onConnected(Bundle bundle) {
		// TODO player is signed in, proceed?
		Timber.d("Google login successful");
	}


	@Override
	public void onConnectionSuspended(int i) {
		// nothing to do for now
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

		// Put code here to display the sign-in button
	}


	private void login() {
		loginClicked = true;
		googleApiClient.connect();
	}


	// TODO use somewhere
	private void logout() {
		loginClicked = false;
		Games.signOut(googleApiClient);
	}

}
