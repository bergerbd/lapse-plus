package lapsePlus.views;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import lapsePlus.utils.StringUtils;

class StatisticsManager {
	
    private Collection<SinkMatch> matches = new LinkedList<SinkMatch>();
    HashMap<String, Integer> map = new HashMap<String, Integer>();
    private HashMap<String, Boolean> categories = new HashMap<String, Boolean>();

    public void add(SinkMatch match) {
        matches.add(match);
    }

    public void gatherStatistics() {
        if (map.size() > 0) matches.clear(); // clear the map
        
        for (Iterator<SinkMatch> iter = matches.iterator(); iter.hasNext();) {
        	
            SinkMatch match = iter.next();
            
            inc("total.all");
            
            if (match.isError()) inc("unsafe.all");
            
            if (match.hasSource() && match.isError()) inc("source+unsafe.all");
            
            inc("total." + match.getCategory());
            
            if (match.isError()) inc("unsafe." + match.getCategory());
            
            if (match.isError() && match.hasSource()) inc("source+unsafe." + match.getCategory());
            
            categories.put(match.getCategory(), null);
            
            if (match.hasSource()) inc("source.all");
        }
    }

    public void printStatistics() {
        System.out.println(getStatistics());
    }

    public String getStatistics() {
        if (map.size() == 0) gatherStatistics();
        StringBuffer result = new StringBuffer();
        result.append("Statistics block:\n");
        result.append(getKey("all"));
        
        int totalAll=0;
        int sourceAll=0;
        int unsafeAll=0;
        int sourceUnsafeAll=0;
        
        if(totalAll==-1){
        	
        	totalAll=0;
        }
        
        if(sourceAll==-1){
        	sourceAll=0;
        }
        
        if(unsafeAll==-1){
        	unsafeAll=0;
        }
        
        if(sourceUnsafeAll==-1){
        	
        	sourceUnsafeAll=0;
        }
        
        
        for (Iterator<String> iter = categories.keySet().iterator(); iter.hasNext();) {
            String category = iter.next();
            result.append(getKey(category));
        }
        
        totalAll=get("total.all");
        sourceAll=get("source.all");
        unsafeAll=get("unsafe.all");
        sourceUnsafeAll=get("source+unsafe.all");
        
        if(totalAll==-1){
        	
        	totalAll=0;
        }
        
        if(sourceAll==-1){
        	sourceAll=0;
        }
        
        if(unsafeAll==-1){
        	unsafeAll=0;
        }
        
        if(sourceUnsafeAll==-1){
        	
        	sourceUnsafeAll=0;
        }
        
        
        result.append("Out of " + totalAll + " sinks, " + sourceAll
            + " occur in the source.\n");
        
        
        result.append("Out of " + unsafeAll + " unsafe sinks, "
            + sourceUnsafeAll + " occur in the source.\n");
        
        
        
        return result.toString();
    }

    private String getKey(String key) {
    	
    	int total=get("total." + key);
    	int source=get("source+unsafe." + key);
    	int unsafe=get("unsafe." + key);
    	
    	if(total==-1){
    		
    		total=0;
    	}
    	
    	if(source==-1){
    		source=0;
    	}
    	
    	if(unsafe==-1){
    		unsafe=0;
    	}
    	
        return ("\t" + StringUtils.cutto(key, 20) + "\tTotal:\t" + total + ",\tunsafe:\t"
            + unsafe + ",\tin source:\t" + source + "\n");
    }

    private void inc(String key) {
        if (map.get(key) == null) {
            map.put(key, new Integer(1));
        } else {
            Integer oldValue = (Integer) map.get(key);
            map.put(key, new Integer(oldValue.intValue() + 1));
        }
    }

    private int get(String key) {
        Integer i = ((Integer) map.get(key));
        if (i != null) {
            return i.intValue();
        } else {
            SinkView.logError("Key '" + key + "' is missing.");
            return -1;
        }
    }

    /**
     * Clears all internal data structures.
     */
    public void clearMatches() {
        matches.clear();
        map.clear();
        categories.clear();
    }
}