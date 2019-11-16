package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
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
    private final String VIEWPAGER_FRAG = "view_pager_frag";

    Button searchButton;
    EditText searchText;

    BookDetailsFragment bookDetailsFragment;
    BookListFragment bookListFragment;
    ViewPagerFragment viewPagerFragment;

    Handler jsonHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            String response = (String) message.obj;
            generateBookList(response);
            displayFragments();
            return false;
        }
    });

    ArrayList<Book> bookList = null;
    FragmentManager fm = getSupportFragmentManager();

    @Override
    public void onFragmentInteraction(int index) {
        ((BookDetailsFragment) fm.findFragmentByTag(BOOKDETAILS_FRAG)).displayBook(bookList.get(index));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bookDetailsFragment = (BookDetailsFragment) fm.findFragmentByTag(BOOKDETAILS_FRAG);
        bookListFragment = (BookListFragment) fm.findFragmentByTag(BOOKLIST_FRAG);
        viewPagerFragment = (ViewPagerFragment) fm.findFragmentByTag(VIEWPAGER_FRAG);

        searchButton = findViewById(R.id.searchButton);
        searchText = findViewById(R.id.searchText);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJsonResponse(getResources().getString(R.string.BookSearchAPI) + searchText.getText());
            }
        });
        bookList = getBookList();
        if(bookList == null)
            getJsonResponse(getResources().getString(R.string.BookAPI));
        else
            displayFragments();
    }

    private ArrayList<Book> getBookList() {
        if(findViewById(R.id.bookList) != null){
            if(viewPagerFragment != null){
                return viewPagerFragment.getBooks();
            } else if(bookListFragment != null) {
                return bookListFragment.getBooks();
            }
        } else {
            if(bookListFragment != null){
                return bookListFragment.getBooks();
            } else if(viewPagerFragment != null){
                return viewPagerFragment.getBooks();
            }
        }
        return null;
    }

    private void displayFragments(){
        if(findViewById(R.id.bookList) != null){
            addBookDetailsFragment();
            addBookListFragment();
        } else {
            addViewPagerFragment();
        }
    }

    private void addViewPagerFragment(){
        if(viewPagerFragment == null) {
            viewPagerFragment = ViewPagerFragment.newInstance(bookList);
            fm.beginTransaction().add(R.id.viewPagerFragment, viewPagerFragment, VIEWPAGER_FRAG).commit();
        } else {
            fm.beginTransaction().remove(viewPagerFragment).commit();
            fm.executePendingTransactions();
            viewPagerFragment = ViewPagerFragment.newInstance(bookList);
            fm.beginTransaction()
                    .add(R.id.viewPagerFragment, viewPagerFragment, VIEWPAGER_FRAG)
                    .commit();
        }
    }

    private void addBookDetailsFragment(){
        Book toView = null;
        if(viewPagerFragment != null && viewPagerFragment.getCurrentFragment() != null)
            toView = viewPagerFragment.getCurrentFragment().getBook();
        if(bookDetailsFragment == null) {
            bookDetailsFragment = BookDetailsFragment.newInstance(toView);
            fm.beginTransaction().add(R.id.bookDetail, bookDetailsFragment, BOOKDETAILS_FRAG).commit();
        } else{
            fm.beginTransaction().remove(bookDetailsFragment).commit();
            bookDetailsFragment = BookDetailsFragment.newInstance(toView);
            fm.beginTransaction().add(R.id.bookDetail, bookDetailsFragment, BOOKDETAILS_FRAG).commit();

        }
    }

    private void addBookListFragment(){
        if(bookListFragment == null) {
            bookListFragment = BookListFragment.newInstance(bookList);
            fm.beginTransaction().add(R.id.bookList, bookListFragment, BOOKLIST_FRAG).commit();
        } else {
            fm.beginTransaction().remove(bookListFragment).commit();
            fm.executePendingTransactions();
            bookListFragment = BookListFragment.newInstance(bookList);
            fm.beginTransaction().add(R.id.bookList, bookListFragment, BOOKLIST_FRAG).commit();
        }
    }

    private void generateBookList(String response)  {
        bookList = new ArrayList<Book>();
        JSONArray reader = null;
        try {
            reader = new JSONArray(response);
        } catch (JSONException e){}
        for(int book=0; book < reader.length(); book++){
            JSONObject obj = null;
            try {
                obj = reader.getJSONObject(book);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int id = 0;
            String title = null;
            String author = null;
            int published = 0;
            String coverURL = null;
            try {
                id = obj.getInt("book_id");
                title = obj.getString("title");
                author = obj.getString("author");
                published = obj.getInt("published");
                coverURL = obj.getString("cover_url");
            }catch (JSONException e){
                e.printStackTrace();
            }
            bookList.add(new Book(id, title, author, published, coverURL));
        }
    }

    private void getJsonResponse(final String url) {
        new Thread() {
          @Override
          public void run(){
              String response;
              URL bookAPI = null;
              try {
                  bookAPI = new URL(url);
              }catch (MalformedURLException e){
                  e.printStackTrace();
              }
              URLConnection connection = null;
              BufferedReader in = null;
              try {
                  connection = bookAPI.openConnection();
              } catch (IOException e) {
                  e.printStackTrace();
              }
              try {
                  in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
              } catch (Exception e){
                  e.printStackTrace();
              }
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
              try {
                  in.close();
              } catch (IOException e){
                  e.printStackTrace();
              }
              response = sb.toString();
              Message msg = Message.obtain();
              msg.obj = response;
              jsonHandler.sendMessage(msg);
          }
        }.start();
    }
}
