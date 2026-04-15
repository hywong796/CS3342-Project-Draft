package command;

import main.Main;
import data.Library;
import data.Library.LibraryFields;
import data.BookCopy;
import data.LibraryNetwork;
import utilities.Searching;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ReturnCopy extends RecordedCommand {
    
    private static Scanner scanner = new Scanner(System.in);

    @Override
    public void execute(String[] commandParts) {
        returnCopyByBorrowerID();
    }

    private void returnCopyByBorrowerID() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Return Book Copy ===");
        
        // Get current library or prompt for Library ID
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
        
        // Get borrower ID
        System.out.print("Enter Borrower ID: ");
        String borrowerID = scanner.nextLine().trim().toUpperCase();
        
        if (borrowerID.isEmpty()) {
            System.out.println("Borrower ID cannot be empty");
            return;
        }
        
        // Search for borrowed copies by this borrower
        List<BookCopy> borrowedByUser = targetLibrary.getBookCopyCollection().stream()
            .filter(copy -> BookCopy.Status.isBorrowed(copy) && 
                           copy.getBorrowerID() != null && 
                           copy.getBorrowerID().equalsIgnoreCase(borrowerID))
            .toList();
        
        if (borrowedByUser.isEmpty()) {
            System.out.println("No borrowed copies found for Borrower ID: " + borrowerID);
            return;
        }
        
        // Display borrowed copies
        System.out.println("\n# Borrowed Books for " + borrowerID + " (" + borrowedByUser.size() + " total):");
        for (int i = 0; i < borrowedByUser.size(); i++) {
            BookCopy copy = borrowedByUser.get(i);
            
            // Get book record details
            Optional<data.BookRecord> record = Searching.searchSingle(
                targetLibrary.getBookRecordCollection(),
                data.BookRecord.RecordFields.ISBN,
                copy.getIsbn()
            );
            
            String title = "Unknown Title";
            if (record.isPresent()) {
                title = (String) record.get().getField(data.BookRecord.RecordFields.TITLE);
            }
            
            System.out.println("- [" + (i + 1) + "] " + title + " | Copy ID: " + copy.getCopyID() + 
                             " | Last Borrowed: " + copy.getLastBorrowingDate());
        }
        
        // Get copy selection
        System.out.print("\nSelect copy number to return: ");
        int copyIndex;
        try {
            copyIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (copyIndex < 0 || copyIndex >= borrowedByUser.size()) {
                System.out.println("Invalid selection");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number");
            return;
        }
        
        BookCopy selectedCopy = borrowedByUser.get(copyIndex);
        
        // Return the copy
        if (targetLibrary.returnCopy(selectedCopy)) {
            System.out.println("✓ Book copy returned successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to return book copy. Please check the details and try again.");
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
