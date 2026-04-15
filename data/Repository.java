package data;

import java.io.*;
import java.util.ArrayList;

/* role:
read files
load data into files
*/
@SuppressWarnings("unused")
public class Repository {
    private static ArrayList<Library> LibraryData = new ArrayList<>();
    private static ArrayList<BookRecord> BookRecordData = new ArrayList<>();
    private static ArrayList<BookCopy> BookCopyData = new ArrayList<>();

    private static String LIB_FILE = "txt/library.txt";
    private static String RECORD_FILE = "txt/bookRecord.txt";
    private static String COPY_FILE = "txt/bookCopy.txt"; 

    //getters
    public static ArrayList<Library> getLibraryData() {
        return LibraryData;
    }
    public static ArrayList<BookRecord> getBookRecordData() {
        return BookRecordData;
    }
    public static ArrayList<BookCopy> getBookCopyData() {
        return BookCopyData;
    }

    //setters
    public static void setLibraryData(ArrayList<Library> data) {
        LibraryData = data;
    }
    public static void setBookRecordData(ArrayList<BookRecord> data) {
        BookRecordData = data;
    }
    public static void setBookCopyData(ArrayList<BookCopy> data) {
        BookCopyData = data;
    }

    public static void storeToFiles() {
        // 1. Get all libraries from LibraryNetwork
        LibraryData = LibraryNetwork.getInstance();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LIB_FILE))) {
            
            for (Library lib : LibraryData) {
                writer.write(lib.getLibraryID() + "|"+
                lib.getName() + "|"+
                lib.getAddress() + "|"+
                lib.getPhone() + "|"+
                lib.getEmail());
                writer.newLine();
            }
            System.out.println("✅ LIBRARY STORATION SUCCESS!!!");
        } catch (Exception e) {
            System.out.println("❌ FUCK, HERE WE GO AGAIN (Library): " + e.getMessage());
        }

        // 2. Store book records
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECORD_FILE))) {
            for (Library lib : LibraryData) {
                for (BookRecord record : lib.getBookRecordCollection()) {
                    writer.write(
                        record.getOwner() + "|" +
                        record.getIsbn() + "|" +
                        record.getTitle() + "|" +
                        record.getAuthor() + "|" +
                        record.getLanguage() + "|" +
                        record.getCategory() + "|" +
                        record.getPublishingYear() + "|" +
                        record.getBorrowCount() + "|" +
                        record.getCopyCounter() + "|" +
                        record.getTotalCopies() + "|" +
                        record.getAvailableCopies()
                    );
                    writer.newLine();
                }
            }
            System.out.println("✅ BOOK STORATION SUCCESS！");
        } catch (IOException e) {
            System.out.println("❌ FAIL TO STORE THE BOOK: " + e.getMessage());
        }

        // 3. Store book copies
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COPY_FILE))) {
            for (Library lib : LibraryData) {
                for (BookCopy copy : lib.getBookCopyCollection()) {
                    // For copies that are AVAILABLE and haven't been borrowed, use the simple format
                    if (copy.getStatus() == BookCopy.Status.AVAILABLE && copy.getBorrowCounter() == 0) {
                        writer.write(
                            copy.getOwner() + "|" +
                            copy.getIsbn() + "|" +
                            copy.getCopyID() + "|" +
                            copy.getAcquisitionDate() + "|" +
                            copy.getAcquisitionPrice()
                        );
                    } else {
                        // For all other copies, store the full format
                        writer.write(
                            copy.getOwner() + "|" +
                            copy.getIsbn() + "|" +
                            copy.getCopyID() + "|" +
                            copy.getAcquisitionDate() + "|" +
                            copy.getAcquisitionPrice() + "|" +
                            copy.getStatus() + "|" +
                            copy.getBorrowerID() + "|" +
                            copy.getLastBorrowingDate() + "|" +
                            copy.getBorrowCounter()
                        );
                    }
                    writer.newLine();
                }
            }
            System.out.println("✅ COPY STORATION SUCCESS！");
        } catch (IOException e) {
            System.out.println("❌ FAIL TO STORE THE COPY: " + e.getMessage());
        }
    }

    public static void loadFromFiles() {
        // 1. Load libraries from file
        loadLibraryHelper(); 
        
        // Initialize LibraryNetwork with loaded library data
        LibraryNetwork.createInstance(LibraryData);

        // 2. Load book records
        loadBookRecordsHelper();
        
        // 3. Load book copies
        loadBookCopiesHelper();
    }

    private static void loadBookRecordsHelper() {
        File recordFile = new File(RECORD_FILE);
        if (!recordFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(recordFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length == 11) {
                    BookRecord record = new BookRecord(
                        p[0], p[1], p[2], p[3], p[4], p[5], 
                        java.time.Year.parse(p[6]),
                        Integer.parseInt(p[7]),
                        Integer.parseInt(p[8]),
                        Integer.parseInt(p[9]),
                        Integer.parseInt(p[10])
                    );
                    
                    // Add record to the corresponding library
                    LibraryNetwork.getInstance().stream()
                        .filter(lib -> lib.getLibraryID().equals(p[0]))
                        .findFirst()
                        .ifPresent(lib -> lib.addRecord(record));
                }
            }
            System.out.println("✅ Book records loaded successfully！");
        } catch (Exception e) {
            System.out.println("❌ Book records failed to load: " + e.getMessage());
        }
    }

    private static void loadBookCopiesHelper() {
        File copyFile = new File(COPY_FILE);
        if (!copyFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(copyFile))) {
            String lineContent;
            while ((lineContent = reader.readLine()) != null) {
                final String line = lineContent;
                String[] p = line.split("\\|");
                if (p.length >= 5) {
                    // Find the library and add the copy
                    LibraryNetwork.getInstance().stream()
                        .filter(lib -> lib.getLibraryID().equals(p[0]))
                        .findFirst()
                        .ifPresent(lib -> {
                            try {
                                BookCopy copy;
                                
                                // Check if this copy has been borrowed (status is AVAILABLE initially)
                                if (p.length == 5) {
                                    // Simple constructor for copies that haven't been borrowed
                                    copy = new BookCopy(
                                        p[0],                                    // owner
                                        p[1],                                    // isbn
                                        p[2],                                    // copyID
                                        java.time.LocalDate.parse(p[3]),        // acquisitionDate
                                        Double.parseDouble(p[4])                // acquisitionPrice
                                    );
                                } else if (p.length == 9) {
                                    // Full constructor with all fields
                                    copy = new BookCopy(
                                        p[0],                                    // owner
                                        p[1],                                    // isbn
                                        p[2],                                    // copyID
                                        java.time.LocalDate.parse(p[3]),        // acquisitionDate
                                        Double.parseDouble(p[4]),               // acquisitionPrice
                                        BookCopy.Status.valueOf(p[5]),          // status
                                        p[6].equals("null") ? null : p[6],      // borrowerID
                                        p[7].equals("null") ? null : java.time.LocalDate.parse(p[7]),  // lastBorrowingDate
                                        Integer.parseInt(p[8])                   // borrowCounter
                                    );
                                } else {
                                    System.out.println("⚠️ Skipping malformed copy record: " + line);
                                    return;
                                }
                                
                                lib.getBookCopyCollection().add(copy);
                            } catch (Exception e) {
                                System.out.println("❌ Error adding book copy: " + e.getMessage());
                            }
                        });
                }
            }
            System.out.println("✅ Book copies loaded successfully！");
        } catch (Exception e) {
            System.out.println("❌ Book copies failed to load: " + e.getMessage());
        }
    }

    private static void loadLibraryHelper() {
        File file = new File(LIB_FILE);
        if (!file.exists()) return;
        
        LibraryData.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    Library lib = new Library(parts[0], parts[1], parts[2], parts[3], parts[4]);
                    LibraryData.add(lib);
                }
            }
            System.out.println("✅ Libraries loaded successfully！");
        } catch (IOException e) { 
            System.out.println("❌ Library load failed: " + e.getMessage());
        }
    }
}