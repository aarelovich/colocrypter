package org.ariela.colocrypter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

import java.util.List;

public class ImportView extends BaseClass {

    private Button btnImport;

    private ActivityResultLauncher<Intent> launchActivityForSelectingCSV = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    processCSV(result.getResultCode(),data);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        btnImport = findViewById(R.id.btnImport);

//        Aux.ReqPermReturn rpr = Aux.getRequiredPermissions(this);
//        if (rpr.code != Aux.REQUEST_NOTHING){
//            // Permissions are required.
//            btnImport.setEnabled(false);
//            // Ask for permissions
//            askPermissions(rpr);
//        }

    }

//    @TargetApi(23)
//    void askPermissions(Aux.ReqPermReturn rpr){
//        requestPermissions(rpr.permissions,rpr.code);
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        int code = Aux.requestPermissionResult(requestCode, grantResults);
//        if (code == Aux.REQPERM_RESULT_OK) {
//            btnImport.setEnabled(true);
//        } else {
//            String title = getResources().getString(R.string.status_permission_dialog_title);
//            String msg = getResources().getString(R.string.status_permission_required);
//            Aux.showProblemDialog(this, title, msg);
//        }
//    }

    public void importCSV(View view){
        // We need to select the CSV
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        launchActivityForSelectingCSV.launch(intent);
    }

    public void processCSV(int resultCode, Intent data){

        if (resultCode != RESULT_OK){
            Aux.showProblemDialog(this,"ACTIVITY RES ERROR","The activity result code was not ok, but: " + Integer.toString(resultCode));
            return;
        }

        Uri fileURI = data.getData();

        Aux.DbugCcrypt("Got the FILE URI: " + fileURI.getPath());
        DocumentFile dfile = DocumentFile.fromSingleUri(this,fileURI);
        Aux.DbugCcrypt("File exists: " + dfile.exists());
        Aux.DbugCcrypt("File can read: " + dfile.canRead());
        Aux.DbugCcrypt("File can write: " + dfile.canWrite());

        CSVReader reader = new CSVReader();
        List<List<String>> readData = reader.loadCSV(fileURI,this);

        if (reader.getStatus() == CSVReader.CSV_COULD_NOT_READ_FILE){
            String title = getResources().getString(R.string.app_name);
            String message = getResources().getString(R.string.import_error_file) + reader.getLastError();
            Aux.showProblemDialog(this,title,message);
            return;
        }

        if (reader.getStatus() == CSVReader.CSV_BAD_CSV_FORMAT){
            String title = getResources().getString(R.string.app_name);
            String message = getResources().getString(R.string.import_error_format);
            Aux.showProblemDialog(this,title,message);
            return;
        }

        // All is good.
        int counterGood = 0;
        for (int i = 0; i < readData.size(); i++){
            // Making sure there are exactly three fields.
            if (readData.get(i).size() >= 3){
                Entry e = new Entry();
                e.userName = readData.get(i).get(1);
                e.passWord = readData.get(i).get(2);

                if (readData.get(i).size() > 3){
                    e.notes = readData.get(i).get(3);
                }
                else e.notes = "";

                // Making sure that either the user name or the password dont contain reserved characters.
                // And that the entry does not exist.
                if ( Aux.validateString(e.userName) && Aux.validateString(e.passWord) && Aux.validateString(e.notes)) {
                    String entry = readData.get(i).get(0);
                    if (!Aux.appData.exists(entry)) {
                        counterGood++;
                        Aux.appData.saveEntry(entry, e);
                    }
                }
            }
        }

        // Saving the data to file.
        AESEngine.AESReturn r = Aux.encrypt("",this);
        if (r.retCode != AESEngine.AES_OK) {
            String title = getResources().getString(R.string.status_enc_error_title);
            String msg = getResources().getString(R.string.status_enc_error_message);
            msg = msg + "\n" + Integer.toString(r.retCode) + ": " + r.lastError;
            Aux.showProblemDialog(this, title, msg);
        }
        Intent intent = new Intent(this,ListView.class);
        String msg = getResources().getString(R.string.import_error_import_report_1);
        msg = msg + Integer.toString(counterGood) + " " + getResources().getString(R.string.import_error_import_report_2) + " ";
        msg = msg + Integer.toString(readData.size());
        intent.putExtra(Aux.INTENT_FROM_IMPORT_MESSAGE,msg);
        startActivity(intent);


    }

}
