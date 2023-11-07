package org.ariela.colocrypter;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordView extends BaseClass {

    private Button btnSetLoginPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btnSetLoginPass = (Button) findViewById(R.id.btnSetLoginPass);

        Aux.ReqPermReturn rpr = Aux.getRequiredPermissions(this);
        if (rpr.code != Aux.REQUEST_NOTHING){
            // Permissions are required.
            btnSetLoginPass.setEnabled(false);
            // Ask for permissions
            askPermissions(rpr);
        }
        else{
            Intent intent = getIntent();
            if (intent.getBooleanExtra(Aux.INTENT_FIRST_TIME,false)) {
                Aux.showProblemDialog(this,
                        getResources().getString(R.string.welcome_message_title),
                        getResources().getString(R.string.welcome_message));
            }
        }
    }

    @TargetApi(23)
    void askPermissions(Aux.ReqPermReturn rpr){
        requestPermissions(rpr.permissions,rpr.code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int code = Aux.requestPermissionResult(requestCode, grantResults);
        if (code == Aux.REQPERM_RESULT_OK) {
            btnSetLoginPass.setEnabled(true);
            Intent intent = getIntent();
            if (intent.getBooleanExtra(Aux.INTENT_FIRST_TIME, false)) {
                Aux.showProblemDialog(this,
                        getResources().getString(R.string.welcome_message_title),
                        getResources().getString(R.string.welcome_message));
            }
        } else {
            String title = getResources().getString(R.string.status_permission_dialog_title);
            String msg = getResources().getString(R.string.status_permission_required);
            Aux.showProblemDialog(this, title, msg);
        }
    }

    // Called in the onClick listener for the set password button.
    public void changePassword(View view){

        EditText pass1 = (EditText) findViewById(R.id.etChangePassword);
        EditText pass2 = (EditText) findViewById(R.id.etVerifyPassword);
        String password1 = pass1.getText().toString();
        String password2 = pass2.getText().toString();

        if (password1.length() < Aux.MIN_ENC_PASSWORD_LENGTH){
            Toast.makeText(this,getResources().getString(R.string.status_pass_too_short)
                    + Integer.toString(Aux.MIN_ENC_PASSWORD_LENGTH),
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!password1.equals(password2)){
            Toast.makeText(this,getResources().getString(R.string.status_pass_not_match),Toast.LENGTH_LONG).show();
            return;
        }

        // If it got here, all is ok.
        AESEngine.AESReturn r = Aux.encrypt(password1);
        if (r.retCode != AESEngine.AES_OK){
            String title = getResources().getString(R.string.status_enc_error_title);
            String msg = getResources().getString(R.string.status_enc_error_message);
            msg = msg + "\n" + Integer.toString(r.retCode) + ": " + r.lastError;
            Aux.showProblemDialog(this,title,msg);
        }
        else{
            Aux.appData.clear();
            Intent intent = new Intent(this,LoginView.class);
            startActivity(intent);
        }

    }

    @Override
    public void onDestroy(){
        Aux.appData.clear();
        super.onDestroy();
    }



}
