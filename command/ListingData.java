package command;

import data.Library;
import data.Library.LibraryFields;
import data.LibraryNetwork;
import data.BookRecord;
import data.BookCopy;
import utilities.Searching;
import utilities.ListingService;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class ListingData implements Command {
    
    private static Scanner scanner = new Scanner(System.in);

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: LIST [LIBRARIES|RECORDS|COPIES]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "LIBRARIES" -> listLibraries();
            case "RECORDS" -> listRecords();
            case "COPIES" -> listCopies();
            default -> System.out.println("Invalid option. Please use LIBRARIES, RECORDS, or COPIES");
        }
    }

    private void listLibraries() {
        ArrayList<Library> libraries = LibraryNetwork.getInstance();
        
        if (libraries.isEmpty()) {
            System.out.println("No libraries found.");
            return;
        }
        
        System.out.println("\n=== List of Libraries ===");
        ListingService.listing(libraries);
    }

    private void listRecords() {
        ArrayList<Library> libraries = LibraryNetwork.getInstance();
        
        if (libraries.isEmpty()) {
            System.out.println("No libraries found.");
            return;
        }
        
        System.out.print("Enter Library ID (leave blank to list from all libraries): ");
        String libraryID = scanner.nextLine().trim().toUpperCase();
        
        if (libraryID.isEmpty()) {
            // List records from all libraries
            System.out.println("\n=== Book Records from All Libraries ===");
            int totalRecords = 0;
            
            for (Library lib : libraries) {
                ArrayList<BookRecord> records = lib.getBookRecordCollection();
                if (!records.isEmpty()) {
                    System.out.println("\nLibrary: " + lib.getName() + " (" + lib.getLibraryID() + ")");
                    ListingService.listing(records);
                    totalRecords += records.size();
                }
            }
            
            System.out.println("Total records across all libraries: " + totalRecords);
        } else {
            // List records from specific library
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
            
            System.out.println("\n=== Book Records for Library: " + targetLibrary.get().getName() + " ===");
            
            if (records.isEmpty()) {
                System.out.println("No book records found for this library.");
                return;
            }
            
            ListingService.listing(records);
        }
    }

    private void listCopies() {
        ArrayList<Library> libraries = LibraryNetwork.getInstance();
        
        if (libraries.isEmpty()) {
            System.out.println("No libraries found.");
            return;
        }
        
        System.out.print("Enter Library ID (leave blank to list from all libraries): ");
        String libraryID = scanner.nextLine().trim().toUpperCase();
        
        if (libraryID.isEmpty()) {
            // List copies from all libraries
            System.out.println("\n=== Book Copies from All Libraries ===");
            int totalCopies = 0;
            
            for (Library lib : libraries) {
                ArrayList<BookCopy> copies = lib.getBookCopyCollection();
                if (!copies.isEmpty()) {
                    System.out.println("\nLibrary: " + lib.getName() + " (" + lib.getLibraryID() + ")");
                    ListingService.listing(copies);
                    totalCopies += copies.size();
                }
            }
            
            System.out.println("Total copies across all libraries: " + totalCopies);
        } else {
            // List copies from specific library
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
            
            System.out.println("\n=== Book Copies for Library: " + targetLibrary.get().getName() + " ===");
            
            if (copies.isEmpty()) {
                System.out.println("No book copies found for this library.");
                return;
            }
            
            ListingService.listing(copies);
        }
    }
}
