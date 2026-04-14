package command;

import data.Library;
import data.Library.LibraryFields;
import data.BookRecord;
import data.BookRecord.RecordFields;
import data.BookCopy;
import data.BookCopy.CopyFields;
import data.LibraryNetwork;
import utilities.Searching;
import utilities.ListingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class SearchingData implements Command {
    
    private static Scanner scanner = new Scanner(System.in);

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: SEARCH [LIBRARIES|RECORDS|COPIES]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "LIBRARIES" -> searchLibraries();
            case "RECORDS" -> searchRecords();
            case "COPIES" -> searchCopies();
            default -> System.out.println("Invalid option. Please use LIBRARIES, RECORDS, or COPIES");
        }
    }

    private void searchLibraries() {
        ArrayList<Library> libraries = LibraryNetwork.getInstance();
        
        if (libraries.isEmpty()) {
            System.out.println("No libraries found.");
            return;
        }
        
        System.out.println("\n=== Search Libraries ===");
        System.out.println("Available fields: LIBRARY_ID, NAME, ADDRESS, PHONE, EMAIL");
        
        System.out.print("Enter field name to search by: ");
        String fieldName = scanner.nextLine().trim().toUpperCase();
        
        if (!LibraryFields.isValidField(fieldName)) {
            System.out.println("Invalid field: " + fieldName);
            return;
        }
        
        System.out.print("Enter search value: ");
        String searchValue = scanner.nextLine().trim();
        
        if (searchValue.isEmpty()) {
            System.out.println("Search value cannot be empty");
            return;
        }
        
        LibraryFields field = LibraryFields.matchField(fieldName).get();
        List<Library> results = Searching.searchMultiple(libraries, field, searchValue);
        
        if (results.isEmpty()) {
            System.out.println("No libraries found matching: " + fieldName + " = " + searchValue);
            return;
        }
        
        System.out.println("\n=== Search Results for Libraries ===");
        ListingService.listing(new ArrayList<>(results));
    }

    private void searchRecords() {
        ArrayList<Library> libraries = LibraryNetwork.getInstance();
        
        if (libraries.isEmpty()) {
            System.out.println("No libraries found.");
            return;
        }
        
        System.out.print("Enter Library ID (leave blank to search across all libraries): ");
        String libraryID = scanner.nextLine().trim().toUpperCase();
        
        System.out.println("\n=== Search Book Records ===");
        System.out.println("Available fields: OWNER, ISBN, TITLE, AUTHOR, LANGUAGE, CATEGORY, PUBLISHING_YEAR");
        System.out.println("                  COPY_COUNTER, TOTAL_COPIES, AVAILABLE_COPIES, BORROW_COUNT");
        
        System.out.print("Enter field name to search by: ");
        String fieldName = scanner.nextLine().trim().toUpperCase();
        
        if (!RecordFields.isValidField(fieldName)) {
            System.out.println("Invalid field: " + fieldName);
            return;
        }
        
        System.out.print("Enter search value: ");
        String searchValue = scanner.nextLine().trim();
        
        if (searchValue.isEmpty()) {
            System.out.println("Search value cannot be empty");
            return;
        }
        
        RecordFields field = RecordFields.matchField(fieldName).get();
        
        if (libraryID.isEmpty()) {
            // Search across all libraries
            System.out.println("\n=== Search Results for Book Records (All Libraries) ===");
            ArrayList<BookRecord> allResults = new ArrayList<>();
            
            for (Library lib : libraries) {
                ArrayList<BookRecord> records = lib.getBookRecordCollection();
                List<BookRecord> results = Searching.searchMultiple(records, field, searchValue);
                allResults.addAll(results);
            }
            
            if (allResults.isEmpty()) {
                System.out.println("No book records found matching: " + fieldName + " = " + searchValue);
                return;
            }
            
            ListingService.listing(allResults);
        } else {
            // Search in specific library
            Optional<Library> targetLibrary = Searching.searchSingle(
                libraries, 
                LibraryFields.LIBRARY_ID, 
                libraryID
            );
            
            if (targetLibrary.isEmpty()) {
                System.out.println("Library not found with ID: " + libraryID);
                return;
            }
            
            ArrayList<BookRecord> records = targetLibrary.get().getBookRecordCollection();
            List<BookRecord> results = Searching.searchMultiple(records, field, searchValue);
            
            if (results.isEmpty()) {
                System.out.println("No book records found in library " + libraryID + " matching: " + fieldName + " = " + searchValue);
                return;
            }
            
            System.out.println("\n=== Search Results for Book Records (Library: " + targetLibrary.get().getName() + ") ===");
            ListingService.listing(new ArrayList<>(results));
        }
    }

    private void searchCopies() {
        ArrayList<Library> libraries = LibraryNetwork.getInstance();
        
        if (libraries.isEmpty()) {
            System.out.println("No libraries found.");
            return;
        }
        
        System.out.print("Enter Library ID (leave blank to search across all libraries): ");
        String libraryID = scanner.nextLine().trim().toUpperCase();
        
        System.out.println("\n=== Search Book Copies ===");
        System.out.println("Available fields: OWNER, ISBN, COPY_ID, ACQUISITION_DATE, ACQUISITION_PRICE");
        System.out.println("                  STATUS, BORROWER_ID, LAST_BORROWING_DATE, BORROW_COUNTER");
        
        System.out.print("Enter field name to search by: ");
        String fieldName = scanner.nextLine().trim().toUpperCase();
        
        if (!CopyFields.isValidField(fieldName)) {
            System.out.println("Invalid field: " + fieldName);
            return;
        }
        
        System.out.print("Enter search value: ");
        String searchValue = scanner.nextLine().trim();
        
        if (searchValue.isEmpty()) {
            System.out.println("Search value cannot be empty");
            return;
        }
        
        CopyFields field = CopyFields.matchField(fieldName).get();
        
        if (libraryID.isEmpty()) {
            // Search across all libraries
            System.out.println("\n=== Search Results for Book Copies (All Libraries) ===");
            ArrayList<BookCopy> allResults = new ArrayList<>();
            
            for (Library lib : libraries) {
                ArrayList<BookCopy> copies = lib.getBookCopyCollection();
                List<BookCopy> results = Searching.searchMultiple(copies, field, searchValue);
                allResults.addAll(results);
            }
            
            if (allResults.isEmpty()) {
                System.out.println("No book copies found matching: " + fieldName + " = " + searchValue);
                return;
            }
            
            ListingService.listing(allResults);
        } else {
            // Search in specific library
            Optional<Library> targetLibrary = Searching.searchSingle(
                libraries, 
                LibraryFields.LIBRARY_ID, 
                libraryID
            );
            
            if (targetLibrary.isEmpty()) {
                System.out.println("Library not found with ID: " + libraryID);
                return;
            }
            
            ArrayList<BookCopy> copies = targetLibrary.get().getBookCopyCollection();
            List<BookCopy> results = Searching.searchMultiple(copies, field, searchValue);
            
            if (results.isEmpty()) {
                System.out.println("No book copies found in library " + libraryID + " matching: " + fieldName + " = " + searchValue);
                return;
            }
            
            System.out.println("\n=== Search Results for Book Copies (Library: " + targetLibrary.get().getName() + ") ===");
            ListingService.listing(new ArrayList<>(results));
        }
    }
}