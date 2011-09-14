package com.cog.lab;

import java.util.ArrayList;

import com.cog.lab.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class OrderAdapter extends ArrayAdapter<Order> {

    private ArrayList<Order> items;

    public OrderAdapter(Context context, int textViewResourceId, ArrayList<Order> items) {
            super(context, textViewResourceId, items);
            this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row, null);
            }
            Order o = items.get(position);
            if (o != null) {
                    TextView subjectText = (TextView) v.findViewById(R.id.toptext);
                    
                    TextView messageText = (TextView) v.findViewById(R.id.bottomtext);
                    if (subjectText != null) {
                    	subjectText.setText(o.Subject);   
                    	subjectText.setTextColor(android.graphics.Color.WHITE);
                    	
                    }
                    if(messageText != null){
                    	messageText.setText(o.Message);
                    	messageText.setTextColor(android.graphics.Color.WHITE);
                    }
            }
            return v;
    }
}