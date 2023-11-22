package org.ariela.colocrypter;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
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

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    private ActivityResultLauncher<Intent> launchActivityForSelectingFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                   Intent data = result.getData();
                   onFileAccessRequestResult(Aux.ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_ONLY,result.getResultCode(),data);
                }
            });

    private ActivityResultLauncher<Intent> launchActivityForCreatingFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    onFileAccessRequestResult(Aux.ACTIVITY_REQUEST_CODE_SELECT_CCRYPT_NO_FILE,result.getResultCode(),data);
                }
            });

    private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true /* enabled by default */) {
        @Override
        public void handleOnBackPressed() {
            backButtonPressed();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Registering the event
        this.getOnBackPressedDispatcher().addCallback(onBackPressedCallback);

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

        AppFileInitialization();

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

    void AppFileInitialization(){
        int ret_code =  Aux.Init(this);
        if (ret_code == Aux.INIT_FILE_DOES_NOT_EXIST){
            // In This case we need to see if the users wants to load an existing file or they want to create a new one.
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getResources().getString(R.string.select_backup_or_new_title));
            alertDialog.setMessage(getResources().getString(R.string.select_backup_or_new_body));
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.select_backup_or_new_choose_new),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            requestAccessToNewFile();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.select_backup_or_new_choose_restore),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            requestAccessToExistingFile();
                        }
                    });
            alertDialog.show();

        }
        else if (ret_code == Aux.INIT_FILE_EXISTS_CANTREAD){
            // Request permission but without creating file.
            requestAccessToExistingFile();
            Aux.DbugCcrypt("Would request permissions right here");
        }
        else {
            Aux.DbugCcrypt("App File initialization needs to do nothing. File exists and is modifiable.");
        }
    }

    protected void requestAccessToExistingFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        launchActivityForSelectingFile.launch(intent);
    }

    protected void requestAccessToNewFile(){
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, Aux.DATAFILE);
        launchActivityForCreatingFile.launch(intent);
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
    public void backButtonPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
