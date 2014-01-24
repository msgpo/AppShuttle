package lab.davidahn.appshuttle.predict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.bhv.FavoriteBhv;
import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.bhv.UserBhvManager;
import lab.davidahn.appshuttle.bhv.ViewableUserBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class PresentBhvManager {
	
	public static Map<UserBhv, PresentBhv> getPresentBhvs() {
		return AppShuttleApplication.presentBhvMap;
	}
	
	public static Map<UserBhv, PresentBhv> extractPresentBhvs(
			Map<UserBhv, PredictedBhvInfo> currPredictionInfos) {
		if (currPredictionInfos == null || currPredictionInfos.isEmpty())
			return Collections.emptyMap();

		Map<UserBhv, PresentBhv> currPresentBhvs = new HashMap<UserBhv, PresentBhv>();
		for (UserBhv uBhv : currPredictionInfos.keySet()) {
			PredictedBhvInfo currPredictionInfo = currPredictionInfos.get(uBhv);
			PresentBhv recentPresentBhv = getPresentBhvs().get(uBhv);
			PresentBhv currPresentBhv = new PresentBhv(uBhv);
			if (recentPresentBhv == null) {
				for (MatcherType matcherType : currPredictionInfo
						.getMatcherResultMap().keySet())
					currPresentBhv.setStartPredictionInfoByMatcherType(
							matcherType, currPredictionInfo);
			} else {
				for (MatcherType matcherType : currPredictionInfo
						.getMatcherResultMap().keySet()) {
					PredictedBhvInfo startPredictionInfo = recentPresentBhv
							.getStartPredictionInfoByMatcherType(matcherType);
					if (startPredictionInfo == null)
						currPresentBhv.setStartPredictionInfoByMatcherType(
								matcherType, currPredictionInfo);
					else {
						// if(uBhv.getBhvName().equals("com.android.chrome")){
						// Log.d("test", matcherType + ", " +
						// recentPresentBhv.getStartPredictionInfoByMatcherType(matcherType).getTimeDate().toString());
						// }

						if (matcherType.isOverwritableForNewPrediction)
							currPresentBhv.setStartPredictionInfoByMatcherType(
									matcherType, currPredictionInfo);
						else
							currPresentBhv.setStartPredictionInfoByMatcherType(
									matcherType, startPredictionInfo);
					}
				}
			}
			currPresentBhvs.put(uBhv, currPresentBhv);
		}
		AppShuttleApplication.presentBhvMap = currPresentBhvs;
		return currPresentBhvs;
	}

	public static List<ViewableUserBhv> getPresentBhvListFilteredSorted(int topN) {
		List<ViewableUserBhv> res = new ArrayList<ViewableUserBhv>();
	
		List<PresentBhv> predictedPresent = new ArrayList<PresentBhv>();
		for(PresentBhv predictedBhv : getPresentBhvs().values())
			if(isEligible(predictedBhv))
				predictedPresent.add(predictedBhv);
	
		Collections.sort(predictedPresent, Collections.reverseOrder());
		
		for(PresentBhv uBhv : predictedPresent)
			res.add(UserBhvManager.getInstance().getViewableUserBhv(uBhv));
			
		return res.subList(0, Math.min(res.size(), topN));
	}

	private static boolean isEligible(UserBhv uBhv){
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		if(userBhvManager.getNormalBhvSet().contains(uBhv))
			return true;
		else if(userBhvManager.getFavoriteBhvSet().contains(uBhv)
				&& !((FavoriteBhv)userBhvManager.getViewableUserBhv(uBhv)).isNotifiable())
			return true;
		else
			return false;
	}
}