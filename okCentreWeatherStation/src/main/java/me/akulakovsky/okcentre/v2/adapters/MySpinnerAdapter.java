package me.akulakovsky.okcentre.v2.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MySpinnerAdapter extends ArrayAdapter<String> {

    public MySpinnerAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        if (position == getCount()) {
            ((TextView)v.findViewById(android.R.id.text1)).setText("");
            ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
        } else {
            ((TextView)v.findViewById(android.R.id.text1)).setText(getItem(position));
        }

        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        if (position == getCount()) {
            ((TextView)v.findViewById(android.R.id.text1)).setText("");
            ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
        } else {
            ((TextView)v.findViewById(android.R.id.text1)).setText(getItem(position));
        }

        return v;
    }

    @Override
    public int getCount() {
        return super.getCount()-1; // you dont display last item. It is used as hint.
    }
}