package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import edu.temple.audiobookplayer.AudiobookService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookListFragmentCommunicator, BookDetailsFragment.BookDetailsFragmentCommunicator {

    private final String BOOKLIST_FRAG = "book_list_frag";
    private final String BOOKDETAILS_FRAG = "book_details_frag";
    private final String VIEWPAGER_FRAG = "view_pager_frag";
    private final String BOOK_LIST = "book_list";
    private final String BOOK_PLAYING = "book_playing";

    private Book currentBook;
    ComponentName audiobookService;
    private static String TITLE = "BookCase";
    private String bookCasePath;

    Button searchButton;
    EditText searchText;

    Button pauseButton;
    Button stopButton;
    ImageView fileButton;
    SeekBar seekBar;

    BookDetailsFragment bookDetailsFragment;
    BookListFragment bookListFragment;
    ViewPagerFragment viewPagerFragment;

    Gson gson = new Gson();

    Handler seekBarHandler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(@NonNull Message message){
            if(message.obj != null){
                seekBar.setProgress(((AudiobookService.BookProgress) message.obj).getProgress());
                currentBook.setBookPos(((AudiobookService.BookProgress) message.obj).getProgress());
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
    SharedPreferences SP;


    @Override
    public void onBookClicked(int index) {
        ((BookDetailsFragment) fm.findFragmentByTag(BOOKDETAILS_FRAG)).displayBook(bookList.get(index));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SP = getPreferences(this.MODE_PRIVATE);

        bookCasePath = getExternalFilesDir(null).getAbsolutePath() + File.separator;
        getPreferences(this.MODE_PRIVATE);

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
        bookList = getSavedBookList();
        if (bookList == null)
            getJsonResponse(getResources().getString(R.string.BookAPI));
        else
            displayFragments();

        currentBook = getSavedBook();

        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBook != null) {
                    if(!((AudiobookService.MediaControlBinder) service).isPlaying())
                        TITLE = "Now Playing: " + currentBook.getTitle();
                    else
                        TITLE = "On Standby: " + currentBook.getTitle();
                    setTitle(TITLE);
                }
                ((AudiobookService.MediaControlBinder) service).pause();
                saveBookPlaying(currentBook);
            }
        });

        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AudiobookService.MediaControlBinder) service).stop();
                currentBook.setBookPos(0);
                seekBar.setProgress(currentBook.getBookPos());
                TITLE = "BookCase";
                setTitle(TITLE);
                currentBook = null;
            }
        });

        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(currentBook != null ? currentBook.getDuration() : 100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(currentBook != null) {
                    ((AudiobookService.MediaControlBinder) service).seekTo(seekBar.getProgress());
                    currentBook.setBookPos(seekBar.getProgress());
                }
            }
        });

        Intent serviceIntent = new Intent(this, AudiobookService.class);
        if (audiobookService == null)
            audiobookService = startService(serviceIntent);
        if (service == null) {
            bindService(serviceIntent, mServerConn, this.BIND_AUTO_CREATE);
        }
    }


    private String bookToJson(Book book) {
        return gson.toJson(book);
    }

    private Book bookFromJson(String json){
        return gson.fromJson(json, Book.class);
    }

    private String bookArrayListToJson(ArrayList<Book> bookList){
        Type bookArrayList = new TypeToken<ArrayList<Book>>(){}.getType();
        return gson.toJson(bookList, bookArrayList);
    }

    private ArrayList<Book> bookArrayListFromJson(String json){
        Type bookArrayList = new TypeToken<ArrayList<Book>>(){}.getType();
        return gson.fromJson(json, bookArrayList);
    }

    private void saveBookPlaying(Book book){
        String bookAsJson = bookToJson(book);
        SharedPreferences.Editor editor = SP.edit();
        editor.putString(BOOK_PLAYING, bookAsJson);
        editor.commit();
    }

    private Book getSavedBook(){
        String retrieved = SP.getString(BOOK_PLAYING, null);
        if(retrieved == null)
            return null;
        Book savedBook = bookFromJson(retrieved);
        return savedBook;
    }

    private void saveBookList(ArrayList<Book> bookList){
        String bookListJson = bookArrayListToJson(bookList);
        SharedPreferences.Editor editor = SP.edit();
        editor.putString(BOOK_LIST, bookListJson);
        editor.commit();
    }

    private ArrayList<Book> getSavedBookList(){
        String bookListJson = SP.getString(BOOK_LIST, null);
        if(bookListJson == null)
            return null;
        ArrayList<Book> bookList = bookArrayListFromJson(bookListJson);
        return bookList;
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
        Book toView = currentBook;
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
        saveBookList(bookList);
    }

    private String downloadBook(final Book book){
        final Context mainContext = this;
        final String url = getResources().getString(R.string.BookDownloadAPI) + book.getId();
        final String[] filepath = {""};
        new Thread(new Runnable() {
            @Override
            public void run() {
                String response;
                URL bookAPI = null;
                try {
                    bookAPI = new URL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                URLConnection connection = null;
                BufferedReader in = null;
                try {
                    connection = bookAPI.openConnection();
                } catch (IOException e){
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
                File bookDir = new File(mainContext.getFilesDir(), "books");
                if(!bookDir.exists()){
                    bookDir.mkdir();
                }
                try {
                    File ofile = new File(bookDir, "book-" + String.valueOf(book.getId()));
                    filepath[0] = ofile.getAbsolutePath();
                    FileWriter fw = new FileWriter(ofile);
                    fw.append(response);
                    fw.flush();
                    fw.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
        System.out.println(filepath[0]);
        return filepath[0];
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
    public void onPlayButtonPressed(Book book) {
        currentBook = book;
        TITLE = "Now Playing: " + book.getTitle();
        setTitle(TITLE);
        if(!book.equals(currentBook)){
            ((AudiobookService.MediaControlBinder) service).stop();
            currentBook.setBookPos(0);
        }
        System.out.println(book.getFilePath());
        if(book.getFilePath() == null)
            ((AudiobookService.MediaControlBinder) service).play(currentBook.getId());
        else
            ((AudiobookService.MediaControlBinder) service).play(new File(book.getFilePath()), book.getBookPos());
        seekBar.setMax(currentBook.getDuration());
        saveBookPlaying(currentBook);
    }

    @Override
    public void onDownloadButtonPressed(Book book) {
        String filepath = downloadBook(book);
        System.out.println(filepath);
        for(int i=0; i < bookList.size(); i++){
            if(bookList.get(i).equals(book)){
                bookList.get(i).setFilePath(filepath);
            }
        }
        saveBookList(bookList);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        outState.putParcelableArrayList(BOOK_LIST, bookList);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(mServerConn);
    }
}
