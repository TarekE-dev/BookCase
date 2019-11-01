package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import java.util.ArrayList;

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
        bookList = getResources().getStringArray(R.array.books);
        if(findViewById(R.id.bookList) != null){
            addBookDetailsFragment();
            addBookListFragment();
        } else {
            for(String book: bookList){
                books.add(BookDetailsFragment.newInstance(book));
            }
            ((ViewPager) findViewById(R.id.viewpager)).setAdapter(new BookAdapter(getSupportFragmentManager()));
        }
    }

    private void addBookDetailsFragment(){
        if(getSupportFragmentManager().findFragmentByTag(BOOKDETAILS_FRAG) == null)
            fm.beginTransaction().add(R.id.bookDetail, BookDetailsFragment.newInstance(""), BOOKDETAILS_FRAG).commit();
    }

    private void addBookListFragment(){
        if(getSupportFragmentManager().findFragmentByTag(BOOKLIST_FRAG) == null)
            fm.beginTransaction().add(R.id.bookList, BookListFragment.newInstance(bookList), BOOKLIST_FRAG).commit();
    }

}
