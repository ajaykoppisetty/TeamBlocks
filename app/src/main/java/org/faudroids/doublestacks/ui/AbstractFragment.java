package org.faudroids.doublestacks.ui;


import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.faudroids.doublestacks.R;

import roboguice.fragment.provided.RoboFragment;
import roboguice.inject.InjectView;

abstract class AbstractFragment extends RoboFragment {

	private final int layoutResource;
	protected ActionListener actionListener = null;

	@InjectView(R.id.spinner) View spinnerContainer;
	@InjectView(R.id.spinner_image) ImageView spinnerImage;

	AbstractFragment(int layoutResource) {
		this.layoutResource = layoutResource;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(layoutResource, container, false);
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			actionListener = (ActionListener) activity;
		} catch (ClassCastException e) {
			throw new RuntimeException("activity must implement " + ActionListener.class.getName());
		}
	}


	protected void showSpinner() {
		spinnerContainer.setVisibility(View.VISIBLE);

		spinnerImage.setBackgroundResource(R.drawable.spinner);
		AnimationDrawable animationDrawable = (AnimationDrawable) spinnerImage.getBackground();
		animationDrawable.start();
	}


	protected void hideSpinner() {
		AnimationDrawable animationDrawable = (AnimationDrawable) spinnerImage.getBackground();
		animationDrawable.stop();

		spinnerContainer.setVisibility(View.GONE);
	}


	protected boolean isSpinnerVisible() {
		return spinnerContainer.getVisibility() == View.VISIBLE;
	}

}
