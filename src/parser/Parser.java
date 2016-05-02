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
public abstract class Parser {
    
    public Parser() {

    }
    
    public abstract void parseLine(String txt);
    public abstract HashMap getHashMap();
}
