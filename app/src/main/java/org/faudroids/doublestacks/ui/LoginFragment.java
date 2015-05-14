package org.faudroids.doublestacks.ui;


import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.SignInButton;

import org.faudroids.doublestacks.R;

import roboguice.inject.InjectView;

public class LoginFragment extends AbstractFragment {

	@InjectView(R.id.login_button) private SignInButton loginButton;

	public LoginFragment() {
		super(R.layout.fragment_login);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actionListener.onLoginClicked();
			}
		});
	}

}
