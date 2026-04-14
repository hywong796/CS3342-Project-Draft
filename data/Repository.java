package data;

import java.util.ArrayList;

/* 
role:
read files
load data into files
*/
@SuppressWarnings("unused")
public class Repository {
    private static ArrayList<Library> LibraryData;
    private static ArrayList<BookRecord> BookRecordData;
    private static ArrayList<BookCopy> BookCopyData;

    //getters
    public static void getLibraryData() {
        
    }
    public static void getBookRecordData() {

    }
    public static void getBookCopyData() {

    }

    //setters
    public static void setLibraryData() {

    }
    public static void setBookRecordData() {

    }
    public static void setBookCopyData() {

    }

    public static void loadFromFiles() {
        //load data from library.txt to LibraryData
        //load data from bookRecord.txt to BookRecordData
        //load data from bookCopy.txt to BookCopyData
    }
    public static void storeToFiles() {
        //convert Library type object into string
        //write string into library.txt
        //convert BookRecord type object into string
        //write string into bookRecord.txt
        //convert BookCopy type object into string
        //write string into bookCopy.txt
    }
}