package org.faudroids.doublestacks.google;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Inject;

import roboguice.inject.ContextSingleton;

public class GoogleModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MessageSender.class).to(ConnectionManager.class);
	}

	@Provides
	@Inject
	@ContextSingleton
	public GoogleApiClient provideGoogleApiClient(Context context) {
		return new GoogleApiClient.Builder(context)
				.addApi(Games.API)
				.addScope(Games.SCOPE_GAMES)
				.build();
	}

}
