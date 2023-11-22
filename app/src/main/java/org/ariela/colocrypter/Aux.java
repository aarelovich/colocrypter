package org.ariela.colocrypter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;

/**
 * Created by ariela on 12/30/17.
 */

public class Aux {

    // System wide constants.
    public static final String INTENT_FIRST_TIME            = "intent_first_time";
    public static final String INTENT_ENTRY_TO_EDIT         = "intent_entry_to_edit";
    public static final String INTENT_FROM_IMPORT_MESSAGE   = "intent_from_import_message";

    public static final int MIN_ENC_PASSWORD_LENGTH      = 5;
    public static final int DEFAULT_GEN_PASSWORD_SIZE    = 8;
    public static final int MIN_GEN_PASSWORD_SIZE        = 4;

    // Draw limits
    public static final int MAX_CHARS_ENTRY               = 16;
    public static final int MAX_CHARS_PASSWORD            = 12;
    public static final int MAX_CHARS_USER                = 23;

    // Default timeout 5 minutes
    public static final int DEFAULT_TIME_OUT              = 300000;

    public static final String ENTRY_DELIMITER = "<>";
    public static final char ENTRY_FIELD_DELIMITER_READ = '|';
    public static final String ENTRY_FIELD_DELIMITER_WRITE = "|";
    public static final String CONFIG_START_STRING         = "<>CONFIGS<>";
    public static final String CONFIG_LOGOUT_TIME          = "logOutTime";

    public static final int INIT_RET_CODE_OK                = 0;
    public static final int INIT_FILE_EXISTS_CANTREAD       = 1;
    public static final int INIT_FILE_DOES_NOT_EXIST        = 2;

    public static final int ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_ONLY    = 100;
    public static final int ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_NO_FILE = 101;

    // AES Engine data
    public static final String DATAFILE = "data.aux";
    public static final String SHARED_PREF_STRING_KEY = "uri_ccrypt_dir";
    private static String encryptionPassword;

    // App wide access to appData.
    public static PasswordData appData;

    // Intialization function;
    public static int Init(Context context){

        appData = new PasswordData();
        encryptionPassword = "";

        DbugCcrypt("Int Version of Android: " + Integer.toString(Build.VERSION.SDK_INT));

        // Now we check using the URI.
        DbugCcrypt("Getting the Shared Preference URI");
        String dataAuxUriString = getStringInSharedPreferences(context,SHARED_PREF_STRING_KEY);
        if (!dataAuxUriString.isEmpty()){

            DbugCcrypt("Got the URI File. Testing Read/Write" );
            Uri dataAuxUri= Uri.parse(dataAuxUriString);
            DocumentFile dfile = DocumentFile.fromSingleUri(context,dataAuxUri);
            DbugCcrypt("Data File Exists: " + dfile.exists());
            DbugCcrypt("Data File Can be read: " + dfile.canRead());
            DbugCcrypt("Data File Can be written: " + dfile.canWrite());

            if (dfile.exists()){
                if (dfile.canWrite() && dfile.canRead()) return INIT_RET_CODE_OK;
                else return INIT_FILE_EXISTS_CANTREAD;
            }
            else return INIT_FILE_DOES_NOT_EXIST;

        }
        else {
            return INIT_FILE_DOES_NOT_EXIST;
        }


    }

    public static void DbugCcrypt(String msg){
        System.err.println("[CCRYPT_DBUG] " + msg);
    }

    public static AESEngine.AESReturn encrypt(String password, Context context){
        if (!password.isEmpty()){
            encryptionPassword = password;
        }

        AESEngine aesEngine = new AESEngine();
        AESEngine.AESReturn aer = null;

        aer = aesEngine.initEngine(encryptionPassword, context);
        if (aer.retCode != AESEngine.AES_OK){
            return aer;
        }

        String dataAuxUriString = getStringInSharedPreferences(context,SHARED_PREF_STRING_KEY);
        if (!dataAuxUriString.isEmpty()) {
            Uri dataAuxUri = Uri.parse(dataAuxUriString);
            aer = aesEngine.encrypt(appData.getRawData(),dataAuxUri);
        }
        else {
            aer.lastError = "Crypt File URI is Empty";
            aer.retCode = AESEngine.AES_WRITE_ERROR;
            aer.data = "";
        }
        return aer;


    }

    public static AESEngine.AESReturn decrypt(String password, Context context){

        encryptionPassword = password;

        AESEngine aesEngine = new AESEngine();

        AESEngine.AESReturn aer = aesEngine.initEngine(encryptionPassword,context);
        if (aer.retCode != AESEngine.AES_OK){
            return aer;
        }

        String dataAuxUriString = getStringInSharedPreferences(context,SHARED_PREF_STRING_KEY);
        if (!dataAuxUriString.isEmpty()) {
            Uri dataAuxUri = Uri.parse(dataAuxUriString);
            aer = aesEngine.decrypt(dataAuxUri);
        }
        else {
            aer.lastError = "Crypt File URI is Empty";
            aer.retCode = AESEngine.AES_READ_ERROR;
            aer.data = "";
        }
        return aer;

    }


    public static void showProblemDialog(Activity activity, String title, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static boolean validateString(String toValidate){
        boolean status = !toValidate.contains(ENTRY_DELIMITER);
        status = status && !toValidate.contains(ENTRY_FIELD_DELIMITER_WRITE);
        return status;
    }

    public static String adjustStringForShow(String s, int max_chars){
        if (s.length() <= max_chars) return s;
        String ret = s.substring(0,max_chars-1);
        ret = ret + "...";
        return ret;
    }

    public static Uri searchForCCryptFileOnStoredDirUri(Context context){

        String workDirUriString = getStringInSharedPreferences(context,SHARED_PREF_STRING_KEY);
        if (workDirUriString.isEmpty()) return null;

        DbugCcrypt("Setting the directory URI: " + workDirUriString);
        Uri workDirUri = Uri.parse(workDirUriString);

        DocumentFile documentFile = DocumentFile.fromTreeUri(context, workDirUri);
        for (DocumentFile file : documentFile.listFiles()) {
            String fname = file.getName();
            if(file.isDirectory()) continue;
            if (fname.equals(Aux.DATAFILE)){
                return file.getUri();
            }
        }
        return null;
    }

    public static void storeStringInSharedPreferences(Context context, String key, String value){
        SharedPreferences sharedPref = context.getSharedPreferences("application", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public static String getStringInSharedPreferences(Context context, String key){
        SharedPreferences sharedPref = context.getSharedPreferences("application", Context.MODE_PRIVATE);
        return sharedPref.getString(key, "");
    }


}
