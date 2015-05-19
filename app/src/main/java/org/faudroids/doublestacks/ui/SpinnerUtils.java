package org.faudroids.doublestacks.ui;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import org.faudroids.doublestacks.R;

class SpinnerUtils {

	private final View container;
	private final ImageView image;

	public SpinnerUtils(View container, ImageView image) {
		this.container = container;
		this.image = image;
	}


	public void showSpinner() {
		container.setVisibility(View.VISIBLE);

		image.setBackgroundResource(R.drawable.spinner);
		AnimationDrawable animationDrawable = (AnimationDrawable) image.getBackground();
		animationDrawable.start();
	}


	protected void hideSpinner() {
		AnimationDrawable animationDrawable = (AnimationDrawable) image.getBackground();
		animationDrawable.stop();

		container.setVisibility(View.GONE);
	}


	protected boolean isSpinnerVisible() {
		return container.getVisibility() == View.VISIBLE;
	}

}
