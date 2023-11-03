package com.colocrypter;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class ListView extends BaseClass implements EntryListAdapter.IEntryClicked, ShowDialog.ShowDialogInterface, ConfigurationDialog.SettingsDialogInterface {
//public class ListView extends AppCompatActivity implements EntryListAdapter.IEntryClicked {

    // The adapter for the list view.
    private EntryListAdapter listAdapter;

    // The actual view where the rows will be added.
    private RecyclerView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entryClicked("");
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // Filling the list.
        listView = (RecyclerView) findViewById(R.id.rvEntryList);
        listView.addItemDecoration(new ListDivider(this,R.drawable.divider));

        // The entry list adapter has access to the data.
        listAdapter = new EntryListAdapter(this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        Intent intent = getIntent();
        String msg = intent.getStringExtra(Aux.INTENT_FROM_IMPORT_MESSAGE);
        if (msg != null) {
            if (!msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        }

    }

    // Disabling the back button.
    @Override
    public void onBackPressed() {
    }

    // Actions on App Bar Button pressed.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;

        if (item.getItemId() == R.id.miChangePass){
            intent = new Intent(this,ChangePasswordView.class);
            intent.putExtra(Aux.INTENT_FIRST_TIME,false);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.miLogout){
            logOut();
            return true;
        }
        else if (item.getItemId() == R.id.miImportData){
            intent = new Intent(this,ImportView.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.miBackup){
            intent = new Intent(this,BackupInstructionsView.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.miSettings){
            ConfigurationDialog diag = new ConfigurationDialog();
            diag.setInterface(this);
            diag.show(getFragmentManager(), "Settings");
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }

    }

    // Adding the buttons to the app bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_view,menu);

        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();

        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Nothing to do here.
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                listAdapter.setFilter(query);
                listAdapter.notifyDataSetChanged();
                return true;
            }
        });

        MenuItem miSearch = menu.findItem(R.id.menu_search);
        miSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                listAdapter.setFilter("");
                listAdapter.notifyDataSetChanged();
                return true;
            }
        });

        return true;
    }

    @Override
    public void entryClicked(String entryName) {
        if (entryName.isEmpty()) {
            Intent intent = new Intent(this,EditEntryView.class);
            intent.putExtra(Aux.INTENT_ENTRY_TO_EDIT,entryName);
            startActivity(intent);
        }
        else {
            ShowDialog dialog = new ShowDialog();
            dialog.setInterface(this);
            dialog.entryName = entryName;
            Entry entry = Aux.appData.getEntry(entryName);
            dialog.userName = entry.userName;
            dialog.passWord = entry.passWord;
            dialog.notes    = entry.notes;
            dialog.show(getFragmentManager(), "ShowDialog");
        }
    }

    @Override
    public void onDestroy(){
        // Erasing the data from memory.
        Aux.appData.clear();
        super.onDestroy();
    }

    @Override
    public void editClicked(String entryName) {
        Intent intent = new Intent(this,EditEntryView.class);
        intent.putExtra(Aux.INTENT_ENTRY_TO_EDIT,entryName);
        startActivity(intent);
    }


    @Override
    public void saveChanges() {
        // Saves the changes to the configuration.
        AESEngine.AESReturn r = Aux.encrypt("");
        if (r.retCode != AESEngine.AES_OK) {
            String title = getResources().getString(R.string.status_enc_error_title);
            String msg = getResources().getString(R.string.status_enc_error_message);
            msg = msg + "\n" + Integer.toString(r.retCode) + ": " + r.lastError;
            Aux.showProblemDialog(this, title, msg);
        }
        // Sets the new timer value
        setLogOutTime(Aux.appData.getConfigurationInteger(Aux.CONFIG_LOGOUT_TIME));
    }
}
