package edu.temple.bookcase;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;


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

    View inflatedView;
    View bookTitle;
    View bookImg;
    View bookAuthor;
    Button playButton;
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
                parentFragment.onButtonPressed(bookObj);
            }
        }});
        displayBook(bookObj);
        return inflatedView;
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
    }

    public Book getBook(){return this.bookObj;}

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

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface BookDetailsFragmentCommunicator {
        void onButtonPressed(Book book);
    }

}
