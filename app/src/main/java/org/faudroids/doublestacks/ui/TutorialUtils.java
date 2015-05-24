package org.faudroids.doublestacks.ui;


import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

public class TutorialUtils {

	private static final String PREFS_TUTORIAL_UTILS = TutorialUtils.class.getSimpleName();
	private static final String KEY_SHOW_TUTORIAL = "SHOW_TUTORIAL";

	private final Context context;

	@Inject
	TutorialUtils(Context context) {
		this.context = context;
	}


	public boolean shouldShowTutorial() {
		return context
				.getSharedPreferences(PREFS_TUTORIAL_UTILS, Context.MODE_PRIVATE)
				.getBoolean(PREFS_TUTORIAL_UTILS, true);
	}


	public void onShowTutorial() {
		SharedPreferences.Editor editor = context
				.getSharedPreferences(PREFS_TUTORIAL_UTILS, Context.MODE_PRIVATE)
				.edit();
		editor.putBoolean(PREFS_TUTORIAL_UTILS, false);
		editor.commit();
	}

}
