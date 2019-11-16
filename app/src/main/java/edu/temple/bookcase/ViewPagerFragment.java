package edu.temple.bookcase;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import static androidx.viewpager.widget.PagerAdapter.POSITION_NONE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ViewPagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPagerFragment extends Fragment {

    ViewPager viewPager;
    ArrayList<Book> bookList;
    ArrayList<BookDetailsFragment> books = new ArrayList<BookDetailsFragment>();
    BookAdapter bookAdapter;

    public class BookAdapter extends FragmentPagerAdapter {
        public ArrayList<BookDetailsFragment> books;
        private BookDetailsFragment currentFragment = null;
        public BookAdapter(@NonNull FragmentManager fm, ArrayList<BookDetailsFragment> books) {
            super(fm);
            this.books = books;
        }
        public int getItemPosition(Object object) { return POSITION_NONE; }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            currentFragment = books.get(position);
            return currentFragment;
        }

        public BookDetailsFragment getCurrentFragment(){
            return currentFragment;
        }

        @Override
        public int getCount() {
            return books.size();
        }
    }


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String BOOKLIST = "BOOK_LIST";

    public ViewPagerFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param books Parameter 1.
     * @return A new instance of fragment ViewPagerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewPagerFragment newInstance(ArrayList<Book> books) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(BOOKLIST, books);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookList = getArguments().getParcelableArrayList(BOOKLIST);
            for(Book book: bookList) {
                books.add(BookDetailsFragment.newInstance(book));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager_adapter, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        bookAdapter = new BookAdapter(getChildFragmentManager(), books);
        viewPager.setAdapter(bookAdapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public ArrayList<Book> getBooks(){return this.bookList;}

    public BookDetailsFragment getCurrentFragment(){
        if(bookAdapter == null)
            return null;
        return bookAdapter.getCurrentFragment();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
