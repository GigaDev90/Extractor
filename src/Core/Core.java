/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Core;

import extractor.Extractor;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import util.Couple;
import util.CouplePlus;
import util.DpsData;

/**
 *
 * @author gabriele
 */
public class Core {

    private final DpsData data;
    public static final double P_INIT = 0.75;
    public static final double DEFAULT_BETA = 0.25;
    public static final int SECONDINTIMEUNIT = 30;
    public static final double STARTTIME = 9000;
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
        double pAOld = pred / (Math.pow(GAMMA, dTime));

        if (pAOld >= 0.85 && pred > 0.000001) {
           // System.out.println("pAOld = " + pAOld + " pred = " + pred + " dTime = " + dTime / SECONDINTIMEUNIT);
            if ( pred > 0.80 ) {
                return 2;
            }
            return 1;
        }
        
        return 0;
    }

    private double encounterTime(double newPred, double oldPred, double time) {
        double nm = newPred - (Math.pow(GAMMA, time) * (oldPred - (oldPred * P_INIT)));
        double dnm = Math.pow(GAMMA, time) * P_INIT;
        double w = -logOfBase(GAMMA, nm / dnm);
        //System.out.println("w = "+w+" time = "+time);
//        double oldPreAndDecay = ageDeliveryPreds(w, oldPred);
//        double oldPredAndEnc = updateDeliveryPredFor(oldPreAndDecay);
//        double testNew = ageDeliveryPreds(time - w, oldPredAndEnc);

        return w;
    }
    
    public void calcEstimateEnc() {
        for (Couple key : data.getPredForCouple().keySet()) {

            LinkedHashMap<Double, CouplePlus> sortedList = data.sort(data.getPredForCouple().get(key));
            Map.Entry<Double, CouplePlus> previousEntryA = null;
            Map.Entry<Double, CouplePlus> previousEntryB = null;
            int tmpEnc = 0;
            
            for (Map.Entry<Double, CouplePlus> keyTime_valueCouple : sortedList.entrySet()) {
              // System.out.println("id = " + keyTime_valueCouple.getValue().getNodeA() + " entry = " + keyTime_valueCouple.getValue().getNodeB() + " time = " + keyTime_valueCouple.getKey() / SECONDINTIMEUNIT + " pred = " + keyTime_valueCouple.getValue().getPred());

                if (previousEntryA == null && previousEntryB == null) { //init
                    
                    previousEntryA = keyTime_valueCouple;
                    double dTime = (keyTime_valueCouple.getKey() - STARTTIME) / SECONDINTIMEUNIT;
                    int n = onFirstEncounter(keyTime_valueCouple.getValue().getPred(), dTime);
                    data.addEstimatedEnc(keyTime_valueCouple.getValue(), n);
                    //System.out.println("start NodeA with "+keyTime_valueCouple.getValue().getNodeA()+" first enc = "+n);
                  

                } else if (previousEntryA.getValue().getNodeA().equals(keyTime_valueCouple.getValue().getNodeA())) {
                  //  System.out.println("branch "+keyTime_valueCouple.getValue().getNodeA());
                    int n = analize(previousEntryA, keyTime_valueCouple);
                    data.addEstimatedEnc(keyTime_valueCouple.getValue(), n);
                    previousEntryA = keyTime_valueCouple;
                    //System.out.println("inc enc by = "+n+" owner = "+keyTime_valueCouple.getValue().getNodeA());
                    if ( tmpEnc > data.getEstimatedEnc(keyTime_valueCouple.getValue()) ) {
                        data.addEstimatedEnc(keyTime_valueCouple.getValue(), tmpEnc -  data.getEstimatedEnc(keyTime_valueCouple.getValue()));
                       // System.out.println("align first");
                    }

                } else if (previousEntryB == null) {
                    
                    previousEntryB = keyTime_valueCouple;
                    double dTime = (keyTime_valueCouple.getKey() - STARTTIME) / SECONDINTIMEUNIT;
                    tmpEnc = onFirstEncounter(keyTime_valueCouple.getValue().getPred(), dTime);
                    //System.out.println("start NodeB with "+keyTime_valueCouple.getValue().getNodeA()+" first enc = "+tmpEnc);
                    if ( tmpEnc < data.getEstimatedEnc(keyTime_valueCouple.getValue()) ) {
                        tmpEnc = data.getEstimatedEnc(keyTime_valueCouple.getValue());
                        //System.out.println("align second");
                    }

                } else {
                   // System.out.println("branch "+keyTime_valueCouple.getValue().getNodeA());
                    int n = analize(previousEntryB, keyTime_valueCouple);
                    tmpEnc += n;
                    previousEntryB = keyTime_valueCouple;
                    //System.out.println("inc enc by = "+n+" owner = "+keyTime_valueCouple.getValue().getNodeA());
                    if ( tmpEnc < data.getEstimatedEnc(keyTime_valueCouple.getValue()) ) {
                        tmpEnc = data.getEstimatedEnc(keyTime_valueCouple.getValue());
                        //System.out.println("align second");
                    }
                }

            }
            if (data.getEstimatedEnc(key) < tmpEnc) {
                data.addEstimatedEnc( key, tmpEnc - data.getEstimatedEnc(key) );
            }
        }

    }
    
    
    private int analize(Map.Entry<Double, CouplePlus> previousEntry, Map.Entry<Double, CouplePlus> entry) {

        int enc = 0;
        double dTime = (entry.getKey() - previousEntry.getKey()) / SECONDINTIMEUNIT;
        double aged = ageDeliveryPreds(dTime, previousEntry.getValue().getPred());
        //System.out.printf("aged = %f\n", aged);
        if (Math.abs(aged - entry.getValue().getPred()) > 0.000001) {
            //System.out.println("inc");
            enc++;
            
            double encounterTime = encounterTime(entry.getValue().getPred(), previousEntry.getValue().getPred(), dTime);

            if ( encounterTime > dTime ) {
                enc++;
                //System.out.println("double inc");
                if ( (encounterTime - dTime) > 10 ) {
                    // System.out.println("triple inc");
                    enc++;
                }
            }  
        } 
        return enc;
    }
   
    public void extractData() {
        double rapport[] = new double[data.getEstimateEncList().size()];
        int errorForCouple[] = new int[data.getEstimateEncList().size()];
        int sample[] = new int[data.getEstimateEncList().size()];
        int real[] = new int[data.getEstimateEncList().size()];
        int noEnc = 0;
        int i = 0;
        
        for (Map.Entry<Couple, Integer> entry : data.getEstimateEncList().entrySet()) {
            
            //calc real/est;
            if ( entry.getValue() == 0 && data.getRealEnc(entry.getKey()) == 0 ) {
                rapport[i] = 1;
            } else if ( entry.getValue() == 0 || data.getRealEnc(entry.getKey()) == 0 ) {
                noEnc++;
                continue;
            } else {
                rapport[i] = data.getRealEnc(entry.getKey()) / entry.getValue();
            }
            
            //save sample
            sample[i] = data.getPredForCouple().get(entry.getKey()).size();
            
            //calc error for couple
            errorForCouple[i] = data.getRealEnc(entry.getKey()) - entry.getValue();
            
            //real enc
            real[i] = data.getRealEnc(entry.getKey());
            
            System.out.println("raport = "+rapport[i]+" sample = "+sample[i]+" errorforCouple = "+errorForCouple[i]+" real[i] = "+real[i]);
            i++;
        }
        
        printRes(rapport, errorForCouple, sample, real, noEnc);
    }

    public void result() {
        String txt0 = "";
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
        
        for (Couple entry : data.getEstimateEncList().keySet()) {
            //txt1 += data.getEstimatedEnc(entry) + "\n";
            //txt2 += data.getRealEnc(entry.getNodeA(), entry.getNodeB()) + "\n";
            double real = data.getRealEnc(entry);
            double est = data.getEstimatedEnc(entry);
           // if ( real < 9 ) continue;
            int summ = 0;
            for (Couple key : data.getPredForCouple().keySet()) {
                if (key.equals(entry)) {
//                    boolean first = true;
//                    boolean second = true;
//                    String owner = "";
//
//                    int a = 0, b = 0;
                    for ( Double time :  data.getPredForCouple().get(key).keySet() ) {
                        if ( data.getPredForCouple().get(key).get(time).getPred() > 0.1 ) {
                            summ++;
                        }
                    }
//                    if ( a > b ) {
//                        txt1 += a + "\n";
//                    } else {
//                        txt1 += b + "\n";
//                    }
//                    
                    //txt2 += wastTime > wastTime2 ? wastTime + "\n" : wastTime2 + "\n";
                    txt1 += summ+"\n";
                    //test4 = summ;
                    break;
                }
            }
            
            if (est == 0 && real == 0) {
                rapport = 1;
            } else if (est == 0 ) {
                //System.out.println("patatrack");
               continue;
            } else {
                rapport = real/est;
            }
            
           
            //System.out.println("real = "+(data.getRealEnc(entry.getNodeA(), entry.getNodeB()))+" est = "+(data.getEstimatedEnc(entry)));
            //System.out.println("id = "+entry.getNodeA()+" entry = "+entry.getNodeB()+"\n");
           
            txt3 += rapport + "\n";
            txt0 += real + "\n";
          
            
            txt2 += Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry))+ "\n";
                
            if (Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry)) < 5) {
                test++;
            }
            if (Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry)) < 3) {
                testA++;
            }
            if (rapport < 1) {
                testB++;
            }
            test1++;
     
            //if ( Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry.getNodeA(), entry.getNodeB())) < 20 )
            test3 += Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry));
            testC += (data.getEstimatedEnc(entry) - data.getRealEnc(entry));
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
            bWriter.write(txt0);
            bWriter.write("thirt set of data");
            bWriter.write(txt2);
            //bWriter.write("adesso i reali");
            //bWriter.write(txt2);
            bWriter.close();
            writer.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
    }
    
