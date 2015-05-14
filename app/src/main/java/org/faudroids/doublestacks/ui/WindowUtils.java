package org.faudroids.doublestacks.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.Window;

final class WindowUtils {

	private static final boolean immersiveAvailable;
	private static final boolean immersiveStickyAvailable;
	static {
		immersiveAvailable = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
		immersiveStickyAvailable = (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT);
	}


	/**
	 * Starts the fullscreen immersive mode if API level is sufficient.
	 * @return whether API level was sufficient
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public boolean startImmersiveMode(Window window) {
		if (immersiveAvailable) {
			int visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
			if (immersiveStickyAvailable) visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			window.getDecorView().setSystemUiVisibility(visibility);
			return true;
		}
		return false;
	}

}
