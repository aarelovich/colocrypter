/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.colocrypter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ariela
 */
public class CSVReader {
    
    // Status codes.
    public static final int CSV_COULD_NOT_READ_FILE = 0;
    public static final int CSV_OK = 2;
    public static final int CSV_BAD_CSV_FORMAT = 1;
    
    // File location is known.
    private final int CSV_STATE_INIT = 0;
    private final int CSV_STATE_DQUOTES_WORD = 2;
    private final int CSV_STATE_DQUOTES_DQUOTES = 3;
    private final int CSV_STATE_ADD_WORD = 5;
    private final int CSV_STATE_NOQUOTES_WORD = 4;
    
    // The status of the operation
    private int status;
    
    public CSVReader(){
        status = CSV_OK;
    }
    
    public int getStatus(){
        return status;
    }
    
    // Loading the actual data.
    public List< List<String> > loadCSV(String inputFilePath){
        
       List < List<String> > results = new ArrayList<>();

       try{
          File inputF = new File(inputFilePath);
          InputStream inputFS = new FileInputStream(inputF);
          BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
          
          while (true){
              
              String line = br.readLine();
              if (line == null) break;
              results.add(parseLine(line));
              if (status != CSV_OK){
                  results.clear();
                  return results;
              }
              
          }
          
          br.close();
          
        } 
        catch (IOException e) {
            status = CSV_COULD_NOT_READ_FILE;
        }        
        
        if (status != CSV_OK) return results;
        
        return results;
    }
    
    private List<String> parseLine(String line){
        
        List<String> results = new ArrayList<>();
        
        if (line == null) return results;
        if (line.isEmpty()) return results;
        
        int state = CSV_STATE_INIT;
        int i = 0;
        String currentField = "";
        
        while (true){
            //if (i < line.length())
               //System.err.println("CF: " + currentField + " Char: " + line.charAt(i) + " State: " + state);
            switch(state){                
                case CSV_STATE_INIT:
                    if (i >= line.length()) return results;                    
                    switch (line.charAt(i)) {
                        case '"':
                            // Double quotes field
                            state = CSV_STATE_DQUOTES_WORD;
                            i++;
                            break;
                        case ',':
                            // Empty field
                            state = CSV_STATE_ADD_WORD;
                            i++;
                            break;
                        default:
                            // Normal filed
                            state = CSV_STATE_NOQUOTES_WORD;
                            break;
                    }                    
                    break;
                case CSV_STATE_ADD_WORD:
                    results.add(currentField);
                    currentField = "";
                    state = CSV_STATE_INIT;
                    break;
                case CSV_STATE_DQUOTES_DQUOTES:
                    if (i >= line.length()){
                        state = CSV_STATE_ADD_WORD;
                    }
                    else{
                        switch (line.charAt(i)) {
                            case '"':
                                // A double quotes is added to the string
                                currentField = currentField + '"';
                                state = CSV_STATE_DQUOTES_WORD;
                                break;
                            case ',':
                                // Empty field
                                state = CSV_STATE_ADD_WORD;
                                break;
                            default:
                                // Error
                                System.err.println("FORMAT ERROR");
                                System.err.println("CF: " + currentField  + " State: " + state);
                                results.clear();
                                status = CSV_BAD_CSV_FORMAT;
                                return results;
                        }
                        i++;
                    }
                    break;
                case CSV_STATE_DQUOTES_WORD:
                    if (i >= line.length()){
                        results.clear();
                        System.err.println("FORMAT ERROR");
                        System.err.println("CF: " + currentField + " State: " + state);                        
                        status = CSV_BAD_CSV_FORMAT;
                        return results;
                    }
                    if (line.charAt(i) != '"'){
                        currentField =  currentField + line.charAt(i);
                        i++;
                    }
                    else{
                        state = CSV_STATE_DQUOTES_DQUOTES;
                        i++;
                    }
                    break;
                case CSV_STATE_NOQUOTES_WORD:
                    if (i >= line.length()){
                        state = CSV_STATE_ADD_WORD;
                    }
                    else if (line.charAt(i) != ','){
                        currentField =  currentField + line.charAt(i);
                        i++;
                    }
                    else{
                        state = CSV_STATE_ADD_WORD;
                        i++;
                    }
                    break;
            }
            
        }
        
    }
    
}
