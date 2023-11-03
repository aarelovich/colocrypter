package com.colocrypter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by ariela on 2/7/18.
 */

public class ShowDialog extends DialogFragment implements View.OnClickListener {

    public String entryName;
    public String passWord;
    public String userName;
    public String notes;

    public interface ShowDialogInterface {
        void editClicked(String entryName);
    }

    private ShowDialogInterface sdInterface;

    public void setInterface(ShowDialogInterface sdi){
        sdInterface = sdi;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflating the layout.
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_info_layout,null);
        builder.setView(dialogView);

        // Adding the data.
        TextView pass = dialogView.findViewById(R.id.tvShowPassword);
        TextView title = dialogView.findViewById(R.id.tvShowEntryName);
        TextView user = dialogView.findViewById(R.id.tvShowUserName);
        TextView noteview = dialogView.findViewById(R.id.tvShowNotes);
        Button edit = dialogView.findViewById(R.id.btnDialogEdit);
        edit.setOnClickListener(this);

        title.setText(entryName);
        pass.setText(passWord);
        user.setText(userName);
        noteview.setText(notes);

        return builder.create();

    }


    @Override
    public void onClick(View view) {
        this.dismiss();
        sdInterface.editClicked(entryName);
    }
}
