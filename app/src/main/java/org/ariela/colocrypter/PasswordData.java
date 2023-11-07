package org.ariela.colocrypter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by ariela on 12/29/17.
 */

// Simple class to hold the username and password.
class Entry {

    String userName;
    String passWord;
    String notes;

    public Entry(){
        userName = "";
        passWord = "";
        notes    = "";
    }

}

public class PasswordData {

    // The data to be constructed.
    private HashMap<String,Entry> passwordData;
    private HashMap<String,String> configurationData;
    private List<String> keyList;


    public PasswordData() {
        passwordData = new HashMap<>();
        configurationData = new HashMap<>();
        keyList = new ArrayList<>();
    }

    public void clear(){
        passwordData.clear();
        keyList.clear();
        configurationData.clear();
    }

    public boolean setData(String rawData){
        if (rawData.isEmpty()) return true;

        // Parsing configuration options.
        String config = "";
        int configStart = rawData.indexOf(Aux.CONFIG_START_STRING);
        if (configStart != -1){
            config = rawData.substring(configStart+Aux.CONFIG_START_STRING.length());
            rawData = rawData.substring(0,configStart);
        }
        setConfigurationData(config);

        // Parsing password data.
        String[] entries = rawData.split(Aux.ENTRY_DELIMITER);
        for (int i = 0; i < entries.length; i++){
            Entry e = new Entry();
            String entryline = entries[i];
            ArrayList<String> fields = parseEntry(entryline);

            // For DEBUGGING
//            System.err.print("EntryLine is: " + entryline + ". FIELDS: ");
//            for (int k = 0; k < fields.size(); k++){
//                System.err.print(fields.get(k) + " ");
//            }
//            System.err.println("");

            // Breaks the program if somehow no id gets saved.
            if (fields.get(0).isEmpty()) continue;

            if ((fields.size() != 3) && (fields.size() != 4)){
                //System.err.println("FORMAT ERR: Line: "  + entries[i] + " has " + fields.size() + " fields: " + fields);
                return false;
            }
            e.userName = fields.get(1);
            e.passWord = fields.get(2);
            if (fields.size() == 4){
                e.notes = fields.get(3);
            }
            passwordData.put(fields.get(0),e);
        }
        recreateKeyList();
        return true;
    }


    public Entry getEntry(String entry){
        Entry ret;
        if (passwordData.containsKey(entry)){
            ret = passwordData.get(entry);
        }
        else{
            ret = new Entry();
        }
        return ret;
    }

    public void saveEntry(String entryName, Entry entry){
        passwordData.put(entryName,entry);
        recreateKeyList();
    }

    public void replaceEntry(String toReplace, String newEntryName, Entry entry){
        if (passwordData.containsKey(toReplace)){
            passwordData.remove(toReplace);
        }
        saveEntry(newEntryName,entry);
    }

    public void deleteEntry(String entry){
        passwordData.remove(entry);
        recreateKeyList();
    }

    public String getRawData(){
        if (keyList.isEmpty()) return "";

        List<String> data = new ArrayList<>();
        for (int i = 0; i < keyList.size(); i++){
            Entry e = passwordData.get(keyList.get(i));
            data.add(keyList.get(i) + Aux.ENTRY_FIELD_DELIMITER_WRITE +
                    e.userName + Aux.ENTRY_FIELD_DELIMITER_WRITE +
                    e.passWord + Aux.ENTRY_FIELD_DELIMITER_WRITE + e.notes);
        }

        String passData = android.text.TextUtils.join(Aux.ENTRY_DELIMITER,data);

        data.clear();
        ArrayList<String> confkeys = new ArrayList<>(configurationData.keySet());
        for (int i = 0; i < confkeys.size(); i++){
           data.add(confkeys.get(i) + Aux.ENTRY_FIELD_DELIMITER_WRITE + configurationData.get(confkeys.get(i)));
        }

        String confData = android.text.TextUtils.join(Aux.ENTRY_DELIMITER,data);

        return passData + Aux.CONFIG_START_STRING + confData;

    }

    public boolean exists(String entry){
        return passwordData.containsKey(entry);
    }

    public List<String> getEntries(){
        return keyList;
    }

    public List<String> getFilteredEntries(String filter) {
        if (filter == null) return keyList;
        if (filter.isEmpty()) return keyList;

        List<String> list = new ArrayList<>();

        Pattern filterPattern = Pattern.compile(Pattern.quote(filter), Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < keyList.size(); i++){
            if (filterPattern.matcher(keyList.get(i)).find()){
                list.add(keyList.get(i));
            }
        }

        return list;

    }

    public int getConfigurationInteger(String key){
        if (!configurationData.containsKey(key)) return -1;
        return Integer.valueOf(configurationData.get(key));
    }

    public void setConfigurationValue(String key, String value){
        if (!configurationData.containsKey(key)) return;
        configurationData.put(key,value);
    }

    // Creating the keylist and sorting it.
    private void recreateKeyList(){
        keyList.clear();
        keyList = new ArrayList<>(passwordData.keySet());
        Collections.sort(keyList,new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
    }

    private boolean setConfigurationData(String config){
        if (config.isEmpty()){
            // Default values
            configurationData.put(Aux.CONFIG_LOGOUT_TIME,Integer.toString(Aux.DEFAULT_TIME_OUT));
        }
        else{
            String[] entries = config.split(Aux.ENTRY_DELIMITER);
            for (int i = 0; i < entries.length; i++){
                Entry e = new Entry();
                String configpair = entries[i];

                ArrayList<String> fields = parseEntry(configpair);

                // Configuration pairs should have a size of two.
                if ((fields.size() != 2)){
                    return false;
                }
                // Adding the configuration pair.
                configurationData.put(fields.get(0),fields.get(1));
            }

        }
        return true;
    }

    private ArrayList<String> parseEntry(String entry){
        ArrayList<String> fields = new ArrayList<>();
        String field = "";
        for (int j = 0; j < entry.length(); j++){
            if (entry.charAt(j) == Aux.ENTRY_FIELD_DELIMITER_READ) {
                fields.add(field);
                field = "";
            }
            else field = field + entry.charAt(j);
        }
        // Adding the last one.
        fields.add(field);
        return fields;
    }




}
