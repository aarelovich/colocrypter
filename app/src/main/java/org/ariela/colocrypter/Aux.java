package org.ariela.colocrypter;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import androidx.core.content.PermissionChecker;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.provider.Settings;
import android.media.MediaScannerConnection;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by ariela on 12/30/17.
 */

public class Aux {

//    public static class ReqPermReturn{
//        String permissions[];
//        int code;
//    }

    // System wide constants.
    public static final String INTENT_FIRST_TIME            = "intent_first_time";
    public static final String INTENT_ENTRY_TO_EDIT         = "intent_entry_to_edit";
    public static final String INTENT_FROM_IMPORT_MESSAGE   = "intent_from_import_message";

//    public static final int REQUEST_CODE_ASK_WRITE       = 1;
//    public static final int REQUEST_CODE_ASK_READ        = 2;
//    public static final int REQUEST_CODE_ASK_READ_WRITE  = 3;
//    public static final int REQUEST_NOTHING              = 4;
//
//    public static final int REQPERM_RESULT_READ          = 0;
//    public static final int REQPERM_RESULT_WRITE         = 1;
//    public static final int REQPERM_RESULT_RW            = 2;
//    public static final int REQPERM_RESULT_OK            = 3;
//    public static final int REQPERM_RESULT_UNKNOWN       = 4;

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
//    public static final String LOADFILE = "data.csv";
//    public static final String WORKDIR  = "ccrypt";
    public static final String SHARED_PREF_STRING_KEY = "uri_ccrypt_dir";
//    private static String FULL_PATH_DATA;
//    private static String FULL_PATH_WORKDIR;
    private static String encryptionPassword;

    // App wide access to appData.
    public static PasswordData appData;

