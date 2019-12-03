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
    BookAdapter bookAdapter;
    Book viewing;

    public class BookAdapter extends FragmentPagerAdapter {
        private BookDetailsFragment currentFragment = null;
        public BookAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }
        public int getItemPosition(Object object) { return POSITION_NONE; }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment newFragment = BookDetailsFragment.newInstance(bookList.get(position));
            viewing = bookList.get(position);
            System.out.println(viewing);
            return newFragment;
        }

        @Override
        public int getCount() {
            return bookList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position){
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            return createdFragment;
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager_adapter, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        bookAdapter = new BookAdapter(getChildFragmentManager());
        viewPager.setAdapter(bookAdapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public ArrayList<Book> getBooks(){return this.bookList;}

    public Book getCurrentBook(){
        return viewing;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
