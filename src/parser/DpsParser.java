/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.util.HashMap;
import java.util.StringTokenizer;

/**
 *
 * @author gabriele
 */
public class DpsParser extends Parser {
    
    private HashMap<String, HashMap> dps;
    private String id;
    
    public DpsParser () {
        dps = new HashMap<>();
    }
    
    public HashMap<String, HashMap> getDPS() {
        return dps;
    }

    @Override
    public void parseLine(String txt) {
        StringTokenizer tString = new StringTokenizer(txt);
        if ( txt.startsWith("@") ) {
            tString.nextToken(" ");
            id = tString.nextToken(" ");
            dps.put(id, new HashMap<String, Double>());
        } else if (!txt.equals("")) {
            String node = tString.nextToken();
            tString.nextToken();
            Double pred  = Double.parseDouble(tString.nextToken(" "));
            dps.get(id).put(node, pred);
        }
    }   

    @Override
    public HashMap<String, HashMap> getHashMap() {
        return dps;
    }
}
