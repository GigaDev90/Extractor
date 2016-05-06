/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.util.HashMap;
import java.util.StringTokenizer;
import util.DpsData;

/**
 *
 * @author gabriele
 */
public abstract class Parser {
    
    protected DpsData dps;
    
    public Parser() {
       dps = DpsData.getDpsData();
    }
    
    public DpsData getDpsData() {
        return dps;
    }

    public abstract void parseLine(String txt);
}
