package edu.temple.bookcase;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class BookDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String BOOK_OBJ = "BOOK_OBJ";

    // TODO: Rename and change types of parameters
    private Book bookObj;
    private boolean DOWNLOADING = false;
    private boolean DOWNLOADED = false;

    View inflatedView;
    View bookTitle;
    View bookImg;
    View bookAuthor;
    Button playButton;
    ImageView fileButton;
    ImageView deleteButton;

    Handler downloadHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            updateViews();
            return false;
        }
    });

    private BookDetailsFragmentCommunicator parentFragment;

    public BookDetailsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bookObj Parameter 1.
     * @return A new instance of fragment BookListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BookDetailsFragment newInstance(Book bookObj) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(BOOK_OBJ, bookObj);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookObj = getArguments().getParcelable(BOOK_OBJ);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflatedView =  inflater.inflate(R.layout.fragment_book_details, container, false);
        bookTitle = (TextView) inflatedView.findViewById(R.id.bookTitle);
        bookImg = (ImageView) inflatedView.findViewById(R.id.bookImg);
        bookAuthor = (TextView) inflatedView.findViewById(R.id.bookAuthor);
        playButton = inflatedView.findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) {
            if(bookObj != null) {
                parentFragment.onPlayButtonPressed(bookObj);
            }
        }});
        DOWNLOADED = bookObj != null && bookObj.getFilePath() != null ? true : false;
        fileButton = inflatedView.findViewById(R.id.fileButton);
        deleteButton = inflatedView.findViewById(R.id.deleteButton);
        updateViews();
        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DOWNLOADING) {
                    Toast.makeText((Context) parentFragment, "Wait for the current download to finish", Toast.LENGTH_SHORT).show();
                }
                else if(bookObj != null) {
                    downloadAudioBook(bookObj);
                    DOWNLOADING = true;
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bookObj != null){
                    parentFragment.onDeleteButtonPressed(bookObj);
                    DOWNLOADED = false;
                    updateViews();
                }
            }
        });

        displayBook(bookObj);
        return inflatedView;
    }

    private void updateViews() {
        DOWNLOADED = bookObj != null && bookObj.getFilePath() != null ? true : false;
        if(bookObj == null || bookObj.getAuthor() == null) {
            fileButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        } else if(DOWNLOADED) {
            fileButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            fileButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.GONE);
        }
    }

    public void displayBook(Book book){
        if(book != null && book.getTitle() != null) {
            bookObj = book;
            ((TextView) bookTitle).setText(book.getTitle());
            Picasso.with(getActivity().getApplicationContext()).load(book.getURL()).fit().into((ImageView) bookImg);
            ((TextView) bookAuthor).setText(book.getPublished() + ": " + book.getAuthor());
            inflatedView.findViewById(R.id.playButton).setVisibility(View.VISIBLE);
        } else {
            inflatedView.findViewById(R.id.playButton).setVisibility(View.GONE);
        }
        updateViews();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookDetailsFragment.BookDetailsFragmentCommunicator) {
            parentFragment = (BookDetailsFragment.BookDetailsFragmentCommunicator) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BookDetailsFragmentCommunicator");
        }
    }

    private void downloadAudioBook(final Book book){
        final Context mContext = (Context) parentFragment;
        new Thread() {
            @Override
            public void run(){
                File bookDir = new File(mContext.getFilesDir(), "books");
                if(!bookDir.exists()){
                    bookDir.mkdir();
                }
                File ofile = new File(bookDir, "book-" + String.valueOf(book.getId()) + ".mp3");
                String url = mContext.getResources().getString(R.string.BookDownloadAPI) + book.getId();
                try {
                    URL bookAPI = new URL(url);
                    InputStream in = new BufferedInputStream(bookAPI.openStream());
                    OutputStream out = new FileOutputStream(ofile);
                    byte bytes[] = new byte[4096];
                    int numBytesRead;
                    while ((numBytesRead = in.read(bytes)) != -1) {
                        out.write(bytes, 0, numBytesRead);
                    }
                    out.flush();
                    out.close();
                    in.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
                DOWNLOADING = false;
                DOWNLOADED = true;
                parentFragment.onDownloadButtonPressed(book);
                downloadHandler.sendMessage(Message.obtain());
            }
        }.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface BookDetailsFragmentCommunicator {
        void onPlayButtonPressed(Book book);
        void onDownloadButtonPressed(Book book);
        void onDeleteButtonPressed(Book book);
    }

}
