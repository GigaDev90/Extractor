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
public class AnalizeWithTransRule extends Analize {

    @Override
    public int onFirstEncounter(double pred, double dTime) {
        double pAOld = pred / (Math.pow(GAMMA, dTime));

        if (pred >= 0.91) {
            // System.out.println("pAOld = " + pAOld + " pred = " + pred + " dTime = " + dTime / SECONDINTIMEUNIT);
            return 1;
        }

        return 0;
    }

    @Override
    public int analize(Map.Entry<Double, CouplePlus> previousEntry, Map.Entry<Double, CouplePlus> entry) {
        int enc = 0;
        double dTime = (entry.getKey() - previousEntry.getKey()) / SECONDINTIMEUNIT;
        double aged = ageDeliveryPreds(dTime, previousEntry.getValue().getPred());
        //System.out.printf("aged = %f\n", aged);
        //if (Math.abs(aged - entry.getValue().getPred()) > 0.58) {
            //System.out.println("inc");
          //  enc++;

            double encounterTime = encounterTime(entry.getValue().getPred(), previousEntry.getValue().getPred(), dTime);

                //enc++;
                //System.out.println("double inc");
                if (encounterTime > dTime ) {
                    // System.out.println("triple inc");
                    enc++;
                }
                if (encounterTime > dTime  && entry.getValue().getPred() > previousEntry.getValue().getPred() ) {
                    System.out.println("true!!!!!!!!!!!1");
                } else if ( encounterTime > dTime  && entry.getValue().getPred() < previousEntry.getValue().getPred()) {
                    System.out.println("false!!!!!!!!!!!1");
                }
            
        //}
        return enc;
    }

}
