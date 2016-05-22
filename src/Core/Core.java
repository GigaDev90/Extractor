/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;
import util.Couple;
import util.CouplePlus;
import util.DpsData;

/**
 *
 * @author gabriele
 */
public class Core {

    private DpsData data;
    private final double standardPred = 0.5;
    private final double firstDPS = 2;
    public static final double P_INIT = 0.75;
    public static final double DEFAULT_BETA = 0.25;
    public static final int secondsInTimeUnit = 30;
    public static final double startTime = 8000;
    /**
     * delivery predictability aging constant
     */
    public static final double GAMMA = 0.98;

    public Core(DpsData data) {
        this.data = data;
    }

    private double updateDeliveryPredFor(double oldValue) {
        return oldValue + (1 - oldValue) * P_INIT;
    }

    private double logOfBase(double base, double num) {
        return Math.log(num) / Math.log(base);
    }

    private double ageDeliveryPreds(double time, double pred) {
        if (time == 0) {
            return pred;
        }

        double mult = Math.pow(GAMMA, time);
        return pred * mult;
    }

    private int onFirstEncounter(double pred, double dTime) {
        int n = 0;
        double pAOld = pred / (Math.pow(GAMMA, dTime));

        if (pAOld >= 0.90 && pred > 0.09) {
           // System.out.println("pAOld = " + pAOld + " pred = " + pred + " dTime = " + dTime / secondsInTimeUnit);
            if ( pred > 0.90 ) {
                return 2;
            }
            return 1;
        }
        
        return 0;
    }

    private double verifyOneEnc(double newPred, double oldPred, double time) {
        double nm = newPred - (Math.pow(GAMMA, time) * (oldPred - (oldPred * P_INIT)));
        double dnm = Math.pow(GAMMA, time) * P_INIT;
        double w = -logOfBase(GAMMA, nm / dnm);
        //System.out.println("w = "+w+" time = "+time);
        double oldPreAndDecay = ageDeliveryPreds(w, oldPred);
        double oldPredAndEnc = updateDeliveryPredFor(oldPreAndDecay);
        double testNew = ageDeliveryPreds(time - w, oldPredAndEnc);

        return w;
    }
    
    public void processData() {
        for (String id : data.getData().keySet()) {

            //second loop for every dps
            for (Double time : data.getDpsID(id).keySet()) {

                //loop for each dps's entry
                for (String entry : data.getDpsID(id).get(time).keySet()) {
                    data.addEncounter(id, entry, time, (data.getDpsID(id).get(time).get(entry)));
                }
            }
        }
        System.out.println("size EncForCouple = "+data.getPredForCouple().size()+" realEnc = "+data.getRealEncList().size());
        calculateEstimateEncounters2();
    }
    public Map.Entry<Double, CouplePlus> getPreviousEntry(LinkedHashMap<Double, CouplePlus> list, Map.Entry<Double, CouplePlus> e) {
        Map.Entry<Double, CouplePlus>[] arrayList = new Map.Entry[list.size()];
        arrayList = list.entrySet().toArray(arrayList);
        for ( int i = 0; i < arrayList.length; i++) {
            if ( e == arrayList[i]) {
                for ( int j = i - 1; j >= 0; j--) {
                    if ( e.getValue().getNodeA().equals(arrayList[j].getValue().getNodeA())) {
                        return arrayList[j];
                    }
                }
            }
        }
        
        return null;
    }

