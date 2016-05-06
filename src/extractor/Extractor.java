/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package extractor;

import Core.Core;
import java.io.File;
import java.util.HashMap;
import java.util.Set;
import parser.AdjParser;
import parser.DpsParser;
import reader.Reader;
import util.DpsData;

/**
 *
 * @author gabriele
 */
public class Extractor {

    private static final String dpsFileLocation = "/home/gabriele/Documenti/git/the-one/reports/PRoPHET2-30siu_SpyDeliveryPredictabilitiesReport.txt";
    private static final String adjFileLocation = "/home/gabriele/Documenti/git/the-one/reports/PRoPHET2-30siu_AdjacencyGraphvizReport.txt";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Reader reader = new Reader(new File(dpsFileLocation), new DpsParser());
        reader.read();
        
       //System.out.println(reader.getParser().getDpsData().toString());
        
        Reader reader1 = new Reader(new File(adjFileLocation), new AdjParser());
        reader1.read();

       // System.out.println(reader1.getParser().getDpsData().toString());

       Core core = new Core(DpsData.getDpsData());
       core.calculateEstimateEncounters();
    }

}
