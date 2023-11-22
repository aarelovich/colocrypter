package org.ariela.colocrypter;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.documentfile.provider.DocumentFile;

public class BackupInstructionsView extends BaseClass {

    private static final String LOCATION_PLACEHOLDER = "__LOCATION__";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_instructions);

        // We need to change the location place holder with the location of the file.
        TextView tvInstructions = (TextView) findViewById(R.id.tvInstructions);
        String content = getResources().getString(R.string.backup_instructions);

        String location = "ERROR";
        String locationURIString = Aux.getStringInSharedPreferences(this,Aux.SHARED_PREF_STRING_KEY);
        if (locationURIString != ""){
            Uri fileURI = Uri.parse(locationURIString);
            location = fileURI.getPath();
            final String[] split = location.split(":");//split the path.
            if (split.length > 1){
                location = split[1];//assign it to a string(your choice).
            }
        }

        content = content.replaceAll(LOCATION_PLACEHOLDER,location);

        tvInstructions.setText(content);

    }

}
