/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Core;

import java.util.Map;
import util.CouplePlus;

/**
 *
 * @author gabriele
 */
public abstract class Analize {
    
    public static final double P_INIT = 0.75;
    public static final double DEFAULT_BETA = 0.25;
    public static final int SECONDINTIMEUNIT = 30;
    public static final double STARTTIME = 9000;
    public static final double GAMMA = 0.98;
    
    public Analize() {}
    
    public double updateDeliveryPredFor(double oldValue) {
        return oldValue + (1 - oldValue) * P_INIT;
    }

    public double logOfBase(double base, double num) {
        return Math.log(num) / Math.log(base);
    }

    public double ageDeliveryPreds(double time, double pred) {
        if (time == 0) {
            return pred;
        }

        double mult = Math.pow(GAMMA, time);
        return pred * mult;
    }
    
    public double encounterTime(double newPred, double oldPred, double time) {
        double nm = newPred - (Math.pow(GAMMA, time) * (oldPred - (oldPred * P_INIT)));
        double dnm = Math.pow(GAMMA, time) * P_INIT;
        double w = -logOfBase(GAMMA, nm / dnm);
        //System.out.println("w = "+w+" time = "+time);
//        double oldPreAndDecay = ageDeliveryPreds(w, oldPred);
//        double oldPredAndEnc = updateDeliveryPredFor(oldPreAndDecay);
//        double testNew = ageDeliveryPreds(time - w, oldPredAndEnc);

        return w;
    }
    
    abstract public int onFirstEncounter(double pred, double dTime);
    abstract public int analize(Map.Entry<Double, CouplePlus> previousEntry, Map.Entry<Double, CouplePlus> entry);

}
