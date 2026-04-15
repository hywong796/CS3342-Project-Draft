package command;

import main.Main;
import data.Library;
import data.Library.LibraryFields;
import data.BookRecord;
import data.BookCopy;
import data.LibraryNetwork;
import utilities.Searching;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class RemoveData extends RecordedCommand {
    
    private static Scanner scanner = new Scanner(System.in);

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: REMOVE [COPY|RECORD|LIBRARY]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "COPY" -> removeCopy();
            case "RECORD" -> removeRecord();
            case "LIBRARY" -> removeLibrary();
            default -> System.out.println("Invalid option. Please use COPY, RECORD, or LIBRARY");
        }
    }

    private void removeCopy() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Remove Book Copy ===");
        
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
        
        // Create final reference for lambda
        final Library library = targetLibrary;
        
        // Get Copy ID
        System.out.print("Enter Copy ID: ");
        String copyID = scanner.nextLine().trim().toUpperCase();
        
        // Search for the copy
        Optional<BookCopy> targetCopy = library.getBookCopyCollection().stream()
            .filter(copy -> copy.getCopyID().equalsIgnoreCase(copyID))
            .findFirst();
        
        if (targetCopy.isEmpty()) {
            System.out.println("Copy not found with ID: " + copyID);
            return;
        }
        
        BookCopy selectedCopy = targetCopy.get();
        
        // Display copy info for confirmation
        System.out.println("\n## Copy Information:");
        System.out.println("- ISBN: " + selectedCopy.getIsbn());
        System.out.println("- Copy ID: " + selectedCopy.getCopyID());
        System.out.println("- Status: " + selectedCopy.getStatus());
        System.out.println("- Acquisition Date: " + selectedCopy.getAcquisitionDate());
        
        // Confirm removal
        System.out.print("\nAre you sure you want to remove this copy? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (!confirmation.equals("yes")) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        // Remove the copy
        if (library.removeCopy(selectedCopy)) {
            System.out.println("✓ Book copy removed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to remove book copy. Please check the details and try again.");
        }
    }

    private void removeRecord() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Remove Book Record ===");
        
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
        
        // Create final reference for lambda
        final Library library = targetLibrary;
        
        // Get ISBN
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();
        
        // Search for the record
        Optional<BookRecord> targetRecord = library.getBookRecordCollection().stream()
            .filter(record -> record.getIsbn().equals(isbn))
            .findFirst();
        
        if (targetRecord.isEmpty()) {
            System.out.println("Record not found with ISBN: " + isbn);
            return;
        }
        
        BookRecord selectedRecord = targetRecord.get();
        
        // Count copies associated with this record
        List<BookCopy> associatedCopies = library.getBookCopyCollection().stream()
            .filter(copy -> copy.getIsbn().equals(isbn))
            .toList();
        
        // Display record info for confirmation
        System.out.println("\n## Book Record Information:");
        System.out.println("- ISBN: " + selectedRecord.getIsbn());
        System.out.println("- Title: " + selectedRecord.getTitle());
        System.out.println("- Author: " + selectedRecord.getAuthor());
        System.out.println("- Associated Copies: " + associatedCopies.size());
        
        if (!associatedCopies.isEmpty()) {
            System.out.println("\n# This will also remove the following copies:");
            for (int i = 0; i < associatedCopies.size(); i++) {
                BookCopy copy = associatedCopies.get(i);
                System.out.println("- [" + (i + 1) + "] Copy ID: " + copy.getCopyID() + " | Status: " + copy.getStatus());
            }
        }
        
        // Confirm removal
        System.out.print("\nAre you sure you want to remove this record and all associated copies? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (!confirmation.equals("yes")) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        // Remove the record
        if (library.removeRecord(selectedRecord)) {
            System.out.println("✓ Book record and associated copies removed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to remove book record. Please check the details and try again.");
        }
    }

    private void removeLibrary() {
        System.out.println("=== Remove Library ===");
        
        // Get Library ID
        System.out.print("Enter Library ID: ");
        String libraryID = scanner.nextLine().trim().toUpperCase();
        
        Optional<Library> targetLibrary = Searching.searchSingle(LibraryNetwork.getInstance(), LibraryFields.LIBRARY_ID, libraryID);
        if (targetLibrary.isEmpty()) {
            System.out.println("Library not found with ID: " + libraryID);
            return;
        }
        
        Library selectedLibrary = targetLibrary.get();
        
        // Count records and copies
        int recordCount = selectedLibrary.getBookRecordCollection().size();
        int copyCount = selectedLibrary.getBookCopyCollection().size();
        
        // Display library info for confirmation
        System.out.println("\n## Library Information:");
        System.out.println("- Library ID: " + selectedLibrary.getLibraryID());
        System.out.println("- Name: " + selectedLibrary.getName());
        System.out.println("- Address: " + selectedLibrary.getAddress());
        System.out.println("- Records: " + recordCount);
        System.out.println("- Book Copies: " + copyCount);
        
        // Confirm removal
        System.out.print("\n⚠ WARNING: This will remove the entire library and all its data!");
        System.out.print("\nAre you sure? Type the library ID to confirm: ");
        String confirmation = scanner.nextLine().trim().toUpperCase();
        
        if (!confirmation.equals(libraryID)) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        // Remove the library
        ArrayList<Library> librariesToRemove = new ArrayList<>();
        librariesToRemove.add(selectedLibrary);
        
        if (LibraryNetwork.getInstance().removeAll(librariesToRemove)) {
            System.out.println("✓ Library removed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to remove library. Please check the details and try again.");
        }
    }

    @Override
    public void undoMe() {
        System.out.println("Undo functionality not yet implemented for RemoveData");
    }

    @Override
    public void redoMe() {
        System.out.println("Redo functionality not yet implemented for RemoveData");
    }
    
}
