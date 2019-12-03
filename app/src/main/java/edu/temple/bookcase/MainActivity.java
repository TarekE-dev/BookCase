package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import edu.temple.audiobookplayer.AudiobookService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

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

public class MainActivity extends AppCompatActivity implements BookListFragment.BookListFragmentCommunicator, BookDetailsFragment.BookDetailsFragmentCommunicator {

    private final String BOOKLIST_FRAG = "book_list_frag";
    private final String BOOKDETAILS_FRAG = "book_details_frag";
    private final String VIEWPAGER_FRAG = "view_pager_frag";
    private int BOOK_POS = 0;
    static int DURATION = 100;
    static int BOOK_ID = -1;
    static Book currentBook;
    ComponentName audiobookService;
    private static String TITLE = "BookCase";

    Button searchButton;
    EditText searchText;

    Button pauseButton;
    Button stopButton;
    SeekBar seekBar;

    BookDetailsFragment bookDetailsFragment;
    BookListFragment bookListFragment;
    ViewPagerFragment viewPagerFragment;

    Handler seekBarHandler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(@NonNull Message message){
            if(message.obj != null){
                seekBar.setProgress(((AudiobookService.BookProgress) message.obj).getProgress());
                BOOK_POS = ((AudiobookService.BookProgress) message.obj).getProgress();
            }
            return false;
        }
    });
    Handler jsonHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            String response = (String) message.obj;
            generateBookList(response);
            displayFragments();
            return false;
        }
    });

    private IBinder service;
    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = iBinder;
            ((AudiobookService.MediaControlBinder) service).setProgressHandler(seekBarHandler);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            unbindService(mServerConn);
            service = null;
        }
    };

    ArrayList<Book> bookList = null;
    FragmentManager fm = getSupportFragmentManager();

    @Override
    public void onBookClicked(int index) {
        ((BookDetailsFragment) fm.findFragmentByTag(BOOKDETAILS_FRAG)).displayBook(bookList.get(index));
        BOOK_POS = 0;
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
        setTitle(TITLE);
        bookList = getBookList();
        if (bookList == null)
            getJsonResponse(getResources().getString(R.string.BookAPI));
        else
            displayFragments();

        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBook != null) {
                    TITLE = "On Standby: " + currentBook.getTitle();
                    setTitle(TITLE);
                }
                ((AudiobookService.MediaControlBinder) service).pause();
            }
        });

        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AudiobookService.MediaControlBinder) service).stop();
                BOOK_POS = 0;
                seekBar.setProgress(BOOK_POS);
                TITLE = "BookCase";
                setTitle(TITLE);
                currentBook = null;
            }
        });

        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(DURATION);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((AudiobookService.MediaControlBinder) service).seekTo(seekBar.getProgress());
                BOOK_POS = seekBar.getProgress();
            }
        });

        Intent serviceIntent = new Intent(this, AudiobookService.class);
        if (audiobookService == null)
            audiobookService = startService(serviceIntent);
        if (service == null) {
            bindService(serviceIntent, mServerConn, this.BIND_AUTO_CREATE);
        }
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
        if(currentBook != null){
            toView = currentBook;
        }
        if(bookDetailsFragment == null) {
            bookDetailsFragment = BookDetailsFragment.newInstance(toView);
            fm.beginTransaction().add(R.id.bookDetail, bookDetailsFragment, BOOKDETAILS_FRAG).commit();
        } else{
            fm.beginTransaction().remove(bookDetailsFragment).commit();
            bookDetailsFragment = BookDetailsFragment.newInstance(toView);
            fm.beginTransaction().add(R.id.bookDetail, bookDetailsFragment, BOOKDETAILS_FRAG).commit();
        }
        System.out.println(toView);
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
            int duration = 0;
            try {
                id = obj.getInt("book_id");
                title = obj.getString("title");
                author = obj.getString("author");
                published = obj.getInt("published");
                coverURL = obj.getString("cover_url");
                duration = obj.getInt("duration");
            }catch (JSONException e){
                e.printStackTrace();
            }
            bookList.add(new Book(id, title, author, published, coverURL, duration));
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

    @Override
    public void onButtonPressed(Book book) {
        currentBook = book;
        TITLE = "Now Playing: " + book.getTitle();
        setTitle(TITLE);
        if(book.getId() != BOOK_ID){
            ((AudiobookService.MediaControlBinder) service).stop();
            BOOK_POS = 0;
        }
        ((AudiobookService.MediaControlBinder) service).play(book.getId(), BOOK_POS);
        DURATION = book.getDuration();
        seekBar.setMax(DURATION);
        BOOK_ID = book.getId();
    }
}
