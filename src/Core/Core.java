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
import static sun.misc.VM.getState;
import util.Couple;
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
        double timeDiff = time;
        if (timeDiff == 0) {
            return pred;
        }

        double mult = Math.pow(GAMMA, timeDiff);
        return pred * mult;
    }
    
    private int onFirstEncounter(double pred) {
        int n = 0;
        if (pred <= 0.70) {
            n = 1;
        } else if (pred <= 0.80) {
            n = 2;
        } else {
            n = 3;
        }
        return 0;
    }
    
    private double verifyOneEnc(double newPred, double oldPred, double time) {
        double nm = newPred -(Math.pow(GAMMA, time) * (oldPred - (oldPred * P_INIT)));
        double dnm = Math.pow(GAMMA, time) * P_INIT;
        double w = - logOfBase(GAMMA, nm/dnm);
        //System.out.println("w = "+w+" time = "+time);
        double oldPreAndDecay = ageDeliveryPreds(w, oldPred);
        double oldPredAndEnc = updateDeliveryPredFor(oldPreAndDecay);
        double testNew =  ageDeliveryPreds(time - w, oldPredAndEnc);
        if ( Math.abs(testNew - newPred) > 0.1 ) {
            System.out.println("erroreeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        }
        return w;
    }
    
    public void calculateEstimateEncounters() {
        //first loop for all unique node
        for (String id : data.getData().keySet()) {
            double oldTime = 0;
            //second loop for every dps
            for (Double time : data.getDpsID(id).keySet()) {
                
                //loop for each dps's entry
                for (String entry : data.getDpsID(id).get(time).keySet()) {
                    if (oldTime == 0) {
                        int n = onFirstEncounter(data.getDpsID(id).get(time).get(entry));
                      
                        data.addEstimatedEnc(id, entry, n);
                        int real = data.getRealEnc(id, entry);
                        //System.out.println("Estimate enc of " + id + " to encounter " + entry + " = " + data.getEstimatedEnc(id, entry) + " real = " + real + " with this pred " + data.getDpsID(id).get(time).get(entry));
                    } else {
                        if (data.getDpsID(id).get(oldTime).get(entry) ==  null) {
                            int n = onFirstEncounter(data.getDpsID(id).get(time).get(entry));
                            data.addEstimatedEnc(id, entry, n);
                            int real = data.getRealEnc(id, entry);
                            //System.out.println("Estimate enc of " + id + " to encounter " + entry + " = " + n + " real = " + real + " with this pred " + data.getDpsID(id).get(time).get(entry));
                        } else {
                            double oldPred = data.getDpsID(id).get(oldTime).get(entry);
                            double newPred = data.getDpsID(id).get(time).get(entry);

                            double testPred = updateDeliveryPredFor(oldPred);
                            double testPred2 = ageDeliveryPreds((time - oldTime)/secondsInTimeUnit, oldPred);
                            //System.out.println("firsssssttt "+time);
                            double secondComp = 0; 
                            if ( (newPred - oldPred) >= 0) {
                                secondComp = newPred - oldPred;
                            } else if ( Math.abs(newPred - oldPred) < 0.5 ) {
                                secondComp = Math.abs(newPred - oldPred);
                            } else if ( Math.abs(newPred - oldPred) > 0.6 ) {
                                secondComp = 0;
                            }
                            
                           
                            double test = verifyOneEnc(newPred, oldPred, (time - oldTime)/secondsInTimeUnit);
                            
                            
                            if (test > 0) {
                                data.addEstimatedEnc(id, entry, 1);
                            } else {
                                //data.addEstimatedEnc(id, entry, (int)-test/10);
                            }
                            
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
            for (Couple entry : data.getEstimateEncList().keySet()) {
                txt1 += data.getEstimatedEnc(entry)+"\n";
                txt2 += data.getRealEnc(entry.getNodeA(), entry.getNodeB())+"\n";
                   System.out.println("Estimate enc of " + entry.getNodeA() + " to encounter " + entry.getNodeB() + " = " + data.getEstimatedEnc(entry) + " real = " + data.getRealEnc(entry.getNodeA(), entry.getNodeB()));
                   //System.out.println("history of "+id+" "+data.getDpsID(id).toString());
            }
            
            try {
            FileWriter writer = new FileWriter("/home/gabriele/Documenti/risultati.txt");
            BufferedWriter bWriter = new BufferedWriter(writer);
            bWriter.write(txt1);
            bWriter.write("adesso i reali");
            bWriter.write(txt2);
            bWriter.close();
            writer.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }

            
        //}
    }
}
