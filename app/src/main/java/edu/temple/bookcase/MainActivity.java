package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    String [] bookList;
    static ArrayList<BookDetailsFragment> books = new ArrayList<BookDetailsFragment>();

    public static class BookAdapter extends FragmentPagerAdapter {

        public BookAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) { return books.get(position); }

        @Override
        public int getCount() {
            return books.size();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bookList = getResources().getStringArray(R.array.books);
        if(findViewById(R.id.pane1) != null){
            landscape();
        } else {
            nonLandscape();
        }
    }

    private void nonLandscape(){
        for(String book : bookList){
            books.add(BookDetailsFragment.newInstance(book));
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new BookAdapter(getSupportFragmentManager()));
    }

    private void landscape(){

    }
}
