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
public class AdjParser extends Parser {

   
    private String id;

    public AdjParser() {
    }

    @Override
    public void parseLine(String txt) {
        StringTokenizer tString = new StringTokenizer(txt);
        if (txt.startsWith("graph")) {
            tString.nextToken(" ");
            tString.nextToken(" ");
            tString.nextToken(" ");
            
        } else if (txt.startsWith("\t")) {
//           
            String node1 = tString.nextToken("\t-");
            String node2 = tString.nextToken("-- ");
            
            String tmp = tString.nextToken();
            tmp = tmp.substring(8, tmp.length() - 2);
            Integer weight = Integer.parseInt(tmp);
            dps.addRealEncounter(node1, node2, weight);
        }
    }
}
