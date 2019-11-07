package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements  BookListFragment.OnFragmentInteractionListener{

    private final String BOOKLIST_FRAG = "book_list_frag";
    private final String BOOKDETAILS_FRAG = "book_details_frag";


    String[] bookList;
    ArrayList<Book> BOOKS = new ArrayList<Book>();
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

    private void generateBookList(String url) throws MalformedURLException, JSONException {
        String response = getText(url);
        JSONObject reader = new JSONObject(response);
        
    }

    private String getText(String url) throws MalformedURLException{
        final URL bookAPI = new URL(url);
        final String[] response = new String[1];
        new Thread() {
          @Override
          public void run(){
              URLConnection connection = null;
              BufferedReader in = null;
              try {
                  connection = bookAPI.openConnection();
              } catch (IOException e) {
                  e.printStackTrace();
              }
              try {
                  in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
              } catch (Exception e){}
              StringBuilder sb = new StringBuilder();
              String currentLine = null;
              while(true){
                  try {
                      currentLine = in.readLine();
                  } catch (IOException e){
                      e.printStackTrace();
                  }
                  if(currentLine == null){
                      break;
                  }
                  sb.append(currentLine);
              }
              response[0] = sb.toString();
          }
        }.start();
        return response[0];
    }

}
