package org.faudroids.doublestacks.google;


import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import javax.inject.Inject;

import roboguice.inject.ContextSingleton;

@ContextSingleton
public class AchievementManager {

	private static final String
			ACHIEVEMENT_100_POINTS = "CgkI_KePsJcKEAIQAg",
			ACHIEVEMENT_200_POINTS = "CgkI_KePsJcKEAIQAw",
			ACHIEVEMENT_300_POINTS = "CgkI_KePsJcKEAIQBA",
			ACHIEVEMENT_500_POINTS = "CgkI_KePsJcKEAIQBQ",
			ACHIEVEMENT_1000_POINTS = "CgkI_KePsJcKEAIQBg";

	private static final String
			LEADERBOARD_ID = "CgkI_KePsJcKEAIQCA";


	private final GoogleApiClient googleApiClient;

	@Inject
	AchievementManager(GoogleApiClient googleApiClient) {
		this.googleApiClient = googleApiClient;
	}


	public void onScoreChanged(int score) {
		if (score >= 1000) unlockAchievement(ACHIEVEMENT_1000_POINTS);
		else if (score >= 500) unlockAchievement(ACHIEVEMENT_500_POINTS);
		else if (score >= 300) unlockAchievement(ACHIEVEMENT_300_POINTS);
		else if (score >= 200) unlockAchievement(ACHIEVEMENT_200_POINTS);
		else if (score >= 100) unlockAchievement(ACHIEVEMENT_100_POINTS);
	}


	public void onGameFinished(int finalScore) {
		Games.Leaderboards.submitScore(googleApiClient, LEADERBOARD_ID, finalScore);
	}


	public Intent getHighScoreIntent() {
		return Games.Leaderboards.getLeaderboardIntent(googleApiClient, LEADERBOARD_ID);
	}


	private void unlockAchievement(String achievementId) {
		Games.Achievements.unlock(googleApiClient, achievementId);
	}

}
