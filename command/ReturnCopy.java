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

public class ReturnCopy extends RecordedCommand {
    
    private static Scanner scanner = new Scanner(System.in);
    private static Library targetLibrary = Main.getCurrentLibrary();

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: RETURN [BYISBN|BYSEARCHINGNAME]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "BYISBN" -> returnCopyByIsbn();
            case "BYSEARCHINGNAME" -> returnCopyBySearchingName();
            default -> System.out.println("Invalid option. Please use BYISBN or BYSEARCHINGNAME");
        }
    }

    private void returnCopyByIsbn() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Return Book Copy ===");
        
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
        
        // Get ISBN
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();
        
        // Search for borrowed copies with this ISBN
        List<BookCopy> borrowedCopies = Searching.searchMultiple(
            targetLibrary.getBookCopyCollection(),
            CopyFields.ISBN,
            isbn
        );
        
        if (borrowedCopies.isEmpty()) {
            System.out.println("No copies found for ISBN: " + isbn);
            return;
        }
        
        // Filter for borrowed copies only
        List<BookCopy> borrowedForReturn = borrowedCopies.stream()
            .filter(copy -> BookCopy.Status.isBorrowed(copy))
            .toList();
        
        if (borrowedForReturn.isEmpty()) {
            System.out.println("No borrowed copies for ISBN: " + isbn);
            return;
        }
        
        // Display borrowed copies
        System.out.println("Borrowed copies:");
        for (int i = 0; i < borrowedForReturn.size(); i++) {
            BookCopy copy = borrowedForReturn.get(i);
            System.out.println((i + 1) + ". Copy ID: " + copy.getCopyID() + " | Borrower ID: " + copy.getBorrowerID());
        }
        
        // Get copy selection
        System.out.print("Select copy number: ");
        int copyIndex;
        try {
            copyIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (copyIndex < 0 || copyIndex >= borrowedForReturn.size()) {
                System.out.println("Invalid selection");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number");
            return;
        }
        
        BookCopy selectedCopy = borrowedForReturn.get(copyIndex);
        
        // Return the copy
        if (targetLibrary.returnCopy(selectedCopy)) {
            System.out.println("Book copy returned successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to return book copy. Please check the details and try again.");
        }
    }

    private void returnCopyBySearchingName() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Return Book Copy by Title/Author ===");
        
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
        
        // Search for borrowed copies matching the search term
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
            .filter(copy -> BookCopy.Status.isBorrowed(copy))
            .toList();
        
        if (foundCopies.isEmpty()) {
            System.out.println("No borrowed copies found matching: " + searchTerm);
            return;
        }
        
        // Display found copies
        System.out.println("Borrowed copies found:");
        for (int i = 0; i < foundCopies.size(); i++) {
            BookCopy copy = foundCopies.get(i);
            Optional<data.BookRecord> record = Searching.searchSingle(
                targetLibrary.getBookRecordCollection(),
                data.BookRecord.RecordFields.ISBN,
                copy.getIsbn()
            );
            
            if (record.isPresent()) {
                String title = (String) record.get().getField(data.BookRecord.RecordFields.TITLE);
                System.out.println((i + 1) + ". Title: " + title + " | Copy ID: " + copy.getCopyID() + " | Borrower: " + copy.getBorrowerID());
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
        
        // Return the copy
        if (targetLibrary.returnCopy(selectedCopy)) {
            System.out.println("Book copy returned successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to return book copy. Please check the details and try again.");
        }
    }

    @Override
    public void undoMe() {
        System.out.println("Undo functionality not yet implemented for ReturnCopy");
    }

    @Override
    public void redoMe() {
        System.out.println("Redo functionality not yet implemented for ReturnCopy");
    }
}
