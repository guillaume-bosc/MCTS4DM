package de.fraunhofer.iais.ocm.core.model.pattern;

import java.util.Collection;


/**
 * User: paveltokmakov
 * Date: 2/12/13
 *
 * Id generator for patterns
 */
public class PatternIdGenerator {

	public static PatternIdGenerator INSTANCE=new PatternIdGenerator();
	
    private long id = 0l;
    
    private PatternIdGenerator() {
    	;
    }

    public synchronized long getNextId() {
        return id++;
    }

//	public void giveIDs(Collection<Pattern> patterns) {
//	    for(Pattern pattern: patterns) {
//	        pattern.setId(getNextId());
//	    }
//	}
}
