/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import parser.Parser;

/**
 *
 * @author gabriele
 */
public class Reader {
    
    private File file;
    private Parser parser;
    
    public Reader (File file, Parser parser) {
        this.file = file;
        this.parser = parser;
    }
    
    public void read() {
        if (file == null) {
            return;
        }

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                parser.parseLine(line);
            }
        } catch (IOException e) {

        }
    }
    
    public Parser getParser() {
        return parser;
    }
}