    public void calculateEstimateEncounters2() {
        for (Couple key : data.getPredForCouple().keySet()) {
            
            //LinkedHashMap<Double, CouplePlus> sortedList = data.sort(data.getPredForCouple().get(key));
            boolean first = true;
            boolean first2 = true;
            String owner = "";
            int tmpEnc = 0;
            
            for (Map.Entry<Double, CouplePlus> e : data.getPredForCouple().get(key).entrySet()) {
                //System.out.println("id = " + e.getValue().getNodeA() + " entry = " + e.getValue().getNodeB() + " time = " + e.getKey()/ secondsInTimeUnit + " pred = " + e.getValue().getPred());
                if ( first ) {
                    int n = onFirstEncounter(e.getValue().getPred(), (e.getKey() - startTime)/ secondsInTimeUnit);
                    data.addEstimatedEnc(e.getValue().getNodeA(), e.getValue().getNodeB(), n);
                    //System.out.println("increment start = "+n);
                    first = false;
                    owner = data.getPredForCouple().get(key).get(e.getKey()).getNodeA();
                } else if ( data.getPredForCouple().get(key).get(e.getKey()).getNodeA().equals(owner) ){
                    Map.Entry<Double, CouplePlus> previousEntry = getPreviousEntry(data.getPredForCouple().get(key), e);
                    

                    if (previousEntry != null) {
                        if (Math.abs(ageDeliveryPreds((e.getKey() - previousEntry.getKey()) / secondsInTimeUnit, previousEntry.getValue().getPred()) - e.getValue().getPred()) > 0.009) {
                           // System.out.println("prev id = " + previousEntry.getValue().getNodeA() + " entry = " + previousEntry.getValue().getNodeB() + " time = " + previousEntry.getKey() / secondsInTimeUnit + " pred = " + previousEntry.getValue().getPred() + " aged = "+ageDeliveryPreds((e.getKey() - previousEntry.getKey()) / secondsInTimeUnit, previousEntry.getValue().getPred()));
                            data.addEstimatedEnc(e.getValue().getNodeA(), e.getValue().getNodeB(), 1);
                           // System.out.println("increment 2");
                            double test = verifyOneEnc(e.getValue().getPred(), previousEntry.getValue().getPred(), (e.getKey() - previousEntry.getKey()) / secondsInTimeUnit);
                            //System.out.println("test = " + test);

                            if (((e.getKey() - previousEntry.getKey()) / secondsInTimeUnit) > 15) {

                                if (Math.abs(((e.getKey() - previousEntry.getKey()) / secondsInTimeUnit) - test) < 0.5) {
                                    data.addEstimatedEnc(e.getValue().getNodeA(), e.getValue().getNodeB(), 1);
                                    System.out.println("increment 2");
                                    continue;

                                }

                                if (previousEntry.getValue().getPred() < 0.1 && e.getValue().getPred() >= 0.74) {
                                    System.out.println("increment 1");
                                    data.addEstimatedEnc(e.getValue().getNodeA(), e.getValue().getNodeB(), 1);
                                    continue;
                                }

                            }
                        }
                    }
                } else {
                    Map.Entry<Double, CouplePlus> previousEntry = getPreviousEntry(data.getPredForCouple().get(key), e);

                    if (first2) {
                        int n = onFirstEncounter(e.getValue().getPred(), (e.getKey() - startTime) / secondsInTimeUnit);
                        tmpEnc += n;
                      //  System.out.println("Second increment start");
                        first2 = false;
                    } else if (previousEntry != null) {
                        if (Math.abs(ageDeliveryPreds((e.getKey() - previousEntry.getKey()) / secondsInTimeUnit, previousEntry.getValue().getPred()) - e.getValue().getPred()) > 0.009) {
                            //nSystem.out.println("prev id = " + previousEntry.getValue().getNodeA() + " entry = " + previousEntry.getValue().getNodeB() + " time = " + previousEntry.getKey() / secondsInTimeUnit + " pred = " + previousEntry.getValue().getPred() + " aged = " + ageDeliveryPreds((e.getKey() - previousEntry.getKey()) / secondsInTimeUnit, previousEntry.getValue().getPred()));
                            tmpEnc++;
                           // System.out.println("Second increment 1");
                            double test = verifyOneEnc(e.getValue().getPred(), previousEntry.getValue().getPred(), (e.getKey() - previousEntry.getKey()) / secondsInTimeUnit);
                            //nSystem.out.println("test = " + test);
                            if (((e.getKey() - previousEntry.getKey()) / secondsInTimeUnit) > 15) {

                                if (Math.abs(((e.getKey() - previousEntry.getKey()) / secondsInTimeUnit) - test) < 0.5) {
                                    tmpEnc++;
                                    continue;
                                    // System.out.println("Second increment 5");
                                }

                                if (previousEntry.getValue().getPred() < 0.1 && e.getValue().getPred() >= 0.74) {
                                    // System.out.println("Second increment 4");
                                    tmpEnc++;
                                    continue;
                                }
                            }

                        }
                    }
                }
            }
            if (data.getEstimatedEnc(key.getNodeA(), key.getNodeB()) < tmpEnc) {
                data.addEstimatedEnc(key.getNodeA(), key.getNodeB(), tmpEnc - data.getEstimatedEnc(key.getNodeA(), key.getNodeB()));
            }
            //nSystem.out.println("first = "+ data.getEstimatedEnc(key.getNodeA(), key.getNodeB())+" tmpEnc = "+tmpEnc);
        }
        result();
    }

