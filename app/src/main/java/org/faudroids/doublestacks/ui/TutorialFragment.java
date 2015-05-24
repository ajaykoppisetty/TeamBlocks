package org.faudroids.doublestacks.ui;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.faudroids.doublestacks.R;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public class TutorialFragment extends AbstractFragment {

	@InjectView(R.id.button_left) ImageButton leftButton;
	@InjectView(R.id.button_right) ImageButton rightButton;
	@InjectView(R.id.image) ImageView imageView;
	@InjectView(R.id.text_description) TextView textView;

	private List<TutorialStep> tutorialSteps = new ArrayList<>();
	private int currentStep = 0;

	public TutorialFragment() {
		super(R.layout.fragment_tutorial);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		tutorialSteps.add(new TutorialStep(R.string.tutorial_1, R.drawable.tutorial1, TutorialStep.NO_RESOURCE, R.drawable.button_right_selector));
		tutorialSteps.add(new TutorialStep(R.string.tutorial_2, R.drawable.tutorial2, R.drawable.button_left_selector, R.drawable.button_right_selector));
		tutorialSteps.add(new TutorialStep(R.string.tutorial_3, R.drawable.tutorial3, R.drawable.button_left_selector, R.drawable.button_right_selector));
		tutorialSteps.add(new TutorialStep(R.string.tutorial_4, R.drawable.tutorial4, R.drawable.button_left_selector, R.drawable.button_done_selector));
		showTutorialStep();

		leftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentStep <= 0) return;
				--currentStep;
				showTutorialStep();
			}
		});
		rightButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentStep >= tutorialSteps.size() - 1) {
					actionListener.onTutorialEnd();
					return;
				}
				++currentStep;
				showTutorialStep();
			}
		});
	}


	private void showTutorialStep() {
		tutorialSteps.get(currentStep).show();
	}


	private final class TutorialStep {

		static final int NO_RESOURCE = -1;

		private final int descriptionResource;
		private final int imageResource;
		private final int buttonLeftResource, buttonRightResource;

		public TutorialStep(int descriptionResource, int imageResource, int buttonLeftResource, int buttonRightResource) {
			this.descriptionResource = descriptionResource;
			this.imageResource = imageResource;
			this.buttonLeftResource = buttonLeftResource;
			this.buttonRightResource = buttonRightResource;
		}


		public void show() {
			textView.setText(getString(descriptionResource));
			imageView.setImageResource(imageResource);
			showButton(leftButton, buttonLeftResource);
			showButton(rightButton, buttonRightResource);
		}


		private void showButton(ImageButton button, int resource) {
			if (resource == NO_RESOURCE) {
				button.setVisibility(View.GONE);
			} else {
				button.setVisibility(View.VISIBLE);
				button.setImageResource(resource);
			}
		}

	}

}
