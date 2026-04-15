package command;

import main.Main;
import data.Library;
import data.Library.LibraryFields;
import data.BookRecord;
import data.BookRecord.RecordFields;
import data.BookCopy;
import data.BookCopy.CopyFields;
import data.LibraryNetwork;
import utilities.Searching;

import java.util.Optional;
import java.util.Scanner;

public class EditData extends RecordedCommand {
    
    private static Scanner scanner = new Scanner(System.in);

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: EDIT [COPY|RECORD|LIBRARY]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "COPY" -> editCopy();
            case "RECORD" -> editRecord();
            case "LIBRARY" -> editLibrary();
            default -> System.out.println("Invalid option. Please use COPY, RECORD, or LIBRARY");
        }
    }

    private void editCopy() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Edit Book Copy ===");
        
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
        
        // Display current copy info
        System.out.println("\n## Current Copy Information:");
        System.out.println("- ISBN: " + selectedCopy.getIsbn());
        System.out.println("- Copy ID: " + selectedCopy.getCopyID());
        System.out.println("- Status: " + selectedCopy.getStatus());
        System.out.println("- Acquisition Date: " + selectedCopy.getAcquisitionDate());
        System.out.println("- Acquisition Price: " + selectedCopy.getAcquisitionPrice());
        
        // Display editable fields
        System.out.println("\n# Editable Fields:");
        System.out.println("- [1] Status");
        System.out.println("- [2] Acquisition Date");
        System.out.println("- [3] Acquisition Price");
        
        System.out.print("\nSelect field to edit (1-3): ");
        String choice = scanner.nextLine().trim();
        
        String newValue;
        CopyFields fieldToEdit;
        
        switch (choice) {
            case "1":
                fieldToEdit = CopyFields.STATUS;
                System.out.println("Available statuses: AVAILABLE, BORROWED, PROCESSING, DAMAGED, LOST");
                System.out.print("Enter new status: ");
                newValue = scanner.nextLine().trim().toUpperCase();
                break;
            case "2":
                fieldToEdit = CopyFields.ACQUISITION_DATE;
                System.out.print("Enter new acquisition date (yyyy-MM-dd): ");
                newValue = scanner.nextLine().trim();
                break;
            case "3":
                fieldToEdit = CopyFields.ACQUISITION_PRICE;
                System.out.print("Enter new acquisition price: ");
                newValue = scanner.nextLine().trim();
                break;
            default:
                System.out.println("Invalid option");
                return;
        }
        
        // Update the copy
        if (library.editCopy(selectedCopy, fieldToEdit, newValue)) {
            System.out.println("✓ Book copy updated successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to update book copy. Please check the details and try again.");
        }
    }

    private void editRecord() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Edit Book Record ===");
        
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
        
        // Display current record info
        System.out.println("\n## Current Record Information:");
        System.out.println("- ISBN: " + selectedRecord.getIsbn());
        System.out.println("- Title: " + selectedRecord.getTitle());
        System.out.println("- Author: " + selectedRecord.getAuthor());
        System.out.println("- Language: " + selectedRecord.getLanguage());
        System.out.println("- Category: " + selectedRecord.getCategory());
        System.out.println("- Publishing Year: " + selectedRecord.getPublishingYear());
        
        // Display editable fields
        System.out.println("\n# Editable Fields:");
        System.out.println("- [1] Title");
        System.out.println("- [2] Author");
        System.out.println("- [3] Language");
        System.out.println("- [4] Category");
        System.out.println("- [5] Publishing Year");
        
        System.out.print("\nSelect field to edit (1-5): ");
        String choice = scanner.nextLine().trim();
        
        String newValue;
        RecordFields fieldToEdit;
        
        switch (choice) {
            case "1":
                fieldToEdit = RecordFields.TITLE;
                System.out.print("Enter new title: ");
                newValue = scanner.nextLine().trim();
                break;
            case "2":
                fieldToEdit = RecordFields.AUTHOR;
                System.out.print("Enter new author: ");
                newValue = scanner.nextLine().trim();
                break;
            case "3":
                fieldToEdit = RecordFields.LANGUAGE;
                System.out.print("Enter new language: ");
                newValue = scanner.nextLine().trim();
                break;
            case "4":
                fieldToEdit = RecordFields.CATEGORY;
                System.out.print("Enter new category: ");
                newValue = scanner.nextLine().trim();
                break;
            case "5":
                fieldToEdit = RecordFields.PUBLISHING_YEAR;
                System.out.print("Enter new publishing year (yyyy): ");
                newValue = scanner.nextLine().trim();
                break;
            default:
                System.out.println("Invalid option");
                return;
        }
        
        // Update the record
        if (library.editRecord(selectedRecord, fieldToEdit, newValue)) {
            System.out.println("✓ Book record updated successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to update book record. Please check the details and try again.");
        }
    }

    private void editLibrary() {
        System.out.println("=== Edit Library ===");
        
        // Get Library ID
        System.out.print("Enter Library ID: ");
        String libraryID = scanner.nextLine().trim().toUpperCase();
        
        Optional<Library> targetLibrary = Searching.searchSingle(LibraryNetwork.getInstance(), LibraryFields.LIBRARY_ID, libraryID);
        if (targetLibrary.isEmpty()) {
            System.out.println("Library not found with ID: " + libraryID);
            return;
        }
        
        Library selectedLibrary = targetLibrary.get();
        
        // Display current library info
        System.out.println("\n## Current Library Information:");
        System.out.println("- Library ID: " + selectedLibrary.getLibraryID());
        System.out.println("- Name: " + selectedLibrary.getName());
        System.out.println("- Address: " + selectedLibrary.getAddress());
        System.out.println("- Phone: " + selectedLibrary.getPhone());
        System.out.println("- Email: " + selectedLibrary.getEmail());
        
        // Display editable fields
        System.out.println("\n# Editable Fields:");
        System.out.println("- [1] Name");
        System.out.println("- [2] Address");
        System.out.println("- [3] Phone");
        System.out.println("- [4] Email");
        
        System.out.print("\nSelect field to edit (1-4): ");
        String choice = scanner.nextLine().trim();
        
        String newValue;
        LibraryFields fieldToEdit;
        
        switch (choice) {
            case "1":
                fieldToEdit = LibraryFields.NAME;
                System.out.print("Enter new name: ");
                newValue = scanner.nextLine().trim();
                break;
            case "2":
                fieldToEdit = LibraryFields.ADDRESS;
                System.out.print("Enter new address: ");
                newValue = scanner.nextLine().trim();
                break;
            case "3":
                fieldToEdit = LibraryFields.PHONE;
                System.out.print("Enter new phone: ");
                newValue = scanner.nextLine().trim();
                break;
            case "4":
                fieldToEdit = LibraryFields.EMAIL;
                System.out.print("Enter new email: ");
                newValue = scanner.nextLine().trim();
                break;
            default:
                System.out.println("Invalid option");
                return;
        }
        
        // Update the library
        if (selectedLibrary.setField(fieldToEdit, newValue)) {
            System.out.println("✓ Library updated successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to update library. Please check the details and try again.");
        }
    }

    @Override
    public void undoMe() {
        System.out.println("Undo functionality not yet implemented for EditData");
    }

    @Override
    public void redoMe() {
        System.out.println("Redo functionality not yet implemented for EditData");
    }
}
