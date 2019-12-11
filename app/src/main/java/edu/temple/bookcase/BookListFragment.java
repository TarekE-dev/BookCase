package edu.temple.bookcase;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import static android.widget.AdapterView.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BookListFragmentCommunicator} interface
 * to handle interaction events.
 * Use the {@link BookListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookListFragment extends Fragment {

    ListView listView;
    ArrayList<Book> books = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String BOOKLIST = "BOOK_LIST";

    private BookListFragmentCommunicator parentFragment;

    public BookListFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param books Parameter 1.
     * @return A new instance of fragment BookListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BookListFragment newInstance(ArrayList<Book> books) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(BOOKLIST, books);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            books = getArguments().getParcelableArrayList(BOOKLIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        if(books.size() == 0){
            books.add(new Book(""));
        }
        listView.setAdapter(new ListViewAdapter(getActivity(), books));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                parentFragment.onBookClicked(i);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookListFragmentCommunicator) {
            parentFragment = (BookListFragmentCommunicator) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BookListFragmentCommunicator");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentFragment = null;
    }

    public ArrayList<Book> getBooks(){
        return this.books;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface BookListFragmentCommunicator {
        void onBookClicked(int index);
    }
}
