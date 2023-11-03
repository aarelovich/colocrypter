package com.colocrypter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ariela on 12/30/17.
 */

// List adapter class to show entries in the program.

public class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.EntryViewHolder> {

    public interface IEntryClicked{
        void entryClicked(String entryName);
    }

    private List<String> filteredEntries;

    // The View Holder class contains the handles to access the fields in the views of each row element.
    public class EntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView entryName, iconText, passWord, userName;
        public String entryIDName;

        public EntryViewHolder(View view) {
            super(view);
            entryName = (TextView) view.findViewById(R.id.tvEntryName);
            iconText = (TextView) view.findViewById(R.id.tvLetter);
            passWord = (TextView) view.findViewById(R.id.tvDaysSinceChange);
            userName = (TextView) view.findViewById(R.id.tvDescription);
            entryIDName = "";
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickInterface.entryClicked(entryIDName);
        }
    }

    // The interface for the list.
    private IEntryClicked clickInterface;

    public EntryListAdapter(IEntryClicked entryClicked){
        clickInterface = entryClicked;
        filteredEntries = Aux.appData.getFilteredEntries("");
    }

    public void setFilter(String filter){
        filteredEntries = Aux.appData.getFilteredEntries(filter);
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Creates the view, that is the row.
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_row_layout, parent, false);
        return new EntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(EntryViewHolder holder, int position) {
        if (filteredEntries.size() <= position) return;
        if (position < 0) return;
        holder.entryIDName = filteredEntries.get(position);
        Entry e = Aux.appData.getEntry(holder.entryIDName);
        holder.entryName.setText(Aux.adjustStringForShow(holder.entryIDName,Aux.MAX_CHARS_ENTRY));
        holder.userName.setText(Aux.adjustStringForShow(e.userName,Aux.MAX_CHARS_USER));
        holder.iconText.setText(holder.entryIDName.substring(0,1).toUpperCase());
        holder.passWord.setText(Aux.adjustStringForShow(e.passWord,Aux.MAX_CHARS_PASSWORD));
    }

    @Override
    public int getItemCount() {
        return filteredEntries.size();
    }



}
