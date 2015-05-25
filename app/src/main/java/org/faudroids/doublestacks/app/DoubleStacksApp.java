package org.faudroids.doublestacks.app;


import android.app.Application;

import com.crashlytics.android.Crashlytics;

import org.faudroids.doublestacks.BuildConfig;
import org.faudroids.doublestacks.google.GoogleModule;

import io.fabric.sdk.android.Fabric;
import roboguice.RoboGuice;
import timber.log.Timber;

public final class DoubleStacksApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		RoboGuice.getOrCreateBaseApplicationInjector(
				this,
				RoboGuice.DEFAULT_STAGE,
				RoboGuice.newDefaultRoboModule(this),
				new GoogleModule());

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		} else {
			Fabric.with(this, new Crashlytics());
			Timber.plant(new CrashReportingTree());
		}
	}


	private static final class CrashReportingTree extends Timber.Tree {

		@Override
		public void e(String msg, Object... args) {
			Crashlytics.log(msg);
		}

		@Override
		public void e(Throwable e, String msg, Object... args) {
			Crashlytics.log(msg);
			Crashlytics.logException(e);
		}

		@Override
		public void w(String msg, Object... args) {
			Crashlytics.log(msg);
		}

		@Override
		public void w(Throwable e, String msg, Object... args) {
			Crashlytics.log(msg);
			Crashlytics.logException(e);
		}


		@Override
		protected void log(int priority, String tag, String message, Throwable t) {
			// nothing to do here
		}

	}

}