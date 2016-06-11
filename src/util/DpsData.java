/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 *
 * @author gabriele
 */
public class DpsData {

    private LinkedHashMap<Couple, Integer> realEncounter;
    private LinkedHashMap<Couple, Integer> estimatedEncounters;
    private LinkedHashMap<Couple, LinkedHashMap<Double, CouplePlus>> predForCouple;
    
    private static DpsData dpsData = new DpsData();

    private DpsData() {
        realEncounter = new LinkedHashMap<>();
        estimatedEncounters = new LinkedHashMap<>();
        predForCouple = new LinkedHashMap<>();
    }
    
    public static DpsData getDpsData() {
        return dpsData;
    }
    
    public int getRealEnc(Couple sample) {
        if( realEncounter.containsKey(sample) ) {
            return realEncounter.get(sample);
        } else {
            return 0;
        }
    }
    
    public void addRealEncounter(String nodeA, String nodeB, Integer n) {
        if ( nodeA.startsWith("spy") || nodeB.startsWith("spy")) {
            return;
        }
        Couple c = new Couple(nodeA, nodeB);

        if ( realEncounter.containsKey(c) ) {
            realEncounter.put(c, getEstimatedEnc(nodeA, nodeB) + n);
            return;
        }

        realEncounter.put(c, n);
    }

    public LinkedHashMap<Couple, Integer> getRealEncList() {
        return realEncounter;
    }

    public int getEstimatedEnc(Couple entry) {
        if ( estimatedEncounters.containsKey(entry) ) {
            return estimatedEncounters.get(entry);
        } else {
            return 0;
        }
    }
    
    public int getEstimatedEnc(String nodeA, String nodeB) {
        Couple sample = new Couple(nodeA, nodeB);
        return getEstimatedEnc(sample);
    }
  
    public void addEstimatedEnc(Couple c, Integer n) {
        if ( c.getNodeA().startsWith("spy") || c.getNodeB().startsWith("spy")) {
            return;
        }
        
        if ( estimatedEncounters.containsKey(c) ) {
            estimatedEncounters.put(c, getEstimatedEnc(c) + n);
            return;
        }

        estimatedEncounters.put(c, getEstimatedEnc(c) + n);
    }
    
    public LinkedHashMap<Couple, Integer> getEstimateEncList() {
        return estimatedEncounters;
    }
    
    public void addEncounter(String nodeA, String nodeB, Double time, Double pred) {
        if ( nodeA.startsWith("spy") || nodeB.startsWith("spy")) {
            return;
        }
        Couple c = new Couple(nodeA, nodeB);
        CouplePlus cP = new CouplePlus(nodeA, nodeB, pred);
        if ( predForCouple.containsKey(c) ) {
            predForCouple.get(c).put(time, cP);
            return;
        }

        LinkedHashMap<Double, CouplePlus> tmpList = new LinkedHashMap<>();
        tmpList.put(time, cP);
        predForCouple.put(c, tmpList);
    }

   
    
    public LinkedHashMap<Couple, LinkedHashMap<Double, CouplePlus>> getPredForCouple() {
        return predForCouple;
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
