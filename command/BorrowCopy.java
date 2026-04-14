package command;

import main.Main;
import data.Library;
import data.Library.LibraryFields;
import data.BookCopy;
import data.BookCopy.CopyFields;
import data.LibraryNetwork;
import utilities.Searching;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class BorrowCopy extends RecordedCommand {
    
    private static Scanner scanner = new Scanner(System.in);
    private static Library targetLibrary = Main.getCurrentLibrary();

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: BORROW [BYISBN|BYSEARCHINGNAME]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "BYISBN" -> borrowCopyByIsbn();
            case "BYSEARCHINGNAME" -> borrowCopyBySearchingName();
            default -> System.out.println("Invalid option. Please use BYISBN or BYSEARCHINGNAME");
        }
    }

    private void borrowCopyByIsbn() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Borrow Book Copy ===");
        
        // Get Library ID
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

        if (targetLibrary.getBookCopyCollection().isEmpty()) {
            System.out.println("No any copy collection in this library!");
            return;
        }
        
        // Get ISBN
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();
        
        // Search for available copies with this ISBN
        List<BookCopy> availableCopies = Searching.searchMultiple(
            targetLibrary.getBookCopyCollection(),
            CopyFields.ISBN,
            isbn
        );
        
        if (availableCopies.isEmpty()) {
            System.out.println("No copies found for ISBN: " + isbn);
            return;
        }
        
        // Filter for available copies only
        List<BookCopy> availableForBorrow = availableCopies.stream()
            .filter(copy -> BookCopy.Status.isAvailable(copy))
            .toList();
        
        if (availableForBorrow.isEmpty()) {
            System.out.println("No available copies for ISBN: " + isbn);
            return;
        }
        
        // Display available copies
        System.out.println("Available copies:");
        for (int i = 0; i < availableForBorrow.size(); i++) {
            System.out.println((i + 1) + ". Copy ID: " + availableForBorrow.get(i).getCopyID());
        }
        
        // Get copy selection
        System.out.print("Select copy number: ");
        int copyIndex;
        try {
            copyIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (copyIndex < 0 || copyIndex >= availableForBorrow.size()) {
                System.out.println("Invalid selection");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number");
            return;
        }
        
        BookCopy selectedCopy = availableForBorrow.get(copyIndex);
        
        // Get Borrower ID
        System.out.print("Enter Borrower ID: ");
        String borrowerID = scanner.nextLine().trim();
        
        // Borrow the copy
        if (targetLibrary.borrowCopy(selectedCopy, borrowerID)) {
            System.out.println("Book copy borrowed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to borrow book copy. Please check the details and try again.");
        }
    }

    private void borrowCopyBySearchingName() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Borrow Book Copy by Title/Author ===");
        
        // Get Library ID
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
        
        // Get search term
        System.out.print("Enter Title or Author name: ");
        String searchTerm = scanner.nextLine().trim();
        
        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty");
            return;
        }
        
        // Get Borrower ID
        System.out.print("Enter Borrower ID: ");
        String borrowerID = scanner.nextLine().trim();
        
        if (borrowerID.isEmpty()) {
            System.out.println("Borrower ID cannot be empty");
            return;
        }
        
        // Search for available copies matching the search term
        List<BookCopy> foundCopies = targetLibrary.getBookCopyCollection().stream()
            .filter(copy -> {
                // Find the corresponding record
                Optional<data.BookRecord> record = Searching.searchSingle(
                    targetLibrary.getBookRecordCollection(),
                    data.BookRecord.RecordFields.ISBN,
                    copy.getIsbn()
                );
                
                if (record.isEmpty()) {
                    return false;
                }
                
                // Check if title or author matches
                String title = (String) record.get().getField(data.BookRecord.RecordFields.TITLE);
                String author = (String) record.get().getField(data.BookRecord.RecordFields.AUTHOR);
                
                return (title != null && title.toLowerCase().contains(searchTerm.toLowerCase())) ||
                       (author != null && author.toLowerCase().contains(searchTerm.toLowerCase()));
            })
            .filter(copy -> BookCopy.Status.isAvailable(copy))
            .toList();
        
        if (foundCopies.isEmpty()) {
            System.out.println("No available copies found matching: " + searchTerm);
            return;
        }
        
        // Display found copies
        System.out.println("Available copies found:");
        for (int i = 0; i < foundCopies.size(); i++) {
            BookCopy copy = foundCopies.get(i);
            Optional<data.BookRecord> record = Searching.searchSingle(
                targetLibrary.getBookRecordCollection(),
                data.BookRecord.RecordFields.ISBN,
                copy.getIsbn()
            );
            
            if (record.isPresent()) {
                String title = (String) record.get().getField(data.BookRecord.RecordFields.TITLE);
                System.out.println((i + 1) + ". Title: " + title + " | Copy ID: " + copy.getCopyID());
            }
        }
        
        // Get copy selection
        System.out.print("Select copy number: ");
        int copyIndex;
        try {
            copyIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (copyIndex < 0 || copyIndex >= foundCopies.size()) {
                System.out.println("Invalid selection");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number");
            return;
        }
        
        BookCopy selectedCopy = foundCopies.get(copyIndex);
        
        // Borrow the copy
        if (targetLibrary.borrowCopy(selectedCopy, borrowerID)) {
            System.out.println("Book copy borrowed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to borrow book copy. Please check the details and try again.");
        }
    }

    @Override
    public void undoMe() {
        System.out.println("Undo functionality not yet implemented for BorrowCopy");
    }

    @Override
    public void redoMe() {
        System.out.println("Redo functionality not yet implemented for BorrowCopy");
    }
}
