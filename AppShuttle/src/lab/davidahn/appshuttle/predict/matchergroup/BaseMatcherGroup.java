package lab.davidahn.appshuttle.predict.matchergroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.matcher.Matcher;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public abstract class BaseMatcherGroup implements MatcherGroup {
	protected MatcherGroupType matcherGroupType;
	protected int priority;
	protected EnumMap<MatcherType, Matcher> matchers;
	
	public BaseMatcherGroup(MatcherGroupType _matcherType, int _priority) {
		matcherGroupType = _matcherType;
		priority = _priority;
		matchers = new EnumMap<MatcherType, Matcher>(MatcherType.class);
	}

	@Override
	public MatcherGroupType getMatcherGroupType(){
		return matcherGroupType;
	}

	@Override
	public int getPriority() {
		return priority;
	}
	
	@Override
	public MatcherGroupResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt){
		
		if(matchers.isEmpty())
			return null;

		List<MatcherResult> matcherResults = new ArrayList<MatcherResult>();
		for(Matcher matcher : matchers.values()) {
			MatcherResult matcherResult = matcher.matchAndGetResult(uBhv, currUCxt);
			if(matcherResult != null)
				matcherResults.add(matcherResult);
		}
		
		if(matcherResults.isEmpty())
			return null;
		
		MatcherGroupResult matcherGroupResult = new MatcherGroupResult(currUCxt.getTimeDate(), currUCxt.getTimeZone(), currUCxt.getUserEnvs());
		matcherGroupResult.setMatcherGroupType(matcherGroupType);
		matcherGroupResult.setTargetUserBhv(uBhv);
		for(MatcherResult matcherResult : matcherResults)
			matcherGroupResult.addMatcherResult(matcherResult);
		matcherGroupResult.setScore(computeScore(matcherResults));
		matcherGroupResult.setViewMsg(extractViewMsg(matcherResults));
		
		return matcherGroupResult;
	}

	public void registerMatcher(Matcher matcher) {
		matchers.put(matcher.getMatcherType(), matcher);
	}

	protected String extractViewMsg(List<MatcherResult> matcherResults) {
		
		assert(!matcherResults.isEmpty());
		
		Collections.sort(matcherResults);
		return matcherResults.get(0).getMatcherType().viewMsg;
	}

	protected double computeScore(List<MatcherResult> matcherResults) {
		
		assert(!matcherResults.isEmpty());
		
		Collections.sort(matcherResults);
		return matcherResults.get(0).getScore();
	}
}