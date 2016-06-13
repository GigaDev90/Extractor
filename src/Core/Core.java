/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Core;

import extractor.Extractor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
            } else if ( entry.getValue() == 0 ) {
                noEnc++;
                rapport[i] = -1;
            } else {
                rapport[i] = (double)data.getRealEnc(entry.getKey()) / (double)entry.getValue();
            }
            
            //save sample
            sample[i] = data.getPredForCouple().get(entry.getKey()).size();
            
            //calc error for couple
            errorForCouple[i] = data.getRealEnc(entry.getKey()) - entry.getValue();
            
            //real enc
            real[i] = data.getRealEnc(entry.getKey());
            
          //  System.out.println("raport = "+rapport[i]+" sample = "+sample[i]+" errorforCouple = "+errorForCouple[i]+" real[i] = "+real[i]);
            i++;
        }
        
        printRes(rapport, errorForCouple, sample, real, noEnc);
    }

    public void test() {
        int soglia5 = 0;
        int soglia3 = 0;
        int over = 0;
        int erroreInEccesso = 0;
        int tot = 0;
        int erroreAssoluto = 0;
        int falseEnc = 0;
        double rapport = 0;
        
        for (Couple entry : data.getEstimateEncList().keySet()) {

            double real = data.getRealEnc(entry);
            double est = data.getEstimatedEnc(entry);

            
            if (est == 0 && real == 0) {
                rapport = 1;
            } else if (est == 0 ) {
                falseEnc++;
            } else {
                rapport = real/est;
            }
            
                
            if (Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry)) < 5) {
                soglia5++;
            }
            if (Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry)) < 3) {
                soglia3++;
            }
            if (rapport < 1) {
                over++;
            }
            tot++;

            erroreAssoluto += Math.abs(data.getEstimatedEnc(entry) - data.getRealEnc(entry));
            erroreInEccesso += (data.getEstimatedEnc(entry) - data.getRealEnc(entry));

        }

        System.out.println("soglia5 = " + soglia5 + " soglia3 = " + soglia3 + "  minore di zero = " + over + " su = " + tot);
        System.out.println("error Assoluto = " + erroreAssoluto + " errore in eccesso " + erroreInEccesso);
        System.out.println("falsi incontri = " + falseEnc);

     
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
        st.nextToken("-");
        st.nextToken("-");
        String name = st.nextToken("_");
        //int numOfPage = 4;
        
        try {

            String filename = "/home/gabriele/Documenti/risultati prophetSpy/result " + name + ".xlsx";
            FileInputStream fileInput = null;
            Sheet sheet;
            Workbook workbook = null;
            try {
                fileInput = new FileInputStream(filename);
                workbook = create(fileInput);
                sheet = workbook.getSheetAt(0);
                
                System.out.println("found xlsx file");
            } catch (FileNotFoundException fileNotFoundException) {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("foglio 1");
               
                System.out.println("no file found");
            }
            
            Row rowhead = sheet.createRow(0);
            rowhead.createCell(0).setCellValue("rapport");
            rowhead.createCell(1).setCellValue("errorForCouple");
            rowhead.createCell(2).setCellValue("sample");
            rowhead.createCell(3).setCellValue("real");
            rowhead.createCell(7).setCellValue("est = 0");
            rowhead.createCell(8).setCellValue("Total Couple");

            int numRow = 1;
            for (int j = 0; j < rapport.length; j++) {
                if (rapport[j] != -1) {
                    Row row = sheet.createRow(numRow);
                    row.createCell(0).setCellValue(rapport[j]);
                    row.createCell(1).setCellValue(errorForCouple[j]);
                    row.createCell(2).setCellValue(sample[j]);
                    row.createCell(3).setCellValue(real[j]);
                    numRow++;
                }
            }

            sheet.getRow(1).createCell(7).setCellValue(noEnc);
            sheet.getRow(1).createCell(8).setCellValue(rapport.length);
            
            FileOutputStream fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated!");

        } catch ( Exception ex ) {
            System.out.println(ex);
        }
    }
    
    /**
     * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
     *  the given InputStream.
     * Your input stream MUST either support mark/reset, or
     *  be wrapped as a {@link PushbackInputStream}!
     */
    public Workbook create(InputStream inp) throws InvalidFormatException, IOException {
            // If clearly doesn't do mark/reset, wrap up
            if (!inp.markSupported()) {
                inp = new PushbackInputStream(inp, 8);
            }
            
            if (POIFSFileSystem.hasPOIFSHeader(inp)) {
                return new HSSFWorkbook(inp);
            }
            if (POIXMLDocument.hasOOXMLHeader(inp)) {
                return new XSSFWorkbook(OPCPackage.open(inp));
            }
            throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
      
    }
}
