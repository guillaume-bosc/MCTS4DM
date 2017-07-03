package de.fraunhofer.iais.ocm.core.model;

import java.util.*;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;


public class SessionPatternState {
	
	public HashMap<Long, Pattern> patterns;
	
	private List<Pattern> resultPatterns;
	
	public List<Pattern> candidateResultPatterns;
	
	public List<Pattern> deletedPatterns;
	
	public SessionPatternState() {
		this.resultPatterns = new ArrayList<Pattern>();
		this.candidateResultPatterns = new ArrayList<Pattern>();
		this.patterns = new HashMap<Long, Pattern>();
		this.deletedPatterns = new ArrayList<Pattern>();
	}

	public Pattern getPatternById(long id) {
		return patterns.get(id);
	}
	
	public long getPatternIdFromString(String patternId) {
		return Long.parseLong(patternId);
	}

    public List<Pattern> generateCandidateList(int uiRankListSize) {
    	return candidateResultPatterns.subList(0, Math.min(uiRankListSize, candidateResultPatterns.size()));
	}

	public void addPatternsToIndex(List<Pattern> patternList) {
		for (Pattern pattern : patternList) {
			patterns.put(pattern.getId(), pattern);
		}
	}

    public synchronized boolean isInResults(Pattern p) {
        return resultPatterns.contains(p);
    }
    
    public synchronized boolean isInDeleted(Pattern p) {
    	return deletedPatterns.contains(p);
    }

    public synchronized void addResultPattern(Pattern pattern) {
        resultPatterns.add(pattern);
    }

    public void removeCandidate(Pattern pattern) {
        candidateResultPatterns.remove(pattern);
        deletedPatterns.add(pattern);
    }

    public synchronized void removeResultPattern(Pattern pattern) {
        resultPatterns.remove(pattern);
        deletedPatterns.add(pattern);
    }

    public synchronized Collection<Pattern> getResultPatterns() {
        return new ArrayList<Pattern>(resultPatterns);
    }
    
}