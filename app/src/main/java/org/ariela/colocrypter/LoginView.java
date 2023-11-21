package org.ariela.colocrypter;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginView extends AppCompatActivity {

    private EditText login;
    private Button loginButton;

//  Code used to ask for the permission for All File Access Request.
//    private ActivityResultLauncher<Intent> requestFileAccessActivityLauncher =
//            registerForActivityResult(new
//                            ActivityResultContracts.StartActivityForResult(),
//                    (result) -> {
//                        // The if is unnecessary because this code is not called if not true, however I could not compile without it.
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                            if (Environment.isExternalStorageManager()){
//                                // All went well.
//                                System.err.println("All went well");
//                                legacyStartUp();
//                            }
//                            else {
//                                // All did not go well.
//                                String title = getResources().getString(R.string.status_permission_dialog_title);
//                                String msg = getResources().getString(R.string.status_permission_required);
//                                Aux.showProblemDialog(this, title, msg);
//                            }
//                        }
//                    }
//            );

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    private ActivityResultLauncher<Intent> launchActivityForSelectingFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                   Intent data = result.getData();
                   onFileAccessRequestResult(Aux.ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_ONLY,Activity.RESULT_OK,data);
                }
            });

    private ActivityResultLauncher<Intent> launchActivityForCreatingFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    onFileAccessRequestResult(Aux.ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_NO_FILE,Activity.RESULT_OK,data);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        login = (EditText) findViewById(R.id.etDecryptPassword);
        loginButton = findViewById(R.id.btnLogin);

        // Adding a listener for the done button
        login.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onLoginClicked(login);
                }
                return false;
            }
        });

        Aux.ReqPermReturn rpr = Aux.getRequiredPermissions(this);

        if (rpr.code != Aux.REQUEST_NOTHING){
            // Permissions are required.
            loginButton.setEnabled(false);
            // Ask for permissions
            askPermissions(rpr);
        }
        else {
            AppFileInitialization();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        login.setText("");
    }
    @Override
    protected void onResume() {
        super.onResume();
        login.setText("");
        // For Debug. Set the password.
        // login.setText("poli2416");

    }

    void askPermissions(Aux.ReqPermReturn rpr){
        requestPermissions(rpr.permissions,rpr.code);
    }

    void AppFileInitialization(){
        int ret_code =  Aux.Init(this);
        if (ret_code == Aux.INIT_FILE_DOES_NOT_EXIST){
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, Aux.DATAFILE);
            launchActivityForCreatingFile.launch(intent);
        }
        else if (ret_code == Aux.INIT_FAILED_TO_CREATE_DIR){
            String title = getResources().getString(R.string.status_permission_dialog_title);
            String msg = getResources().getString(R.string.status_permission_required);
            Aux.showProblemDialog(this, title, msg);
        }
        else if (ret_code == Aux.INIT_NEED_PERMISSION){
            // Request permission but without creating file.
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.setType("*/*");
            //startActivityForResult(intent, Aux.ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_ONLY);
            launchActivityForSelectingFile.launch(intent);
            Aux.DbugCcrypt("Would request permissions right here");
        }
        else {
            Aux.DbugCcrypt("App File initialization needs to do nothing");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int code = Aux.requestPermissionResult(requestCode, grantResults);
        if (code == Aux.REQPERM_RESULT_OK) {
            loginButton.setEnabled(true);
            AppFileInitialization();
        }
        else {
            String title = getResources().getString(R.string.status_permission_dialog_title);
            String msg = getResources().getString(R.string.status_permission_required);
            Aux.showProblemDialog(this, title, msg);
        }
    }

    protected void onFileAccessRequestResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (resultCode == RESULT_OK){
            if ( (requestCode == Aux.ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_ONLY) || (requestCode == Aux.ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_NO_FILE) ){

                Uri dataFileUri = data.getData();

                Aux.DbugCcrypt("Storing the URI of the Work Directory In Shared Preferences: " + dataFileUri.toString());
                Aux.storeStringInSharedPreferences(this,Aux.SHARED_PREF_STRING_KEY,dataFileUri.toString());

                // And now we take persistent permission.
                ContentResolver cr = this.getContentResolver();
                cr.takePersistableUriPermission(dataFileUri, (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));

            }

            if (requestCode == Aux.ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_NO_FILE){
                // We need to start the change password view.
                Intent intent = new Intent(this,ChangePasswordView.class);
                intent.putExtra(Aux.INTENT_FIRST_TIME,true);
                startActivity(intent);
            }

        }
        else {
            // TODO Handle.
            Aux.DbugCcrypt("On Activity Result was not OK.");
        }

    }

    public void onLoginClicked(View view){
        String password = login.getText().toString();

        AESEngine.AESReturn aer = Aux.decrypt(password,this);
        if (aer.retCode != AESEngine.AES_OK){
            if (aer.retCode != AESEngine.AES_DECRYPT_ERROR) {
                String title = getResources().getString(R.string.status_enc_error_title);
                String msg = getResources().getString(R.string.status_enc_error_message);
                msg = msg + "\n" + Integer.toString(aer.retCode) + ": " + aer.lastError;
                Aux.showProblemDialog(this, title, msg);
            }
            else{
                Toast.makeText(this,getResources().getString(R.string.status_bad_password),Toast.LENGTH_SHORT).show();
            }
        }
        else{

            // Setting the data.
            if (!Aux.appData.setData(aer.data)){
                Toast.makeText(this,getResources().getString(R.string.status_file_corruped),Toast.LENGTH_SHORT).show();
                return;
            }

            // Launching the list view
            Intent intent = new Intent(this,ListView.class);
            startActivity(intent);
        }
    }

    // Back button on login should show home.
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
