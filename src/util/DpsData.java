/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author gabriele
 */
public class DpsData {

    private LinkedHashMap<String, MyList> data;
    private LinkedHashMap<Couple, Integer> realEncounter;
    private LinkedHashMap<Couple, Integer> estimatedEncounters;
    private LinkedHashMap<Couple, LinkedHashMap<Double, CouplePlus>> predForCouple;
    
    private static DpsData dpsData = new DpsData();

    private DpsData() {
        data = new LinkedHashMap<>();
        realEncounter = new LinkedHashMap<>();
        estimatedEncounters = new LinkedHashMap<>();
        predForCouple = new LinkedHashMap<>();
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
        
       //System.out.print("Error: no match found");
       //System.out.println("NodeA "+nodeA+" nodeB "+nodeB);
        return 0;
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
        if ( nodeA.startsWith("spy") || nodeB.startsWith("spy")) {
            return;
        }
        Couple c = new Couple(nodeA, nodeB);
        for (Couple key : realEncounter.keySet()) {
            if (key.equals(c)) {
                realEncounter.put(key, getEstimatedEnc(nodeA, nodeB) + n);
                if (n == -10) {
                    System.out.println("test");
                }
                return;
            }
        }
        
        realEncounter.put(c, n);
    }
  
    public void addEstimatedEnc(String nodeA, String nodeB, Integer n) {
        if ( nodeA.startsWith("spy") || nodeB.startsWith("spy")) {
            return;
        }
        Couple c = new Couple(nodeA, nodeB);
        for (Couple key : estimatedEncounters.keySet()) {
            if (key.equals(c)) {
                estimatedEncounters.put(key, getEstimatedEnc(nodeA, nodeB) + n);
                return;
            }
        }

        estimatedEncounters.put(c, getEstimatedEnc(nodeA, nodeB) + n);

    }
    
    public void addEncounter(String nodeA, String nodeB, Double time, Double pred) {
        if ( nodeA.startsWith("spy") || nodeB.startsWith("spy")) {
            return;
        }
        Couple c = new Couple(nodeA, nodeB);
        CouplePlus cP = new CouplePlus(nodeA, nodeB, pred);
        for (Couple key : predForCouple.keySet()) {
            if (key.equals(c)) {
                predForCouple.get(key).put(time, cP);
                predForCouple.put(key, predForCouple.get(key));
                return;
            }
        }
        LinkedHashMap<Double, CouplePlus> tmpList = new LinkedHashMap<>();
        tmpList.put(time, cP);
        predForCouple.put(c, tmpList);
    }

    public LinkedHashMap<String, MyList> getData() {
        return data;
    }
    
    public LinkedHashMap<Couple, LinkedHashMap<Double, CouplePlus>> getPredForCouple() {
        return predForCouple;
    }
    
    public LinkedHashMap<Double, HashMap<String, Double>> getDpsID(String id) {
        return data.get(id).getList();
    }
    
    public LinkedHashMap<Couple, Integer> getEstimateEncList() {
        return estimatedEncounters;
    }
    
    public LinkedHashMap<Couple, Integer> getRealEncList() {
        return realEncounter;
    }
    
    @Override
    public String toString() {
        return data.toString()+"\n"
                +realEncounter.toString();
    }
    
    public LinkedHashMap<Double, CouplePlus> sort(LinkedHashMap<Double, CouplePlus> list) {
        Map.Entry<Double, CouplePlus>[] arrayList = new Map.Entry[list.size()];
        arrayList = list.entrySet().toArray(arrayList);
        
        for ( int i = 1; i < arrayList.length; i++ ) {
            for ( int j = i; j > 0; j-- ) {
                if ( arrayList[j].getKey() < arrayList[j - 1].getKey() ) {
                    Map.Entry<Double, CouplePlus> tmp = arrayList[j];
                    arrayList[j] = arrayList[j - 1];
                    arrayList[j - 1] = tmp;
                }
            }
        }
        
        LinkedHashMap<Double, CouplePlus> sortedList = new LinkedHashMap<>();
        for ( int i = 0; i < list.size(); i++ ) {
            sortedList.put(arrayList[i].getKey(), arrayList[i].getValue());
        }
        
        return sortedList;
    }
}
