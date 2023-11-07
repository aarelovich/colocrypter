package org.ariela.colocrypter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by ariela on 2/19/18.
 */

public class RngPasswordDialog extends DialogFragment implements View.OnClickListener{

    private CheckBox cbLowercase;
    private CheckBox cbNumbers;
    private CheckBox cbSymbols;
    private CheckBox cbUppercase;
    private EditText etPassSize;
    private int passSize;

    public interface RngDialogInterface {
        void newPassword(String newpass);
    }

    private RngDialogInterface rdInterface;

    public void setInterface(RngDialogInterface sdi){
        rdInterface = sdi;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflating the layout.
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.diag_rng_gen,null);
        builder.setView(dialogView);

        // Adding the data.
        etPassSize = dialogView.findViewById(R.id.etPassSize);
        cbUppercase = dialogView.findViewById(R.id.cbUseUppercase);
        cbLowercase = dialogView.findViewById(R.id.cbUseLowercase);
        cbSymbols = dialogView.findViewById(R.id.cbUseSymbols);
        cbNumbers = dialogView.findViewById(R.id.cbUseNumbers);

        // Minimum password size
        int psize = Aux.DEFAULT_GEN_PASSWORD_SIZE;
        if (passSize > psize){
            psize = passSize;
        }
        etPassSize.setText(Integer.toString(psize));

        // Adding the listeners
        Button ok = dialogView.findViewById(R.id.btnRngGenerate);
        Button cancel = dialogView.findViewById(R.id.btnRngCancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

        // Loading the current data values.
        return builder.create();

    }

    public void setEtPassSize(int psize){
        passSize = psize;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnRngGenerate){
            PasswordGenerator pgen = new PasswordGenerator();
            PasswordGenerator.Configuration config = new PasswordGenerator.Configuration();
            config.useLowercase = cbLowercase.isChecked();
            config.useNumbers   = cbNumbers.isChecked();
            config.useSymbols   = cbSymbols.isChecked();
            config.useUppercase = cbUppercase.isChecked();
            try {
                config.size = Integer.valueOf(etPassSize.getText().toString());
            }
            catch (NumberFormatException exp){
                config.size = 8;
                etPassSize.setText("8");
            }
            String newpass = "";
            if (config.anyCharsUsed()){
                newpass = pgen.generateRandomPassword(config);
            }
            rdInterface.newPassword(newpass);
        }
        this.dismiss();
    }

}
