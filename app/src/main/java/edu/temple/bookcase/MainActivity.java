package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements  BookListFragment.OnFragmentInteractionListener{

    private final String BOOKLIST_FRAG = "book_list_frag";
    private final String BOOKDETAILS_FRAG = "book_details_frag";


    String [] bookList;
    ViewPager viewPager;
    BookDetailsFragment bookDetailsFragment;

    FragmentManager fm = getSupportFragmentManager();
    static ArrayList<BookDetailsFragment> books = new ArrayList<BookDetailsFragment>();

    @Override
    public void onFragmentInteraction(int index) {
        ((BookDetailsFragment) fm.findFragmentByTag(BOOKDETAILS_FRAG)).displayTitle(bookList[index]);
    }

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
        if(getResources().getBoolean(R.bool.forceLandscape))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        bookList = getResources().getStringArray(R.array.books);
        if(findViewById(R.id.pane1) != null)
            landscape();
        else
            nonLandscape();
    }

    private void nonLandscape(){

        for(String book : bookList){
            books.add(BookDetailsFragment.newInstance(book));
        }
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new BookAdapter(getSupportFragmentManager()));
    }

    private void landscape(){
        if(getSupportFragmentManager().findFragmentByTag(BOOKDETAILS_FRAG) == null)
            fm.beginTransaction().add(R.id.pane2, BookDetailsFragment.newInstance(""), BOOKDETAILS_FRAG).commit();
        if(getSupportFragmentManager().findFragmentByTag(BOOKLIST_FRAG) == null)
            fm.beginTransaction().add(R.id.pane1, BookListFragment.newInstance(bookList), BOOKLIST_FRAG).commit();
    }
}
