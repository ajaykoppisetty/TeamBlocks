package org.faudroids.doublestacks.ui;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.faudroids.doublestacks.R;

import javax.inject.Inject;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
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
	@Inject private WindowUtils windowUtils;

	@InjectView(R.id.spinner) private View spinnerContainer;
	@InjectView(R.id.spinner_image) private ImageView spinnerImage;
	protected SpinnerUtils spinnerUtils;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		spinnerUtils = new SpinnerUtils(spinnerContainer, spinnerImage);
	}


	@Override
	public void onStart() {
		super.onStart();
		Timber.d("connecting to google api client");
		// prevent lost connection when screen sleeps
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		googleApiClient.registerConnectionCallbacks(this);
		googleApiClient.registerConnectionFailedListener(this);
		googleApiClient.connect();
		spinnerUtils.showSpinner();
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
		spinnerUtils.hideSpinner();
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

		spinnerUtils.hideSpinner();
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
	public void onLogoutClicked() {
		//  log out and restart
		Games.signOut(googleApiClient);
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}


	@Override
	public void onGameStarted() {
		showFragment(new GameFragment(), true);
	}


	@Override
	public void onGameStopped() {
		getFragmentManager().popBackStack();
	}


	@Override
	public void onSettingsClicked() {
		showFragment(new SettingsFragment(), true);
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			boolean success = windowUtils.startImmersiveMode(getWindow());
			if (!success) return;

			WindowManager.LayoutParams attributes = getWindow().getAttributes();
			attributes.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
			getWindow().setAttributes(attributes);
		}
	}


	private void showFragment(Fragment fragment, boolean addToBackStack) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, fragment);
		if (addToBackStack) transaction.addToBackStack(null);
		transaction.commit();
	}

}
