package com.colocrypter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ariela on 2/19/18.
 */

public class ConfigurationDialog extends DialogFragment implements View.OnClickListener  {

    private EditText logout_time;

    public interface SettingsDialogInterface {
        void saveChanges();
    }

    private SettingsDialogInterface sdInterface;

    public void setInterface(SettingsDialogInterface sdi){
        sdInterface = sdi;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflating the layout.
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_config_layout,null);
        builder.setView(dialogView);

        // Adding the data.
        logout_time = dialogView.findViewById(R.id.etLogoutTime);
        Button ok = dialogView.findViewById(R.id.btnConfigApply);
        Button cancel = dialogView.findViewById(R.id.btnConfigCancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

        // Loading the current data values.
        int t = Aux.appData.getConfigurationInteger(Aux.CONFIG_LOGOUT_TIME);
        if (t == -1) t = Aux.DEFAULT_TIME_OUT;
        logout_time.setText(Integer.toString(t/60000));

        return builder.create();

    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnConfigApply){
            String newval = logout_time.getText().toString();
            // An empty value is treated as a cancel
            if (!newval.isEmpty()) {
                int t = Integer.valueOf(newval);
                t = t * 60000;
                Aux.appData.setConfigurationValue(Aux.CONFIG_LOGOUT_TIME, Integer.toString(t));
                sdInterface.saveChanges();
            }
        }
        this.dismiss();
    }
}
