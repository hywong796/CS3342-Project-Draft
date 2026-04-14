package data;

import data.BookCopy.CopyFields;
import data.BookRecord.RecordFields;
import java.time.*;
import java.util.*;

import utilities.Searching;
import static utilities.Searching.searchMultiple;
import static utilities.Searching.searchSingle;
import utilities.Validation;

public class Library implements Comparable<Library>, AccessibleRecord<Library.LibraryFields>{

    //field
    public enum LibraryFields implements Fields {
        LIBRARY_ID (String.class),
        NAME (String.class),
        ADDRESS (String.class),
        PHONE (String.class),
        EMAIL (String.class);

        private final Class<?> fieldType;

        private LibraryFields (Class<?> fieldType) {
            this.fieldType = fieldType;
        }

        public boolean isValidValue(Object value) {
            return fieldType.isInstance(value);
        }

        public static Optional<LibraryFields> matchField(String target) {
            try {
                return Optional.of(valueOf(target.toUpperCase()));
            } 
            catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        public static boolean isValidField(String target) {
            return matchField(target).isPresent();
        }
    }

    //info
    private String libraryID;
    private String name;
    private String address;
    private String phone;
    private String email;
    //private ArrayList<LibraryTimeSlot> libraryOpeningTime;

    private ArrayList<BookCopy> bookCopyCollection = new ArrayList<>();
    private ArrayList<BookRecord> bookRecordCollection = new ArrayList<>();


