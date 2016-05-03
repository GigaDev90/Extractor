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

    private HashMap<String, HashMap> dps;
    private String id;

    public AdjParser() {
        dps = new HashMap<>();
    }

    public HashMap<String, HashMap> getDPS() {
        return dps;
    }

    @Override
    public void parseLine(String txt) {
        StringTokenizer tString = new StringTokenizer(txt);
        if (txt.startsWith("graph")) {
            //tString.nextToken(" ");
            //tString.nextToken(" ");
            System.out.println(tString.nextToken(" "));
            System.out.println(tString.nextToken(" "));
            System.out.println(tString.nextToken(" "));
            
        } else if (!txt.equals("\t")) {
            //tString.nextToken("-"));
        }
    }

    @Override
    public HashMap<String, HashMap> getHashMap() {
        return dps;
    }

    @Override
    public String toString() {
        String txt = "";
        
        return txt;
    }
}