    public void calculateEstimateEncounters() {
        //first loop for all unique node
        for (String id : data.getData().keySet()) {
            double oldTime = 0;
            //second loop for every dps
            for (Double time : data.getDpsID(id).keySet()) {

                //loop for each dps's entry
                for (String entry : data.getDpsID(id).get(time).keySet()) {

                    if (oldTime == 0 || data.getDpsID(id).get(oldTime).get(entry) == null) {
                        
                        int n = onFirstEncounter(data.getDpsID(id).get(time).get(entry), (time - startTime)/ secondsInTimeUnit);
                        //System.out.println("id = "+id+" entry = "+entry+"\n");
                        

                        data.addEstimatedEnc(id, entry, n);
                      
                        
                    } else {
                        double seconds = (time - oldTime) / secondsInTimeUnit;
                        double oldPred = data.getDpsID(id).get(oldTime).get(entry);
                        double newPred = data.getDpsID(id).get(time).get(entry);

                        double testPred = updateDeliveryPredFor(oldPred);
                        double testPred2 = ageDeliveryPreds(seconds, oldPred);

                        double test = verifyOneEnc(newPred, oldPred, seconds);
                        if ( newPred > oldPred && test > 0) {
                            //data.addEstimatedEnc(id, entry, 1);
                            //data.addEstimatedEnc(id, entry, (int)((newPred - oldPred)* Math.log(seconds)));
                        }
                        
                        
                        
                        
                     
                       
                        //System.out.println("Estimate enc of " + id + " to encounter " + entry + " = " + data.getEstimatedEnc(id, entry) + " real = " + data.getRealEnc(id, entry) + " with this pred " + data.getDpsID(id).get(time).get(entry)+" old pred = "+oldPred+" test = "+test+ " Dtime = "+(time - oldTime));
                        // double value = (((newPred) * (Math.log10(time - oldTime)/2 ))) + ((secondComp))*(Math.log10(time - oldTime)/2);
                        //System.out.println("frist part "+ (((newPred) * ((time - oldTime)/(2500) + 1))));
                        // System.out.println("second "+ (((newPred - oldPred))*3));
                        //data.addEstimatedEnc(id, entry, (int)value);
                        //System.out.print("old = " + oldPred + " new = " + newPred + " ");
                        //System.out.println("ifEnc = " + testPred + " ifDecay = " + testPred2+" diffTime = "+(time - oldTime)+" value = ");
//                            double testTime = timeOfEnc(newPred, oldPred);
//                            if ( testTime < 0 ) {
//                                testTime = timeOfEnc(newPred, testPred);
//                            }
//                            System.out.println("time of enc ="+testTime+" diffTime = "+(time-oldTime));
                    }
                }

                oldTime = time;

                // System.out.println("id = " + id + " time = " + time + " " + data.getDpsID(id).get(time));
            }
        }
       result();
    }

