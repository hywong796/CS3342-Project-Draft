package command;

import main.Main;
import data.Library;
import data.Library.LibraryFields;
import data.BookRecord;
import data.BookCopy;
import data.LibraryNetwork;
import utilities.Searching;
import utilities.Sorting;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class SortingData implements Command {
    
    private static Scanner scanner = new Scanner(System.in);

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: SORT [COPY|RECORD|LIBRARY]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "COPY" -> sortCopy();
            case "RECORD" -> sortRecord();
            case "LIBRARY" -> sortLibrary();
            default -> System.out.println("Invalid option. Please use COPY, RECORD, or LIBRARY");
        }
    }

    private void sortCopy() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Sort Book Copies ===");
        
        // Get current library
        Library targetLibrary = Main.getCurrentLibrary();
        if (targetLibrary == null) {
            System.out.print("Enter Library ID: ");
            String libraryID = scanner.nextLine().trim().toUpperCase();

            Optional<Library> foundLibrary = Searching.searchSingle(LibraryNetwork.getInstance(), LibraryFields.LIBRARY_ID, libraryID);
            if (foundLibrary.isEmpty()) {
                System.out.println("Library not found with ID: " + libraryID);
                return;
            }
            targetLibrary = foundLibrary.get();
        }
        
        // Display sortable fields
        System.out.println("\n# Available Sort Fields:");
        System.out.println("- [1] ISBN");
        System.out.println("- [2] Copy ID");
        System.out.println("- [3] Acquisition Date");
        System.out.println("- [4] Acquisition Price");
        System.out.println("- [5] Owner");
        
        System.out.print("\nSelect field to sort by (1-5): ");
        String choice = scanner.nextLine().trim();
        
        Sorting.BookCopySortRules sortRule;
        switch (choice) {
            case "1":
                sortRule = Sorting.BookCopySortRules.ISBN;
                break;
            case "2":
                sortRule = Sorting.BookCopySortRules.COPYID;
                break;
            case "3":
                sortRule = Sorting.BookCopySortRules.ACQUISITION_DATE;
                break;
            case "4":
                sortRule = Sorting.BookCopySortRules.ACQUISITION_PRICE;
                break;
            case "5":
                sortRule = Sorting.BookCopySortRules.OWNER;
                break;
            default:
                System.out.println("Invalid option");
                return;
        }
        
        // Perform sort
        ArrayList<Sorting.SortRules<BookCopy>> sortRules = new ArrayList<>();
        sortRules.add(sortRule);
        ArrayList<BookCopy> sortedCopies = Sorting.sort(targetLibrary.getBookCopyCollection(), sortRules);
        
        // Display sorted results
        System.out.println("\n# Sorted Book Copies (by " + sortRule + "):");
        if (sortedCopies.isEmpty()) {
            System.out.println("No copies to display");
        } else {
            for (int i = 0; i < sortedCopies.size(); i++) {
                BookCopy copy = sortedCopies.get(i);
                System.out.println("- [" + (i + 1) + "] Copy ID: " + copy.getCopyID() + 
                                 " | ISBN: " + copy.getIsbn() + " | Status: " + copy.getStatus());
            }
        }
        
        // Store sorted list back to target collection
        targetLibrary.getBookCopyCollection().clear();
        targetLibrary.getBookCopyCollection().addAll(sortedCopies);
        
        System.out.println("\n✓ Sort complete");
    }

    private void sortRecord() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Sort Book Records ===");
        
        // Get current library
        Library targetLibrary = Main.getCurrentLibrary();
        if (targetLibrary == null) {
            System.out.print("Enter Library ID: ");
            String libraryID = scanner.nextLine().trim().toUpperCase();

            Optional<Library> foundLibrary = Searching.searchSingle(LibraryNetwork.getInstance(), LibraryFields.LIBRARY_ID, libraryID);
            if (foundLibrary.isEmpty()) {
                System.out.println("Library not found with ID: " + libraryID);
                return;
            }
            targetLibrary = foundLibrary.get();
        }
        
        // Display sortable fields
        System.out.println("\n# Available Sort Fields:");
        System.out.println("- [1] ISBN");
        System.out.println("- [2] Title");
        System.out.println("- [3] Author");
        System.out.println("- [4] Language");
        System.out.println("- [5] Category");
        System.out.println("- [6] Publishing Year");
        System.out.println("- [7] Owner");
        
        System.out.print("\nSelect field to sort by (1-7): ");
        String choice = scanner.nextLine().trim();
        
        Sorting.BookRecordSortRules sortRule;
        switch (choice) {
            case "1":
                sortRule = Sorting.BookRecordSortRules.ISBN;
                break;
            case "2":
                sortRule = Sorting.BookRecordSortRules.TITLE;
                break;
            case "3":
                sortRule = Sorting.BookRecordSortRules.AUTHOR;
                break;
            case "4":
                sortRule = Sorting.BookRecordSortRules.LANGUAGE;
                break;
            case "5":
                sortRule = Sorting.BookRecordSortRules.CATEGORY;
                break;
            case "6":
                sortRule = Sorting.BookRecordSortRules.PUBLISHING_YEAR;
                break;
            case "7":
                sortRule = Sorting.BookRecordSortRules.OWNER;
                break;
            default:
                System.out.println("Invalid option");
                return;
        }
        
        // Perform sort
        ArrayList<Sorting.SortRules<BookRecord>> sortRules = new ArrayList<>();
        sortRules.add(sortRule);
        ArrayList<BookRecord> sortedRecords = Sorting.sort(targetLibrary.getBookRecordCollection(), sortRules);
        
        // Display sorted results
        System.out.println("\n# Sorted Book Records (by " + sortRule + "):");
        if (sortedRecords.isEmpty()) {
            System.out.println("No records to display");
        } else {
            for (int i = 0; i < sortedRecords.size(); i++) {
                BookRecord record = sortedRecords.get(i);
                System.out.println("- [" + (i + 1) + "] " + record.getTitle() + 
                                 " | Author: " + record.getAuthor() + " | ISBN: " + record.getIsbn());
            }
        }
        
        // Store sorted list back to target collection
        targetLibrary.getBookRecordCollection().clear();
        targetLibrary.getBookRecordCollection().addAll(sortedRecords);
        
        System.out.println("\n✓ Sort complete");
    }

    private void sortLibrary() {
        System.out.println("=== Sort Libraries ===");
        
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No libraries to sort");
            return;
        }
        
        // Display sortable fields
        System.out.println("\n# Available Sort Fields:");
        System.out.println("- [1] Name");
        System.out.println("- [2] Address");
        System.out.println("- [3] Phone");
        System.out.println("- [4] Email");
        System.out.println("- [5] Library ID");
        
        System.out.print("\nSelect field to sort by (1-5): ");
        String choice = scanner.nextLine().trim();
        
        Sorting.LibrarySortRules sortRule;
        switch (choice) {
            case "1":
                sortRule = Sorting.LibrarySortRules.NAME;
                break;
            case "2":
                sortRule = Sorting.LibrarySortRules.ADDRESS;
                break;
            case "3":
                sortRule = Sorting.LibrarySortRules.PHONE;
                break;
            case "4":
                sortRule = Sorting.LibrarySortRules.EMAIL;
                break;
            case "5":
                sortRule = Sorting.LibrarySortRules.LIBRARY_ID;
                break;
            default:
                System.out.println("Invalid option");
                return;
        }
        
        // Perform sort
        ArrayList<Sorting.SortRules<Library>> sortRules = new ArrayList<>();
        sortRules.add(sortRule);
        ArrayList<Library> sortedLibraries = Sorting.sort(LibraryNetwork.getInstance(), sortRules);
        
        // Display sorted results
        System.out.println("\n# Sorted Libraries (by " + sortRule + "):");
        if (sortedLibraries.isEmpty()) {
            System.out.println("No libraries to display");
        } else {
            for (int i = 0; i < sortedLibraries.size(); i++) {
                Library lib = sortedLibraries.get(i);
                System.out.println("- [" + (i + 1) + "] " + lib.getName() + 
                                 " (ID: " + lib.getLibraryID() + ") | Address: " + lib.getAddress());
            }
        }
        
        // Store sorted list back to LibraryNetwork
        LibraryNetwork.getInstance().clear();
        LibraryNetwork.getInstance().addAll(sortedLibraries);
        
        System.out.println("\n✓ Sort complete");
    }
}
