package edu.temple.bookcase;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {

    String[] content;
    Context context;

    public ListViewAdapter(Context context, String[] content){
        this.context = context;
        this.content = content;
    }
    @Override
    public int getCount() {
        return content.length;
    }

    @Override
    public Object getItem(int i) {
        return content[i];
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
        textView.setText(getItem(i).toString());
        textView.setTextSize(25);
        return textView;
    }
}
