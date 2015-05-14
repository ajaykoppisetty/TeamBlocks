package org.faudroids.doublestacks.ui;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
		Timber.d("Google login successful");
		showFragment(new MenuFragment(), true);
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
	public void onLoginClicked() {
		loginClicked = true;
		googleApiClient.connect();
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


	private void showFragment(Fragment fragment, boolean addToBackStack) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, fragment);
		if (addToBackStack) transaction.addToBackStack(null);
		transaction.commit();
	}

}
