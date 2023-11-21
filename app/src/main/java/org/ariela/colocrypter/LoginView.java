package org.ariela.colocrypter;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

        //Aux.AttemptDirCreate(this);
        boolean doFileAndDirExist = Aux.Init();

        Aux.TestOpenFile();

        // This is necessary in order to be able to open the file if it was replaced
        // or recreate it if it was deleted. The file needs to be registered (if it exists o not)
        // With the media database. If android version is equal to or greater than 11.
        Aux.PrepareCryptFile(this);

        // Checking if all files exist. Other wise this is the first time.
        if (!doFileAndDirExist){
            // Call the change password activity.
            Intent intent = new Intent(this,ChangePasswordView.class);
            intent.putExtra(Aux.INTENT_FIRST_TIME,true);
            startActivity(intent);
        }

        Aux.ReqPermReturn rpr = Aux.getRequiredPermissions(this);

        if (rpr.code != Aux.REQUEST_NOTHING){
            // Permissions are required.
            loginButton.setEnabled(false);
            // Ask for permissions
            askPermissions(rpr);
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

//        Aux.DbugCcrypt("Asking for permissions");
//        Aux.DbugCcrypt("   CODE: " + Integer.toString(rpr.code));
//        Aux.DbugCcrypt("   Permissions: ");
//        for (int i = 0; i < rpr.permissions.length; i++){
//            Aux.DbugCcrypt("      " + rpr.permissions[i]);
//        }

        requestPermissions(rpr.permissions,rpr.code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

//        Aux.DbugCcrypt("Request Permission Results. Before Super");
//        Aux.DbugCcrypt("   CODE: " + Integer.toString(requestCode));
//        Aux.DbugCcrypt("   Permissions: ");
//        for (int i = 0; i < permissions.length; i++){
//            Aux.DbugCcrypt("      " + permissions[i] + ". Grant Result: " + Integer.toString(grantResults[i]));
//        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        Aux.DbugCcrypt("Request Permission Results. After Super");
//        Aux.DbugCcrypt("   CODE: " + Integer.toString(requestCode));
//        Aux.DbugCcrypt("   Permissions: ");
//        for (int i = 0; i < permissions.length; i++){
//            Aux.DbugCcrypt("      " + permissions[i] + ". Grant Result: " + Integer.toString(grantResults[i]));
//        }


        int code = Aux.requestPermissionResult(requestCode, grantResults);
        if (code == Aux.REQPERM_RESULT_OK) {
            loginButton.setEnabled(true);
        }
        else {
            String title = getResources().getString(R.string.status_permission_dialog_title);
            String msg = getResources().getString(R.string.status_permission_required);
            Aux.showProblemDialog(this, title, msg);
        }
    }


    public void onLoginClicked(View view){
        String password = login.getText().toString();

        AESEngine.AESReturn aer = Aux.decrypt(password);
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
