package edu.temple.bookcase;

import android.content.Context;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    ArrayList<Book> content;
    Context context;

    public ListViewAdapter(Context context, ArrayList<Book> content){
        this.context = context;
        this.content = content;
    }
    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public Object getItem(int i) {
        return content.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView = new TextView(context);
        Book book = (Book) getItem(i);
        textView.setText(((Book) getItem(i)).getTitle());
        textView.setTextSize(25);
        return textView;
    }
}
