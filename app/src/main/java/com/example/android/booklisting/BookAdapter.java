package com.example.android.booklisting;


import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Context context, ArrayList<Book> earth) {
        super(context, 0, earth);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        Book currentListItem = getItem(position);

        TextView authoTextView = (TextView) listItemView.findViewById(R.id.author);
        String author = currentListItem.getAuthor();
        authoTextView.setText(author);

        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title);
        String title = currentListItem.getTitle();
        titleTextView.setText(title);



        return listItemView;



    }


}
