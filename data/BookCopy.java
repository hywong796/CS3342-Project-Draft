package data;
import java.time.LocalDate;
import java.util.Optional;

//role: act as a physical copy in the book collection of a library

public class BookCopy implements Comparable<BookCopy>, AccessibleRecord<BookCopy.CopyFields>{
    
    public enum CopyFields implements Fields {
        OWNER (String.class),
        ISBN (String.class),
        COPY_ID (String.class),
        ACQUISITION_DATE (LocalDate.class),
        ACQUISITION_PRICE (Double.class),
        STATUS (Status.class),
        BORROWER_ID (String.class),
        LAST_BORROWING_DATE (LocalDate.class),
        BORROW_COUNTER (Integer.class);

        private final Class<?> fieldType;

        private CopyFields (Class<?> fieldType) {
            this.fieldType = fieldType;
        }

        public boolean isValidValue(Object value) {
            return fieldType.isInstance(value);
        }
        
        public static Optional<CopyFields> matchField(String target) {
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

    //Status
    public enum Status{
        AVAILABLE, BORROWED, PROCESSING, DAMAGED, LOST;
        
        public static boolean isAvailable(BookCopy bookCopy) {
            return bookCopy.status == AVAILABLE;
        }

        public static boolean isBorrowed (BookCopy bookCopy) {
            return bookCopy.status == BORROWED;
        }

        public static boolean isProcessing (BookCopy bookCopy) {
            return bookCopy.status == PROCESSING;
        }
        
        public static boolean needSpecialHandling (BookCopy bookCopy) {
            return bookCopy.status == DAMAGED || bookCopy.status == LOST;
        }
    }
    
    //Primary key
    private final String owner;
    private final String isbn;
    private final String copyID;
    
    //Acquisition
    private LocalDate acquisitionDate;
    private double acquisitionPrice;
    
    //Status
    private Status status;
    private String borrowerID;
    private LocalDate lastBorrowingDate;
    
    private int borrowCounter;

    //Constructors
    public BookCopy(
    String owner, String isbn, String copyID, 
    LocalDate acquisitionDate, double acquisitionPrice
    ){
        this.owner = owner;
        this.isbn = isbn;
        this.copyID = copyID;
        this.acquisitionDate = acquisitionDate;
        this.acquisitionPrice = acquisitionPrice;
        
        status = Status.AVAILABLE;
        borrowerID = null;
        lastBorrowingDate = null;
        borrowCounter = 0;
    }
    
    public BookCopy (
    String owner, String isbn, String copyID, 
    LocalDate acquisitionDate, double acquisitionPrice, 
    Status status, LocalDate lastBorrowingDate, int borrowCounter
    ){
        this.owner = owner;
        this.isbn = isbn;
        this.copyID = copyID;
        this.acquisitionDate = acquisitionDate;
        this.acquisitionPrice = acquisitionPrice;
        this.status = status;
        this.lastBorrowingDate = lastBorrowingDate;
        this.borrowCounter = borrowCounter;
        
        borrowerID = null;
    }
    
    public BookCopy (
    String owner, String isbn, String copyID, 
    LocalDate acquisitionDate, double acquisitionPrice, 
    Status status, String borrowerID, LocalDate lastBorrowingDate, 
    int borrowCount
    ){
        this.owner = owner;
        this.isbn = isbn;
        this.copyID = copyID;
        this.acquisitionDate = acquisitionDate;
        this.acquisitionPrice = acquisitionPrice;
        this.status = status;
        this.borrowerID = borrowerID;
        this.lastBorrowingDate = lastBorrowingDate;
        this.borrowCounter = borrowCount;
    }
    
    //getters
    public String getOwner() {return owner;}
    public String getIsbn() {return isbn;}
    public String getCopyID() {return copyID;}
    public LocalDate getAcquisitionDate() {return acquisitionDate;}
    public double getAcquisitionPrice() {return acquisitionPrice;}
    public Status getStatus() {return status;}
    public String getBorrowerID() {return borrowerID;}
    public LocalDate getLastBorrowingDate() {return lastBorrowingDate;}
    public int getBorrowCounter() {return borrowCounter;}

    @Override
    public Object getField(String targetField) {
        if (CopyFields.isValidField(targetField)) {
            return switch ((CopyFields.matchField(targetField)).get()) {
                case OWNER -> getOwner();
                case ISBN -> getIsbn();
                case COPY_ID -> getCopyID();
                case ACQUISITION_DATE -> getAcquisitionDate();
                case ACQUISITION_PRICE -> getAcquisitionPrice();
                case STATUS -> getStatus();
                case BORROWER_ID -> getBorrowerID();
                case LAST_BORROWING_DATE -> getLastBorrowingDate();
                case BORROW_COUNTER -> getBorrowCounter();
            };
        } else {
            System.out.println("Unmatched field");
            return Optional.empty();
        }
    }

    @Override
    public Object getField(CopyFields targetField) {
        return switch (targetField) {
            case OWNER -> getOwner();
            case ISBN -> getIsbn();
            case COPY_ID -> getCopyID();
            case ACQUISITION_DATE -> getAcquisitionDate();
            case ACQUISITION_PRICE -> getAcquisitionPrice();
            case STATUS -> getStatus();
            case BORROWER_ID -> getBorrowerID();
            case LAST_BORROWING_DATE -> getLastBorrowingDate();
            case BORROW_COUNTER -> getBorrowCounter();
        };
    }
    
    //setters
    public void setAcquisitionDate(LocalDate acquisitionDate) {this.acquisitionDate = acquisitionDate;}
    public void setAcquisitionPrice(double acquisitionPrice) {this.acquisitionPrice = acquisitionPrice;}

    public void setAsAvailable() {status = Status.AVAILABLE;}
    public void setAsDamaged() {status = Status.DAMAGED;}
    public void setAsLost() {status = Status.LOST;}

    @Override
    public boolean setField (CopyFields targetField, Object newValue) {
        if (targetField.isValidValue(newValue)) {
            if (targetField.equals(CopyFields.ACQUISITION_DATE)) {
                acquisitionDate = (LocalDate) newValue;
            }
            else if (targetField.equals(CopyFields.ACQUISITION_PRICE)) {
                acquisitionPrice = (double) newValue;
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
    
    // borrow()
    public boolean borrowMe(String borrowerID) {
        if (!Status.isAvailable(this)) {
            return false;
        } 
        else {
            status = Status.BORROWED;
            this.borrowerID = borrowerID;
            lastBorrowingDate = LocalDate.now();
            borrowCounter++;
            return true;
        }
    }

    // return()
    public void returnMe() {
        status = Status.PROCESSING;
        borrowerID = null;
    }

    //toString()
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("");
        out.append("Owner: ").append(owner).append("\n");
        out.append("ISBN: ").append(isbn).append("\n");
        out.append("Copy ID: ").append(copyID).append("\n");
        out.append("Acquisition Date: ").append(acquisitionDate).append("\n");
        out.append("Acquisition Price: ").append(acquisitionPrice).append("\n");
        out.append("Status: ").append(status).append("\n");
        out.append("Borrower ID: ").append(borrowerID != null? borrowerID: "null"); out.append("\n");
        out.append("Last Borrowing Date: ").append(lastBorrowingDate != null? lastBorrowingDate.toString() : "null").append("\n");
        return out.toString();
    }
    //compareTo()
    @Override
    public int compareTo(BookCopy others) {
        return this.isbn.compareTo(others.isbn);
    }
}