    public void result() {
        // for (String id : data.getData().keySet()) {
        String txt1 = "";
        String txt2 = "";
        String txt3 = "";
        int test = 0;
        int testA = 0;
        int testB = 0;
        int testC = 0;
        int test1 = 0;
        int test3 = 0;
        double rapport = 0;
        double rapport2 = 0;
        
        for (Couple entry : data.getEstimateEncList().keySet()) {
            //txt1 += data.getEstimatedEnc(entry) + "\n";
            //txt2 += data.getRealEnc(entry.getNodeA(), entry.getNodeB()) + "\n";
            double real = data.getRealEnc(entry.getNodeA(), entry.getNodeB());
            double est = data.getEstimatedEnc(entry);
           // if ( real < 9 ) continue;
            
            for (Couple key : data.getPredForCouple().keySet()) {
                if (key.equals(entry)) {
                    boolean first = true;
                    boolean second = true;
                    String owner = "";

                    int a = 0, b = 0;
                    for ( Double time :  data.getPredForCouple().get(key).keySet() ) {
                       if ( first ) {
                           first = false;
                           owner = data.getPredForCouple().get(key).get(time).getNodeA();
                           a++;
                       } else if ( owner.equals(data.getPredForCouple().get(key).get(time).getNodeA()) ) {
                           a++;
                       } else {
                           b++;
                       }
                            
                       
                    }
                    if ( a > b ) {
                        txt1 += a + "\n";
                    } else {
                        txt1 += b + "\n";
                    }
                    
                    //txt2 += wastTime > wastTime2 ? wastTime + "\n" : wastTime2 + "\n";

                    break;
                }
            }
            
            if (est == 0 && real == 0) {
                rapport = 1;
            } else if (est == 0 ) {
                est = 1;
                rapport = real/est;
            } else {
                rapport = real/est;
            }
            
           
            //System.out.println("real = "+(data.getRealEnc(entry.getNodeA(), entry.getNodeB()))+" est = "+(data.getEstimatedEnc(entry)));
           // System.out.println("id = "+entry.getNodeA()+" entry = "+entry.getNodeB()+"\n");
            txt3 += rapport+"\n";
            if (Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry.getNodeA(), entry.getNodeB())) < 5) {
                test++;
            }
            if (Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry.getNodeA(), entry.getNodeB())) < 3) {
                testA++;
            }
            if (Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry.getNodeA(), entry.getNodeB())) < 2) {
                testB++;
            }
            test1++;
     
            //if ( Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry.getNodeA(), entry.getNodeB())) < 20 )
            test3 += Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry.getNodeA(), entry.getNodeB()));
            testC += (data.getEstimatedEnc(entry) - data.getRealEnc(entry.getNodeA(), entry.getNodeB()));
            //testC += (data.getRealEnc(entry.getNodeA(), entry.getNodeB()) - data.getEstimatedEnc(entry));
            //System.out.println("Estimate enc of " + entry.getNodeA() + " to encounter " + entry.getNodeB() + " = " + data.getEstimatedEnc(entry) + " real = " + data.getRealEnc(entry.getNodeA(), entry.getNodeB()));
            //System.out.println("history of "+id+" "+data.getDpsID(id).toString());
        }

        System.out.println("soglia5 = " + test + " soglia3 = " + testA + "  soglia2 = " + testB + " su = " + test1);
        System.out.println("error = " + test3 + " error2 " + testC);

        try {
            FileWriter writer = new FileWriter("/home/gabriele/Documenti/risultati.txt");
            BufferedWriter bWriter = new BufferedWriter(writer);
            bWriter.write(txt3);
            bWriter.write("second set of data");
            bWriter.write(txt1);
            //bWriter.write("thirt set of data");
            //bWriter.write(txt2);
            //bWriter.write("adesso i reali");
            //bWriter.write(txt2);
            bWriter.close();
            writer.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
    }
    
    public void searchWarmUpTime() {
        String tot = "";
        String totTime = "";
        String id = "t124";
     
        //second loop for every dps
        for (Double time : data.getDpsID(id).keySet()) {
            double sum = 0;
            //loop for each dps's entry
            for (String entry : data.getDpsID(id).get(time).keySet()) {
                sum += data.getDpsID(id).get(time).get(entry);     
            }
            tot += (int)sum + "\n";
            totTime += ((int)(time.longValue())) + "\n";
        }

        printRes(tot+"\nTime : \n"+totTime);
    }
    
    public void printRes(String result) {
        try {
            FileWriter writer = new FileWriter("/home/gabriele/Documenti/risultati.txt");
            BufferedWriter bWriter = new BufferedWriter(writer);
            bWriter.write(result);
            bWriter.close();
            writer.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
    }
}
