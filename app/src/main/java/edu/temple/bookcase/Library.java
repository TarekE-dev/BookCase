package edu.temple.bookcase;

import java.util.ArrayList;

public class Library {

    private ArrayList<Book> bookList;

    public Library(ArrayList<Book> bookList) {
        this.bookList = bookList;
    }

    public Book getBookById(int id) {
        for(int i=0; i < bookList.size(); i++){
            if(bookList.get(i).getId() == id)
                return bookList.get(i);
        }
        return null;
    }

    public void setBookList(ArrayList<Book> bookList) {
        this.bookList = bookList;
    }

    public ArrayList<Book> getBookList() {
        return bookList;
    }
}
