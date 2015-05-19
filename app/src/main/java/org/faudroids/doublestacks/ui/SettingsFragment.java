package org.faudroids.doublestacks.ui;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.faudroids.doublestacks.R;

import roboguice.inject.InjectView;
import timber.log.Timber;

public class SettingsFragment extends AbstractFragment {

	@InjectView(R.id.button_back) ImageButton backButton;
	@InjectView(R.id.text_version) TextView versionView;
	@InjectView(R.id.text_about) TextView aboutView;
	@InjectView(R.id.text_credits) TextView creditsView;
	@InjectView(R.id.text_feedback) TextView feedbackView;


	public SettingsFragment() {
		super(R.layout.fragment_settings);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});


		versionView.setText(getString(R.string.about_version, getVersion()));
		setOnClickDialogForTextView(aboutView, R.string.about_title, R.string.about_msg);
		setOnClickDialogForTextView(creditsView, R.string.credits_title, R.string.credits_msg);

		AlertDialog.Builder feedbackDialog = setOnClickDialogForTextView(feedbackView, R.string.feedback_title, R.string.feedback_msg);
		feedbackDialog.setPositiveButton(R.string.feedback_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
				}
			}
		});
		feedbackDialog.setNegativeButton(R.string.feedback_cancel, null);
	}


	private String getVersion() {
		try {
			return getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException nnfe) {
			Timber.e(nnfe, "failed to get version");
			return null;
		}
	}


	private AlertDialog.Builder setOnClickDialogForTextView(TextView textView, final int titleResourceId, final int msgResourceId) {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
				.setTitle(titleResourceId)
				.setMessage(Html.fromHtml(getString(msgResourceId)))
				.setPositiveButton(android.R.string.ok, null);

		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog dialog = dialogBuilder.show();
				((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			}
		});

		return dialogBuilder;
	}

}
