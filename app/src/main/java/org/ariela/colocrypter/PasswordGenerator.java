/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ariela.colocrypter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author ariela
 */
public class PasswordGenerator {
    
    private static final String PassResUppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PassResLowercase = "abcdefghijklmnopqrstuvwxyz";
    private static final String PassResNumbers   = "0123456789";
    private static final String PassResSymbols   = "!@#$%&*()-_+=";
    

    public static class Configuration {
        boolean useSymbols;
        boolean useUppercase;
        boolean useNumbers;
        boolean useLowercase;
        int size;

        public Configuration(){
            useLowercase = true;
            useSymbols = false;
            useUppercase = true;
            useNumbers = true;
            size  = 8;
        }

        public boolean anyCharsUsed(){
            return useLowercase || useUppercase || useSymbols || useNumbers;
        }

    }    
    
    private Random rng;
    
    public PasswordGenerator(){
       rng = new Random(System.currentTimeMillis());        
    }
    
    
    public String generateRandomPassword(Configuration config){
        
        // Seeding the random number generator.        
        
        List<String> chars = new ArrayList<>();
        String source = "";
        
        // First the requirements are generated.
        if (config.useNumbers){
            chars.add(getRandomChar(PassResNumbers));
            source = source + PassResNumbers;
        }
        if (config.useSymbols){
            chars.add(getRandomChar(PassResSymbols));
            source = source + PassResSymbols;
        }
        if (config.useUppercase){
            chars.add(getRandomChar(PassResUppercase));
            source = source + PassResUppercase;
        }                
        if (config.useLowercase){
            chars.add(getRandomChar(PassResLowercase));
            source = source + PassResLowercase;
        }
        
        if (source.isEmpty()) return "";
        
        // Checking how many letters are left to go.
        int remain = config.size - chars.size();
        
        // Adding the remaing chars
        for (int i = 0; i < remain; i++){
            chars.add(getRandomChar(source));
        }
        
        // Finally the order is randomized.
        String password = "";
        while (chars.size() > 0){
            int i = rng.nextInt(chars.size());
            password = password + chars.get(i);
            chars.remove(i);
        }
        
        // The generated password is returned.
        return password;
        
    }


    private String getRandomChar(String source){        
        String ans = "";
        int i = rng.nextInt(source.length());
        ans = source.charAt(i) + ans;
        return ans;    
    }
}