//    public void searchWarmUpTime() {
//        String tot = "";
//        String totTime = "";
//        String id = "t124";
//     
//        //second loop for every dps
//        for (Double time : data.getDpsID(id).keySet()) {
//            double sum = 0;
//            //loop for each dps's entry
//            for (String entry : data.getDpsID(id).get(time).keySet()) {
//                sum += data.getDpsID(id).get(time).get(entry);     
//            }
//            tot += (int)sum + "\n";
//            totTime += ((int)(time.longValue())) + "\n";
//        }
//
//        printRes(tot+"\nTime : \n"+totTime);
//    }
    
    public void printRes(double[] rapport, int[] errorForCouple, int[] sample, int[] real, int noEnc) {
        StringTokenizer st = new StringTokenizer(Extractor.dpsFileLocation);
        String name = st.nextToken("reports/");
       // String name = st.nextToken("_");
         try {
            String filename = "/home/gabriele/Documenti/resultOf"+name+".ods" ;
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("FirstSheet");  

            HSSFRow rowhead = sheet.createRow((short)0);
            rowhead.createCell(0).setCellValue("rapport");
            rowhead.createCell(1).setCellValue("errorForCouple");
            rowhead.createCell(2).setCellValue("sample");
            rowhead.createCell(3).setCellValue("real");
            rowhead.createCell(4).setCellValue("noEnc");

            for ( int i = 0; i < rapport.length; i++ ) {
                HSSFRow row = sheet.createRow((short)i+1);
                row.createCell(0).setCellValue(rapport[i]);
                row.createCell(1).setCellValue(errorForCouple[i]);
                row.createCell(2).setCellValue(sample[i]);
                row.createCell(3).setCellValue(real[i]);
                
                if ( i == 0 )
                    row.createCell(4).setCellValue(noEnc);

            }

            FileOutputStream fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated!");

        } catch ( Exception ex ) {
            System.out.println(ex);
        }
    }
}
