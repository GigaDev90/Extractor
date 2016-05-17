/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author gabriele
 */
public class MyList {

    private String id;
    private LinkedHashMap<Double, HashMap<String, Double>> list;
    private LinkedHashMap<String, Double> maxVar;

    public MyList(String id) {
        this.id = id;
        list = new LinkedHashMap<>();
        maxVar = new LinkedHashMap<>();
        //estimatedEncounters = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public LinkedHashMap<Double, HashMap<String, Double>> getList() {
        return list;
    }

    public LinkedHashMap<String, Double> getMaxVar() {
        return maxVar;
    }

    @Override
    public String toString() {
        return list.toString() + "\n";
    }
}
