package org.ariela.colocrypter;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import androidx.core.content.PermissionChecker;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;


import java.io.File;
import java.io.FileWriter;

/**
 * Created by ariela on 12/30/17.
 */

public class Aux {

    public static class ReqPermReturn{
        String permissions[];
        int code;
    }

    // System wide constants.
    public static final String INTENT_FIRST_TIME            = "intent_first_time";
    public static final String INTENT_ENTRY_TO_EDIT         = "intent_entry_to_edit";
    public static final String INTENT_FROM_IMPORT_MESSAGE   = "intent_from_import_message";

    public static final int REQUEST_CODE_ASK_WRITE       = 1;
    public static final int REQUEST_CODE_ASK_READ        = 2;
    public static final int REQUEST_CODE_ASK_READ_WRITE  = 3;
    public static final int REQUEST_NOTHING              = 4;

    public static final int REQPERM_RESULT_READ          = 0;
    public static final int REQPERM_RESULT_WRITE         = 1;
    public static final int REQPERM_RESULT_RW            = 2;
    public static final int REQPERM_RESULT_OK            = 3;
    public static final int REQPERM_RESULT_UNKNOWN       = 4;

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

    // AES Engine data
    private static final String DATAFILE = "data.aux";
    private static final String LOADFILE = "data.csv";
    private static final String WORKDIR  = "ccrypt";
    private static String FULL_PATH_DATA;
    private static String FULL_PATH_WORKDIR;
    private static String encryptionPassword;

    // App wide access to appData.
    public static PasswordData appData;

    // Intialization function;
    public static boolean Init(){

        appData = new PasswordData();
        encryptionPassword = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //System.err.println("Android version 11 or higher");
            FULL_PATH_DATA = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        }
        else {
            //System.err.println("Android version lower than 11");
            FULL_PATH_DATA = Environment.getExternalStorageDirectory().toString();
        }

        FULL_PATH_WORKDIR = FULL_PATH_DATA + "/" + WORKDIR;
        FULL_PATH_DATA = FULL_PATH_WORKDIR + "/" + DATAFILE;

        File dir = new File(FULL_PATH_WORKDIR);
        File file = new File(FULL_PATH_DATA);

        return (dir.exists() && file.exists());
    }

    /**
     * This Function is not part of the application. It was just left here in case I ever need it again.
     */
    public static void AttemptDirCreate(){
        String dirname = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/this_is_a_test";
        String filename =  dirname + "/test_file.dat";
        File dir = new File(dirname);

        System.err.println("Attempting to create directory: '" + dirname + "'");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()){
                System.err.println("It IS external storage manager");
            }
            else {
                System.err.println("It's NOT external storage manager");
            }
        }
        else {
            System.err.println("Build version lower than android 11");
        }

        if (!dir.exists()) {
            try {
                dir.mkdirs();
            }
            catch (Exception e) {
                System.err.println("ERROR. Could not make dir. Reason: " + e.getMessage());
            }
        }

        if (dir.exists()){
            System.err.println("Dir Exists or has been created");
        }
        else {
            System.err.println("Was unable to create dir");
            return;
        }

        File testfile = new File(filename);
        if (testfile.exists()){
            System.err.println("File already exists at " + filename);
            return;
        }

        // WE have the directory no we try to crate a file
        try {
            FileWriter fwriter = new FileWriter(filename);
            fwriter.write("This is a sample text");
            fwriter.close();
        }
        catch (Exception e){
            System.err.println("Exception while writing the file: " + e);
            return;
        }

        System.err.println("Successfully created a text file at filename");


    }

    public static String getDataCSVPath(){
        return FULL_PATH_WORKDIR + "/" + LOADFILE;
    }

    public static AESEngine.AESReturn encrypt(String password){
        if (!password.isEmpty()){
            encryptionPassword = password;
        }

        AESEngine aesEngine = new AESEngine();
        AESEngine.AESReturn aer = aesEngine.getEmptyReturn();

        File dir = new File(FULL_PATH_WORKDIR);
        if (!dir.exists()) {
            try {
                dir.mkdir();
            } catch (SecurityException se) {
                aer.retCode = AESEngine.AES_CANNOT_MAKE_DIR;
                aer.lastError = se.getMessage();
                aer.data = "";
            }
        }

        if (aer.retCode != AESEngine.AES_OK){
            return aer;
        }

        aer = aesEngine.initEngine(encryptionPassword);
        if (aer.retCode != AESEngine.AES_OK){
            return aer;
        }

        aer = aesEngine.encrypt(appData.getRawData(),FULL_PATH_DATA);
        return aer;

    }

    public static AESEngine.AESReturn decrypt(String password){

        encryptionPassword = password;

        AESEngine aesEngine = new AESEngine();

        AESEngine.AESReturn aer = aesEngine.initEngine(encryptionPassword);
        if (aer.retCode != AESEngine.AES_OK){
            return aer;
        }

        aer = aesEngine.decrypt(FULL_PATH_DATA);
        return aer;

    }

    // Function that checks if permissions have been granted.
    public static ReqPermReturn getRequiredPermissions(Activity activity){

        int code = REQUEST_NOTHING;
        if (PermissionChecker.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED) {
            code = Aux.REQUEST_CODE_ASK_READ;
        }

        if (PermissionChecker.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED){
            if (code != 0) code = Aux.REQUEST_CODE_ASK_READ_WRITE;
            else code = Aux.REQUEST_CODE_ASK_WRITE;
        }

        // Checking what permissions are required.
        String permissions[] = null;
        switch (code) {
            case Aux.REQUEST_CODE_ASK_READ:
                permissions = new String[1];
                permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
                break;
            case Aux.REQUEST_CODE_ASK_WRITE:
                permissions = new String[1];
                permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
            case Aux.REQUEST_CODE_ASK_READ_WRITE:
                permissions = new String[2];
                permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
                permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
        }

        ReqPermReturn rpr = new ReqPermReturn();
        rpr.permissions = permissions;
        rpr.code = code;
        return rpr;
    }


    // Function that checks permission results.
    public static int requestPermissionResult(int requestCode, int[] grantResults){
        switch (requestCode) {
            case Aux.REQUEST_CODE_ASK_READ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    return REQPERM_RESULT_OK;
                } else {
                    // Permission Denied
                    return REQPERM_RESULT_READ;
                }
            case Aux.REQUEST_CODE_ASK_WRITE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    return REQPERM_RESULT_OK;
                } else {
                    // Permission Denied
                    return REQPERM_RESULT_READ;
                }
            case Aux.REQUEST_CODE_ASK_READ_WRITE:
                int code = REQPERM_RESULT_OK;
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) code = REQPERM_RESULT_READ;
                if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    if (code != REQPERM_RESULT_OK) code = REQPERM_RESULT_WRITE;
                    else code = REQPERM_RESULT_RW;
                }
                return code;
            default: return REQPERM_RESULT_UNKNOWN;
        }
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

}
