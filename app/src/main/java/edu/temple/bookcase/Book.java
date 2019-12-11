package edu.temple.bookcase;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

    private int id;
    private String title;
    private String author;
    private int published;
    private String coverURL;
    private int duration;
    private String filePath;
    private int bookPos = 0;

    public Book(String title){

    }

    public Book(int id, String title, String author, int published, String coverURL, int duration){
        this.id = id;
        this.title = title;
        this.author = author;
        this.published = published;
        this.coverURL = coverURL;
        this.duration = duration;
        filePath = null;
    }

    protected Book(Parcel in){
        id = in.readInt();
        title = in.readString();
        author = in.readString();
        published = in.readInt();
        coverURL = in.readString();
        duration = in.readInt();
        filePath = in.readString();
        bookPos = in.readInt();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel parcel) {
            return new Book(parcel);
        }

        @Override
        public Book[] newArray(int i) {
            return new Book[i];
        }
    };

    public int getId(){
        return this.id;
    }

    public String getTitle(){
        return this.title;
    }

    public String getAuthor(){
        return this.author;
    }

    public int getPublished(){
        return this.published;
    }

    public String getURL(){
        return this.coverURL;
    }

    public int getDuration() { return this.duration; }

    public String getFilePath(){ return this.filePath; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getBookPos() { return this.bookPos; }

    public void setBookPos(int bookPos){ this.bookPos = bookPos; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(author);
        parcel.writeInt(published);
        parcel.writeString(coverURL);
        parcel.writeInt(duration);
        parcel.writeString(filePath);
        parcel.writeInt(bookPos);
    }

    @Override
    public String toString(){
        return String.format("id: %d\ntitle: %s\nauthor: %s\npublished: %d\nURL: %s\nduration: %d\n",
                id, title, author, published, coverURL, duration);
    }

    public boolean equals(Book book){
        return book != null && book.getId() == this.getId() ? true : false;
    }
}
