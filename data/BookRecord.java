package data;
import java.time.*;
import java.util.Optional;

//role: act as a book collection of a library

public class BookRecord implements Comparable<BookRecord>, AccessibleRecord<BookRecord.RecordFields>{
    
    //fields
    public enum RecordFields implements Fields {
        MAX_DAYS_ALLOWED (Integer.class),
        RATE_PER_DAY (Integer.class),
        OWNER (String.class),
        ISBN (String.class),
        TITLE (String.class),
        AUTHOR (String.class),
        LANGUAGE (String.class),
        CATEGORY (String.class),
        PUBLISHING_YEAR (Year.class),
        COPY_COUNTER (Integer.class),
        TOTAL_COPIES (Integer.class),
        AVAILABLE_COPIES (Integer.class),
        BORROW_COUNT (Integer.class);

        private final Class<?> fieldType;

        private RecordFields (Class<?> fieldType) {
            this.fieldType = fieldType;
        }

        public boolean isValidValue(Object value) {
            return fieldType.isInstance(value);
        }

        public static Optional<RecordFields> matchField(String target) {
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

    //static field (decrypted)
    private final int maxDaysAllowed = 30;
    private final int ratePerDay = 2;

    //primary key
    private final String owner;
    private final String isbn;

    //info
    private String title;
    private String author;
    private String language;
    private String category;
    private Year publishingYear;
    private int borrowCount;

    //collection management
    private int copyCounter;
    private int totalCopies;
    private int availableCopies;

    //constructor
    public BookRecord(
        String owner, String isbn, 
        String title, String author, String language, String category, Year publishingYear
    ) {
        this.owner = owner;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.language = language;
        this.category = category;
        this.publishingYear = publishingYear;
        
        this.copyCounter = 0;
        this.totalCopies = 0;
        this.availableCopies = 0;
        this.borrowCount = 0;
    }

    public BookRecord(
        String owner, String isbn, 
        String title, String author, String language, String category, Year publishingYear, int borrowCount,
        int copyCounter, int totalCopies, int availableCopies
    ) {
        this.owner = owner;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.language = language;
        this.category = category;
        this.publishingYear = publishingYear;
        this.copyCounter = copyCounter;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.borrowCount = borrowCount;
    }

    //getters
    public int getMaxDaysAllowed() { return maxDaysAllowed; }
    public int getRatePerDay() { return ratePerDay; }
    public String getOwner() {return owner;}
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getLanguage() { return language; }
    public String getCategory() { return category; }
    public Year getPublishingYear() { return publishingYear; }
    public int getCopyCounter() {return copyCounter; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public int getBorrowCount() { return borrowCount; }

    @Override
    public Object getField(String targetField) {
        if (RecordFields.isValidField(targetField)) {
            return switch ((RecordFields.matchField(targetField)).get()) {
                case MAX_DAYS_ALLOWED -> getMaxDaysAllowed();
                case RATE_PER_DAY -> getRatePerDay();
                case OWNER -> getOwner();
                case ISBN -> getIsbn();
                case TITLE -> getTitle();
                case AUTHOR -> getAuthor();
                case LANGUAGE -> getLanguage();
                case CATEGORY -> getCategory();
                case PUBLISHING_YEAR -> getPublishingYear();
                case COPY_COUNTER -> getCopyCounter();
                case TOTAL_COPIES -> getTotalCopies();
                case AVAILABLE_COPIES -> getAvailableCopies();
                case BORROW_COUNT -> getBorrowCount();
            };
        } else {
            System.out.println("Unmatched field");
            return Optional.empty();
        }
    }

    @Override
    public Object getField(RecordFields targetField) {
        return switch (targetField) {
            case MAX_DAYS_ALLOWED -> getMaxDaysAllowed();
            case RATE_PER_DAY -> getRatePerDay();
            case OWNER -> getOwner();
            case ISBN -> getIsbn();
            case TITLE -> getTitle();
            case AUTHOR -> getAuthor();
            case LANGUAGE -> getLanguage();
            case CATEGORY -> getCategory();
            case PUBLISHING_YEAR -> getPublishingYear();
            case COPY_COUNTER -> getCopyCounter();
            case TOTAL_COPIES -> getTotalCopies();
            case AVAILABLE_COPIES -> getAvailableCopies();
            case BORROW_COUNT -> this.getBorrowCount();
        };
    }
    

    //setters
    public void setTitle(String title) {this.title = title;}
    public void setAuthor(String author) {this.author = author;}
    public void setLanguage(String language) {this.language = language;}
    public void setCategory(String category) {this.category = category;}
    public void setPublishingYear(Year publishingYear) {this.publishingYear = publishingYear;}
    
    @Override
    public boolean setField(RecordFields targetField, Object newValue) {
        if (targetField.isValidValue(newValue)) {
            switch (targetField) {
                case MAX_DAYS_ALLOWED, RATE_PER_DAY -> {
                    System.out.println("Not available in this version");
                    return false;
                }
                case OWNER, ISBN -> {
                    System.out.println("Owner/ISBN field cannot be edited. " +
                                    "Please remove the record or undo the record creation instead.");
                    return false;
                }
                case TITLE           -> title = (String) newValue;
                case AUTHOR          -> author = (String) newValue;
                case LANGUAGE        -> language = (String) newValue;
                case CATEGORY        -> category = (String) newValue;
                case PUBLISHING_YEAR -> publishingYear = (Year) newValue;
                default -> {
                    System.out.println("Field does not exist.");
                    return false;
                }
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

    //BookCopy Management
    public void addCopy() {
        copyCounter++;
        totalCopies++;
        availableCopies++;
    }
    public void removeCopy() {
        if (totalCopies > 0) totalCopies--;
        if (availableCopies > 0) availableCopies--;
        availableCopies--;
    }
    public void borrowCopy() {
        availableCopies--;
        borrowCount++;
    }
    public void finishCopyProcessing() {
        availableCopies++;
    }
    public void undoBorrowCopy() {
        availableCopies++;
        borrowCount--;
    }

    //toString()
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Owner: ").append(owner).append("\n");
        out.append("ISBN: ").append(isbn).append("\n");
        out.append("Title: ").append(title).append("\n");
        out.append("Author: ").append(author).append("\n");
        out.append("Language: ").append(language).append("\n");
        out.append("Category: ").append(category).append("\n");
        out.append("Publishing Year: ").append(publishingYear).append("\n");
        out.append("Total Copies: ").append(totalCopies).append("\n");
        out.append("Available Copies: ").append(availableCopies).append("\n");
        return out.toString();
    }

    //compareTo()
    @Override
    public int compareTo(BookRecord others) {
        return this.isbn.compareTo(others.isbn);
    }
}