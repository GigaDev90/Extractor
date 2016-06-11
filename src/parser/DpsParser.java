/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import util.DpsData;

/**
 *
 * @author gabriele
 */
public class DpsParser extends Parser {
    
    private String id;
    private double time;
    
    public DpsParser () {
    }

    @Override
    public void parseLine(String txt) {
        StringTokenizer tString = new StringTokenizer(txt);
        if ( txt.startsWith("@") ) {
            time = Double.parseDouble(tString.nextToken(" ").substring(1));
            id = tString.nextToken(" ");
            //dps.addUniqueId(id);
        } else if (!txt.equals("")) {
            String node = tString.nextToken();
            tString.nextToken();
            Double pred  = Double.parseDouble(tString.nextToken(" "));
            if ( node.startsWith("spy")) {
                return;
            }
            dps.addEncounter(id, node, time, pred);
        }
    }   
}