    //constructor
    public Library(String libraryID, String name, String address, String phone, String email){
        this.libraryID = libraryID;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    //getters
    public String getLibraryID() {return libraryID;}
    public String getName() {return name;}
    public String getAddress() {return address;}
    public String getPhone() {return phone;}
    public String getEmail() {return email;}
    public ArrayList<BookCopy> getBookCopyCollection() {return bookCopyCollection;}
    public ArrayList<BookRecord> getBookRecordCollection() {return bookRecordCollection;}

    @Override
    public Object getField(String targetField) {
        if (LibraryFields.isValidField(targetField)) {
            return switch ((LibraryFields.matchField(targetField)).get()) {
                case LIBRARY_ID -> getLibraryID();
                case NAME -> getName();
                case ADDRESS -> getAddress();
                case PHONE -> getPhone();
                case EMAIL -> getEmail();
            };
        } else {
            System.out.println("Unmatched field");
            return Optional.empty();
        }
    }

    @Override
    public Object getField(LibraryFields targetField) {
        return switch (targetField) {
            case LIBRARY_ID -> getLibraryID();
            case NAME -> getName();
            case ADDRESS -> getAddress();
            case PHONE -> getPhone();
            case EMAIL -> getEmail();
        };  
    } 

    //setters
    public void setLibraryID(String libraryID) {this.libraryID = libraryID;}
    public void setName(String name) {this.name = name;}
    public void setAddress(String address) {this.address = address;}
    public void setPhone(String phone) {this.phone = phone;}
    public void setEmail(String email) {this.email = email;}
    public void setBookCopyCollection(ArrayList<BookCopy> bookCopyCollection) {this.bookCopyCollection = bookCopyCollection;}
    public void setBookRecordCollection(ArrayList<BookRecord> bookRecordCollection) {this.bookRecordCollection = bookRecordCollection;}

    public boolean setField (LibraryFields targetField, Object newValue) {
        if (targetField.isValidValue(newValue)) {
            switch (targetField) {
                case LIBRARY_ID -> setLibraryID((String) newValue);
                case NAME -> setName((String) newValue);
                case ADDRESS -> setAddress((String) newValue);
                case PHONE ->  setPhone((String) newValue);
                case EMAIL ->  setEmail((String) newValue);
            }
            return true;
        }
        else {
            StringBuilder outMessage = new StringBuilder();
            outMessage.append("Invalid value for ");
            outMessage.append("[").append(targetField.toString()).append("]");
            outMessage.append(" field. \n");
            System.out.println(outMessage);
            return false;
        }
    }

    //book record management:

    //addRecord() -To-Do: 
    public boolean addRecord(BookRecord record) {
        if (Validation.nullOrEmpty(record)) return false;

        if (Searching.searchSingle(bookRecordCollection, BookRecord.RecordFields.ISBN, record.getIsbn()).isPresent()) {
            return false; 
        }

        return bookRecordCollection.add(record);
    }

    public boolean addRecord(
        String isbn, 
        String title, String author, String language, String category, Year publishingYear
    ) {
        //null check
        if (Validation.nullOrEmpty(BookRecord.RecordFields.ISBN, isbn)) return false;
        if (Validation.nullOrEmpty(BookRecord.RecordFields.TITLE, title)) return false;
        if (Validation.nullOrEmpty(BookRecord.RecordFields.AUTHOR, author)) return false;
        if (Validation.nullOrEmpty(BookRecord.RecordFields.LANGUAGE, language)) return false;
        if (Validation.nullOrEmpty(BookRecord.RecordFields.CATEGORY, category)) return false;
        if (Validation.nullOrEmpty(BookRecord.RecordFields.PUBLISHING_YEAR, publishingYear)) return false;

        //attach isbn format check from toolbox
        
        //duplication check
        if (Searching.searchSingle(bookRecordCollection, BookRecord.RecordFields.ISBN, isbn).isPresent()) {
            System.out.println("Duplicate ISBN found");
            return false;
        }

        bookRecordCollection.add(new BookRecord(this.name, isbn, title, author, language, category, publishingYear));
        return true;
    }

    //editRecord() - To-Do: 
    public boolean editRecord(BookRecord targetRecord, BookRecord.RecordFields targetInfoField, String newValue) {
        //null check
        if (Validation.nullOrEmpty(targetRecord)) return false;
        else if (Validation.nullOrEmpty(targetInfoField)) return false;
        else if (Validation.nullOrEmpty(targetInfoField, newValue)) return false;

        return targetRecord.setField(targetInfoField, newValue);
    }
    
    //removeRecord() - To-Do: 
    public boolean removeRecord (BookRecord targetRecord) {
        if (Validation.nullOrEmpty(targetRecord)) return false;

        List<BookCopy> CopiesToRemove = 
            searchMultiple(bookCopyCollection, CopyFields.ISBN, targetRecord.getIsbn());

        if (CopiesToRemove.isEmpty()) return false;
        
        bookCopyCollection.removeAll(CopiesToRemove);

        return bookRecordCollection.remove(targetRecord);
    }

    public boolean removeRecord (ArrayList<BookRecord> targetRecords) {
        if (Validation.nullOrEmpty(targetRecords)) return false;
        
        for (BookRecord targetRecord : targetRecords) {
            List<BookCopy> CopiesToRemove = 
                searchMultiple(bookCopyCollection, BookCopy.CopyFields.ISBN, targetRecord.getIsbn());

            if (CopiesToRemove.isEmpty()) return false;

            ArrayList<BookCopy> CopiesToRemoveList = new ArrayList<>(CopiesToRemove);
            bookCopyCollection.removeAll(CopiesToRemoveList);
        }
        bookRecordCollection.removeAll(targetRecords);
        return true;
    }
    //book copy management

    //addCopy() - To-do: add all type null check
    public boolean addCopy (String isbn, LocalDate acquisitionDate, double acquisitionPrice) {
        if (Validation.nullOrEmpty(CopyFields.ISBN, isbn)) return false;
        if (Validation.nullOrEmpty(CopyFields.ACQUISITION_DATE, acquisitionDate)) return false;
        if (Validation.nullOrEmpty(CopyFields.ACQUISITION_PRICE, acquisitionPrice)) return false;

        //attach isbn format check from toolbox
        Optional<BookRecord> correspondingRecord = searchSingle(bookRecordCollection, BookRecord.RecordFields.ISBN, isbn);
        if (correspondingRecord.isEmpty()) {
            System.out.println("No record matched for ISBN: " + isbn);
            return false;
        }

        correspondingRecord.get().addCopy();
        bookCopyCollection.add(new BookCopy(this.name, isbn, String.format("%05d", correspondingRecord.get().getCopyCounter()), acquisitionDate, acquisitionPrice));
        return true;
    }

    //editCopy() - To-Do: Add multiple-type value input support
    public boolean editCopy (BookCopy targetCopy, CopyFields targetField, String newValue) {
        if (Validation.nullOrEmpty(targetCopy)) return false;
        else if (Validation.nullOrEmpty(targetField)) return false;
        else if (Validation.nullOrEmpty(targetField, newValue)) return false;

        return targetCopy.setField(targetField, newValue);
    }

    //removeCopy()
    public boolean removeCopy (BookCopy CopyToRemove) {
        if (Validation.nullOrEmpty(CopyToRemove)) return false;
        if (!bookCopyCollection.contains(CopyToRemove)) return false;
        return bookCopyCollection.remove(CopyToRemove);
    }

    public boolean removeCopy (ArrayList<BookCopy> CopiesToRemove) {
        if (Validation.nullOrEmpty(CopiesToRemove)) return false;
        if (!bookCopyCollection.containsAll(CopiesToRemove)) return false;
        return bookCopyCollection.removeAll(CopiesToRemove);
    }
    
    //borrowCopy()
    public boolean borrowCopy (BookCopy copyToBorrow, String borrowerId) {
        if (Validation.nullOrEmpty(copyToBorrow)) return false;
        if (!BookCopy.Status.isAvailable(copyToBorrow)) return false;

        BookRecord correspondingRecord = 
            searchSingle(bookRecordCollection, RecordFields.ISBN, copyToBorrow.getIsbn()).get();

        correspondingRecord.borrowCopy();
        copyToBorrow.borrowMe(borrowerId);
        return true;
    }

    //returnCopy()
    public boolean returnCopy (BookCopy copyToReturn) {
        if (Validation.nullOrEmpty(copyToReturn)) return false;
        if (!BookCopy.Status.isBorrowed(copyToReturn))return false;

        copyToReturn.returnMe();
        return true;
    }

    //finishCopyProcessing()
    public boolean processCopy (BookCopy copyToProcess) {
        if (Validation.nullOrEmpty(copyToProcess)) return false;
        if (!BookCopy.Status.isProcessing(copyToProcess)) return false;

        BookRecord correspondingRecord = 
            searchSingle(bookRecordCollection, RecordFields.ISBN, copyToProcess.getIsbn()).get();

        correspondingRecord.finishCopyProcessing();
        copyToProcess.setAsAvailable();
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Library{");
        out.append("ID='").append(libraryID).append("'");
        out.append(", Name='").append(name).append("'");
        out.append(", Address='").append(address).append("'");
        out.append(", Phone='").append(phone).append("'");
        out.append(", Email='").append(email).append("'");
        out.append(", BookRecords=").append(bookRecordCollection.size());
        out.append(", BookCopies=").append(bookCopyCollection.size());
        out.append("}");
        return out.toString();
    }

    @Override
    public int compareTo(Library other) {
        return this.name.compareTo(other.name);
    }
}