    // Intialization function;
    public static int Init(Context context){

        appData = new PasswordData();
        encryptionPassword = "";

        DbugCcrypt("Int Version of Android: " + Integer.toString(Build.VERSION.SDK_INT));

        //GeneratePaths();

        //Aux.DbugCcrypt("Setting the Full Path to be " + FULL_PATH_WORKDIR);

//        File dir = new File(FULL_PATH_WORKDIR);
//
//        // We need to make sure that the directory exists.
//        if (!dir.exists()){
//            try {
//                dir.mkdir();
//            }
//            catch (Exception e){
//                DbugCcrypt("Failed in creating ccrypt directory. Reason: " + e.getMessage());
//                return INIT_FAILED_TO_CREATE_DIR;
//            }
//        }


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

//    public static void GeneratePaths(){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//            //DbugCcrypt("Android version 11 or higher");
//            FULL_PATH_DATA = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
//        }
//        else {
//            //DbugCcrypt("Android version lower than 11");
//            FULL_PATH_DATA = Environment.getExternalStorageDirectory().toString();
//        }
//
//        FULL_PATH_WORKDIR = FULL_PATH_DATA + "/" + WORKDIR;
//        FULL_PATH_DATA = FULL_PATH_WORKDIR + "/" + DATAFILE;
//    }

//    public static void PrepareCryptFile(Context context){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//            File file = new File(FULL_PATH_DATA);
//            String filePath = file.toString();
//            String mimeType = null;
//            MediaScannerConnection.scanFile(context, new String[]{filePath}, new String[]{mimeType}, new MediaScannerConnection.OnScanCompletedListener() {
//                @Override
//                public void onScanCompleted(String path, Uri uri) {
//                    Aux.DbugCcrypt("Scan File Completed. Path: " + path);
//                    if (uri == null){
//                        Aux.DbugCcrypt("File Does not exist");
//                    }
//                    else {
//                        Aux.DbugCcrypt("Scan File Completed. Uri: " + uri.toString());
//                    }
//                }
//            });
//        }
//    }

    public static void DbugCcrypt(String msg){
        System.err.println("[CCRYPT_DBUG] " + msg);
    }

//    public static String getDataCSVPath(){
//        return FULL_PATH_WORKDIR + "/" + LOADFILE;
//    }

    public static AESEngine.AESReturn encrypt(String password, Context context){
        if (!password.isEmpty()){
            encryptionPassword = password;
        }

        AESEngine aesEngine = new AESEngine();
        AESEngine.AESReturn aer = null; //aesEngine.getEmptyReturn();

//        GeneratePaths(); // It should not be necessary to do but somehow sometimes the Full Path Dir is null here. I don't know why.
//
//        File dir = new File(FULL_PATH_WORKDIR);
//
//        if (!dir.exists()) {
//            aer.lastError = "CCrypt Directory Should Exist at this point";
//            aer.retCode = AESEngine.AES_CANNOT_MAKE_DIR;
//            aer.data = "";
//            return aer;
//        }

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

//        aer = aesEngine.decrypt(FILE_URI);
//        return aer;

    }

    // Function that checks if permissions have been granted.
//    public static ReqPermReturn getRequiredPermissions(Activity activity){
//
//        int code = REQUEST_NOTHING;
//        if (PermissionChecker.checkSelfPermission(activity,
//                Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PermissionChecker.PERMISSION_GRANTED) {
//            code = Aux.REQUEST_CODE_ASK_READ;
//        }
//
//        if (PermissionChecker.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PermissionChecker.PERMISSION_GRANTED){
//            if (code != 0) code = Aux.REQUEST_CODE_ASK_READ_WRITE;
//            else code = Aux.REQUEST_CODE_ASK_WRITE;
//        }
//
//        // Checking what permissions are required.
//        String permissions[] = null;
//        switch (code) {
//            case Aux.REQUEST_CODE_ASK_READ:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//                    permissions = new String[3];
//                    permissions[0] = Manifest.permission.READ_MEDIA_IMAGES;
//                    permissions[1] = Manifest.permission.READ_MEDIA_AUDIO;
//                    permissions[2] = Manifest.permission.READ_MEDIA_VIDEO;
//                }
//                else {
//                    permissions = new String[1];
//                    permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
//                }
//                break;
//            case Aux.REQUEST_CODE_ASK_WRITE:
//                permissions = new String[1];
//                permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//                break;
//            case Aux.REQUEST_CODE_ASK_READ_WRITE:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//                    permissions = new String[4];
//                    permissions[0] = Manifest.permission.READ_MEDIA_IMAGES;
//                    permissions[1] = Manifest.permission.READ_MEDIA_AUDIO;
//                    permissions[2] = Manifest.permission.READ_MEDIA_VIDEO;
//                    permissions[3] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//                }
//                else {
//                    permissions = new String[2];
//                    permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
//                    permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//                }
//                break;
//        }
//
//        ReqPermReturn rpr = new ReqPermReturn();
//        rpr.permissions = permissions;
//        rpr.code = code;
//        return rpr;
//    }
//
//    // Function that checks permission results.
//    public static int requestPermissionResult(int requestCode, int[] grantResults){
//        switch (requestCode) {
//            case Aux.REQUEST_CODE_ASK_READ:
//                if (wasReadGranted(grantResults)) {
//                    // Permission Granted
//                    return REQPERM_RESULT_OK;
//                }
//                else {
//                    // Permission Denied
//                    return REQPERM_RESULT_READ;
//                }
//            case Aux.REQUEST_CODE_ASK_WRITE:
//                if (wasWriteGranted(grantResults)) {
//                    // Permission Granted
//                    return REQPERM_RESULT_OK;
//                }
//                else {
//                    // Permission Denied
//                    return REQPERM_RESULT_READ;
//                }
//            case Aux.REQUEST_CODE_ASK_READ_WRITE:
//                int code = REQPERM_RESULT_OK;
//                if (!wasReadGranted(grantResults)) code = REQPERM_RESULT_READ;
//                if (wasWriteGranted(grantResults)) {
//                    if (code != REQPERM_RESULT_OK) code = REQPERM_RESULT_WRITE;
//                    else code = REQPERM_RESULT_RW;
//                }
//                return code;
//            default: return REQPERM_RESULT_UNKNOWN;
//        }
//    }
//
//    private static boolean wasReadGranted(int[] grantResults){
//
//        if (grantResults.length < 1) return false;
//
//        boolean wasReadGranted = true;
//
//        int nmax = 1;
//        if (grantResults.length > 1) nmax = grantResults.length - 1; // If there is only one, that the read permission. Otherwise it is all bu the last one.
//
//        for (int i = 0; i < nmax; i++){
//            wasReadGranted = wasReadGranted && (grantResults[i] == PackageManager.PERMISSION_GRANTED);
//        }
//        return wasReadGranted;
//    }
//
//    private static boolean wasWriteGranted(int[] grantResults){
//        if (grantResults.length < 1) return false;
//        int writeIndexPermission = grantResults.length-1;
//        return (grantResults[writeIndexPermission] == PackageManager.PERMISSION_GRANTED);
//    }

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
