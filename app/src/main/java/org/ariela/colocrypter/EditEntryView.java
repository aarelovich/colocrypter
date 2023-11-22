package org.ariela.colocrypter;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditEntryView extends BaseClass implements RngPasswordDialog.RngDialogInterface{

    private EditText etEntryName;
    private EditText etUserID;
    private EditText etPassword;
    private EditText etNotes;
    private Entry currentEntry;
    private String entryName;
    private String entryToReplace;
    private Button btnAdd;
    boolean permissionsOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Getting a handle on all the graphical elements
        etEntryName = findViewById(R.id.etEntryName);
        etUserID    = findViewById(R.id.etEntryUserID);
        etPassword  = findViewById(R.id.etEntryPassword);
        btnAdd      = findViewById(R.id.btnAdd);
        etNotes     = findViewById(R.id.etNotes);

        Intent intent = getIntent();
        entryName = intent.getStringExtra(Aux.INTENT_ENTRY_TO_EDIT);
        if (!entryName.isEmpty()){
            currentEntry = Aux.appData.getEntry(entryName);
            etEntryName.setText(entryName);
            etUserID.setText(currentEntry.userName);
            etPassword.setText(currentEntry.passWord);
            etNotes.setText(currentEntry.notes);
            btnAdd.setText(getResources().getString(R.string.entry_save));
        }
        else{
            currentEntry = new Entry();
            btnAdd.setText(getResources().getString(R.string.entry_add));
        }

        //Aux.ReqPermReturn rpr = Aux.getRequiredPermissions(this);
        permissionsOk = true;
//        if (rpr.code != Aux.REQUEST_NOTHING){
//            // Permissions are required.
//            btnAdd.setEnabled(false);
//            // Ask for permissions
//            askPermissions(rpr);
//        }

    }

    // Adding the buttons to the app bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_entry,menu);
        String entry = getIntent().getStringExtra(Aux.INTENT_ENTRY_TO_EDIT);
        if (entry.isEmpty()){
            MenuItem mi = menu.findItem(R.id.miDelete);
            mi.setVisible(false);
        }
        return true;
    }

//    @TargetApi(23)
//    void askPermissions(Aux.ReqPermReturn rpr){
//        requestPermissions(rpr.permissions,rpr.code);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        int code = Aux.requestPermissionResult(requestCode, grantResults);
//        if (code == Aux.REQPERM_RESULT_OK) {
//            btnAdd.setEnabled(true);
//        } else {
//            permissionsOk = false;
//            String title = getResources().getString(R.string.status_permission_dialog_title);
//            String msg = getResources().getString(R.string.status_permission_required);
//            Aux.showProblemDialog(this, title, msg);
//        }
//    }

    ////////////////////// DIALOG CLICK LISTENER FOR CONFIRMATION DIALOGS ///////////////////////////
    DialogInterface.OnClickListener modifyDialogClickLister = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    // Current entry must be saved.
                    saveAndGoBack(false);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        }
    };

    DialogInterface.OnClickListener deleteDialogClickLister = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    // Current entry must be saved.
                    saveAndGoBack(true);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        }
    };

    ///////////// BUTTON ACTIONS ///////////////////
    public void generatePassword(View view){
        System.err.println("About to run RNG Dialog");
        RngPasswordDialog diag = new RngPasswordDialog();
        diag.setInterface(this);
        if (currentEntry.passWord.length() >= Aux.MIN_GEN_PASSWORD_SIZE){
            diag.setEtPassSize(currentEntry.passWord.length());
        }
        diag.show(getFragmentManager(), "RngGenerator");
    }

    @Override
    public void newPassword(String newpass) {
        if (newpass.isEmpty()){
            Toast.makeText(this,getResources().getString(R.string.no_chars_for_pass_gen),Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            etPassword.setText(newpass);
        }
    }


    public void addEntry(View view){
        currentEntry.passWord = etPassword.getText().toString();
        currentEntry.userName = etUserID.getText().toString();
        currentEntry.notes    = etNotes.getText().toString();
        String setEntryName = etEntryName.getText().toString();

        if (setEntryName.isEmpty()){
            Toast.makeText(this,getResources().getString(R.string.empty_entry_name),Toast.LENGTH_LONG).show();
            return;
        }

        String error = "";
        if (!Aux.validateString(currentEntry.passWord)){
            error = getResources().getString(R.string.pass_word);
        }
        if (!Aux.validateString(currentEntry.userName)){
            error = getResources().getString(R.string.user_name);
        }
        if (!Aux.validateString(setEntryName)){
            error = getResources().getString(R.string.entry_name);
        }
        if (!Aux.validateString(currentEntry.notes)){
            error = getResources().getString(R.string.notes_label);
        }

        if (!error.isEmpty()){
            error = error + " " + getResources().getString(R.string.invalid_string);
            Toast.makeText(this,error,Toast.LENGTH_LONG).show();
            return;
        }

        // This prevents overwriting Another entry with this one. Its name (new or not) needs to be
        // original.
        if ( (entryName.isEmpty() && Aux.appData.exists(setEntryName)) ||
             (!entryName.equals(setEntryName) && Aux.appData.exists(setEntryName)) ){
            String msg   = getResources().getString(R.string.can_not_overwrite) + "\n" + setEntryName;
            Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
            return;
        }


        if (!entryName.isEmpty()){
            // Over write warning.
            String msg;
            msg = getResources().getString(R.string.modify_msg_pt1) + "\n" + entryName + "\n" +
                    getResources().getString(R.string.mod_del_msg_pt2);
            entryToReplace = entryName;
            entryName = setEntryName;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(msg);
            builder.setPositiveButton(getResources().getString(R.string.diag_yes), modifyDialogClickLister);
            builder.setNegativeButton(getResources().getString(R.string.diag_no), modifyDialogClickLister);
            builder.show();
        }
        else{
            entryToReplace = "";
            entryName = setEntryName;
            saveAndGoBack(false);
        }
    }

    private void saveAndGoBack(boolean delete){
        if (!delete) {
            Aux.appData.replaceEntry(entryToReplace, entryName, currentEntry);
        }
        else{
            Aux.appData.deleteEntry(entryName);
        }
        AESEngine.AESReturn r = Aux.encrypt("",this);
        if (r.retCode != AESEngine.AES_OK) {
            String title = getResources().getString(R.string.status_enc_error_title);
            String msg = getResources().getString(R.string.status_enc_error_message);
            msg = msg + "\n" + Integer.toString(r.retCode) + ": " + r.lastError;
            Aux.showProblemDialog(this, title, msg);
        }
        Intent intent = new Intent(this,ListView.class);
        startActivity(intent);
    }

    // Actions on App Bar Button pressed.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.miDelete){
            if (!permissionsOk) return true;
            String msg;
            msg = getResources().getString(R.string.delete_msg_pt1) + "\n" + entryName + "\n" +
                    getResources().getString(R.string.mod_del_msg_pt2);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(msg);
            builder.setPositiveButton(getResources().getString(R.string.diag_yes), deleteDialogClickLister);
            builder.setNegativeButton(getResources().getString(R.string.diag_no), deleteDialogClickLister);
            builder.show();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

}


