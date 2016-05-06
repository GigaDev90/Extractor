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
public class DpsData {

    private LinkedHashMap<String, MyList> data;
    private LinkedHashMap<Couple, Integer> realEncounter;
    private  LinkedHashMap<Couple, Integer> estimatedEncounters;
    private static DpsData dpsData = new DpsData();

    private DpsData() {
        data = new LinkedHashMap<>();
        realEncounter = new LinkedHashMap<>();
        estimatedEncounters = new LinkedHashMap<>();
    }
    
    public static DpsData getDpsData() {
        return dpsData;
    }

    public void addUniqueId(String id) {
        if (data.get(id) == null) {
            data.put(id, new MyList(id));
        }
    }
    
    public int getRealEnc(String nodeA, String nodeB) {
        Couple sample = new Couple(nodeA, nodeB);
        for (Couple c: realEncounter.keySet()) {
            if (c.equals(sample)) {
                return realEncounter.get(c);
            }
        }
        
        System.out.print("Error: no match found");
        System.out.println("NodeA "+nodeA+" nodeB "+nodeB);
        return -1;
    }
    
    public int getEstimatedEnc(Couple entry) {
        for (Couple c: estimatedEncounters.keySet()) {
            if (c.equals(entry)) {
                return estimatedEncounters.get(c);
            }
        }
        
        //System.out.print("Error: no match found");
        //System.out.println("NodeA "+entry.getNodeA()+" nodeB "+entry.getNodeB());
        return 0;
    }
    
    public int getEstimatedEnc(String nodeA, String nodeB) {
        Couple sample = new Couple(nodeA, nodeB);
        return getEstimatedEnc(sample);
    }

    public void addNewDpsTableToId(String id, Double time) {
        data.get(id).getList().put(time, new HashMap<String, Double>());
    }

    public void addEntryToId(String id, Double time, String idEntry, Double prob) {
        if (data.get(id).getList().get(time) == null) {
            addNewDpsTableToId(id, time);
        }

        data.get(id).getList().get(time).put(idEntry, prob);
    }
    
    public void addRealEncounter(String nodeA, String nodeB, Integer n) {
        Couple c = new Couple(nodeA, nodeB);
        realEncounter.put(c, n);
    }
    
     public void addEstimatedEnc(String nodeA, String nodeB, Integer n) {
         Couple c = new Couple(nodeA, nodeB);
         for ( Couple key: estimatedEncounters.keySet() ) {
             if ( key.equals(c) ) {
                 estimatedEncounters.put(key, getEstimatedEnc(nodeA, nodeB) + n);
                 return;
             }
         }
        
         estimatedEncounters.put(c, getEstimatedEnc(nodeA, nodeB) + n);
        
    }
    
    public LinkedHashMap<String, MyList> getData() {
        return data;
    }
    
    public LinkedHashMap<Double, HashMap<String, Double>> getDpsID(String id) {
        return data.get(id).getList();
    }
    
    public LinkedHashMap<Couple, Integer> getEstimateEncList() {
        return estimatedEncounters;
    }

    class MyList {

        private String id;
        private LinkedHashMap<Double, HashMap<String, Double>> list;

        public MyList(String id) {
            this.id = id;
            list = new LinkedHashMap<>();
            //estimatedEncounters = new HashMap<>();
        }

        public String getId() {
            return id;
        }

        public LinkedHashMap<Double, HashMap<String, Double>> getList() {
            return list;
        }
        
        @Override
        public String toString() {
            return list.toString()+"\n";
        }
    }
    
    @Override
    public String toString() {
        return data.toString()+"\n"
                +realEncounter.toString();
    }
}
