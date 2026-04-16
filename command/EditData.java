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


    // ─── Undo/Redo State ──────────────────────────────────────────────────────

    private enum ActionType { COPY, RECORD, LIBRARY }
    private ActionType lastAction;

    // COPY edit
    private BookCopy   editedCopy;
    private CopyFields copyField;

    // RECORD edit
    private BookRecord   editedRecord;
    private RecordFields recordField;

    // LIBRARY edit
    private Library       editedLibrary;
    private LibraryFields libraryField;

    // Shared — old value captured BEFORE edit, new value captured AFTER edit
    private Object oldValue;
    private Object newValue;


    // ─── execute() ────────────────────────────────────────────────────────────

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: EDIT [COPY|RECORD|LIBRARY]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "COPY"    -> editCopy();
            case "RECORD"  -> editRecord();
            case "LIBRARY" -> editLibrary();
            default -> System.out.println("Invalid option. Please use COPY, RECORD, or LIBRARY");
        }
    }


    // ─── editCopy() ───────────────────────────────────────────────────────────

    private void editCopy() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Edit Book Copy ===");

        Library targetLibrary = Main.getCurrentLibrary();
        if (targetLibrary == null) {
            System.out.print("Enter Library ID: ");
            String libraryID = scanner.nextLine().trim().toUpperCase();

            Optional<Library> foundLibrary = Searching.searchSingle(
                LibraryNetwork.getInstance(), LibraryFields.LIBRARY_ID, libraryID);
            if (foundLibrary.isEmpty()) {
                System.out.println("Library not found with ID: " + libraryID);
                return;
            }
            targetLibrary = foundLibrary.get();
        }

        final Library library = targetLibrary;

        System.out.print("Enter Copy ID: ");
        String copyID = scanner.nextLine().trim().toUpperCase();

        Optional<BookCopy> targetCopy = library.getBookCopyCollection().stream()
            .filter(copy -> copy.getCopyID().equalsIgnoreCase(copyID))
            .findFirst();

        if (targetCopy.isEmpty()) {
            System.out.println("Copy not found with ID: " + copyID);
            return;
        }

        BookCopy selectedCopy = targetCopy.get();

        System.out.println("\n## Current Copy Information:");
        System.out.println("- ISBN: "             + selectedCopy.getIsbn());
        System.out.println("- Copy ID: "          + selectedCopy.getCopyID());
        System.out.println("- Status: "           + selectedCopy.getStatus());
        System.out.println("- Acquisition Date: " + selectedCopy.getAcquisitionDate());
        System.out.println("- Acquisition Price: "+ selectedCopy.getAcquisitionPrice());

        System.out.println("\n# Editable Fields:");
        System.out.println("- [1] Status");
        System.out.println("- [2] Acquisition Date");
        System.out.println("- [3] Acquisition Price");

        System.out.print("\nSelect field to edit (1-3): ");
        String choice = scanner.nextLine().trim();

        String newValueStr;
        CopyFields fieldToEdit;

        switch (choice) {
            case "1" -> {
                fieldToEdit = CopyFields.STATUS;
                System.out.println("Available statuses: AVAILABLE, BORROWED, PROCESSING, DAMAGED, LOST");
                System.out.print("Enter new status: ");
                newValueStr = scanner.nextLine().trim().toUpperCase();
            }
            case "2" -> {
                fieldToEdit = CopyFields.ACQUISITION_DATE;
                System.out.print("Enter new acquisition date (yyyy-MM-dd): ");
                newValueStr = scanner.nextLine().trim();
            }
            case "3" -> {
                fieldToEdit = CopyFields.ACQUISITION_PRICE;
                System.out.print("Enter new acquisition price: ");
                newValueStr = scanner.nextLine().trim();
            }
            default -> {
                System.out.println("Invalid option");
                return;
            }
        }

        // ── capture old value BEFORE edit ─────────────────────────────────────
        Object capturedOldValue = selectedCopy.getField(fieldToEdit);
        // ─────────────────────────────────────────────────────────────────────

        if (library.editCopy(selectedCopy, fieldToEdit, newValueStr)) {
            // ── capture new value AFTER successful edit ───────────────────────
            this.lastAction  = ActionType.COPY;
            this.editedCopy  = selectedCopy;
            this.copyField   = fieldToEdit;
            this.oldValue    = capturedOldValue;
            this.newValue    = selectedCopy.getField(fieldToEdit);
            // ─────────────────────────────────────────────────────────────────
            System.out.println("✓ Book copy updated successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to update book copy. Please check the details and try again.");
        }
    }


    // ─── editRecord() ─────────────────────────────────────────────────────────

    private void editRecord() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Edit Book Record ===");

        Library targetLibrary = Main.getCurrentLibrary();
        if (targetLibrary == null) {
            System.out.print("Enter Library ID: ");
            String libraryID = scanner.nextLine().trim().toUpperCase();

            Optional<Library> foundLibrary = Searching.searchSingle(
                LibraryNetwork.getInstance(), LibraryFields.LIBRARY_ID, libraryID);
            if (foundLibrary.isEmpty()) {
                System.out.println("Library not found with ID: " + libraryID);
                return;
            }
            targetLibrary = foundLibrary.get();
        }

        final Library library = targetLibrary;

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();

        Optional<BookRecord> targetRecord = library.getBookRecordCollection().stream()
            .filter(record -> record.getIsbn().equals(isbn))
            .findFirst();

        if (targetRecord.isEmpty()) {
            System.out.println("Record not found with ISBN: " + isbn);
            return;
        }

        BookRecord selectedRecord = targetRecord.get();

        System.out.println("\n## Current Record Information:");
        System.out.println("- ISBN: "            + selectedRecord.getIsbn());
        System.out.println("- Title: "           + selectedRecord.getTitle());
        System.out.println("- Author: "          + selectedRecord.getAuthor());
        System.out.println("- Language: "        + selectedRecord.getLanguage());
        System.out.println("- Category: "        + selectedRecord.getCategory());
        System.out.println("- Publishing Year: " + selectedRecord.getPublishingYear());

        System.out.println("\n# Editable Fields:");
        System.out.println("- [1] Title");
        System.out.println("- [2] Author");
        System.out.println("- [3] Language");
        System.out.println("- [4] Category");
        System.out.println("- [5] Publishing Year");

        System.out.print("\nSelect field to edit (1-5): ");
        String choice = scanner.nextLine().trim();

        String newValueStr;
        RecordFields fieldToEdit;

        switch (choice) {
            case "1" -> {
                fieldToEdit = RecordFields.TITLE;
                System.out.print("Enter new title: ");
                newValueStr = scanner.nextLine().trim();
            }
            case "2" -> {
                fieldToEdit = RecordFields.AUTHOR;
                System.out.print("Enter new author: ");
                newValueStr = scanner.nextLine().trim();
            }
            case "3" -> {
                fieldToEdit = RecordFields.LANGUAGE;
                System.out.print("Enter new language: ");
                newValueStr = scanner.nextLine().trim();
            }
            case "4" -> {
                fieldToEdit = RecordFields.CATEGORY;
                System.out.print("Enter new category: ");
                newValueStr = scanner.nextLine().trim();
            }
            case "5" -> {
                fieldToEdit = RecordFields.PUBLISHING_YEAR;
                System.out.print("Enter new publishing year (yyyy): ");
                newValueStr = scanner.nextLine().trim();
            }
            default -> {
                System.out.println("Invalid option");
                return;
            }
        }

        // ── capture old value BEFORE edit ─────────────────────────────────────
        Object capturedOldValue = selectedRecord.getField(fieldToEdit);
        // ─────────────────────────────────────────────────────────────────────

        if (library.editRecord(selectedRecord, fieldToEdit, newValueStr)) {
            // ── capture new value AFTER successful edit ───────────────────────
            this.lastAction   = ActionType.RECORD;
            this.editedRecord = selectedRecord;
            this.recordField  = fieldToEdit;
            this.oldValue     = capturedOldValue;
            this.newValue     = selectedRecord.getField(fieldToEdit);
            // ─────────────────────────────────────────────────────────────────
            System.out.println("✓ Book record updated successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to update book record. Please check the details and try again.");
        }
    }


    // ─── editLibrary() ────────────────────────────────────────────────────────

    private void editLibrary() {
        System.out.println("=== Edit Library ===");

        System.out.print("Enter Library ID: ");
        String libraryID = scanner.nextLine().trim().toUpperCase();

        Optional<Library> targetLibrary = Searching.searchSingle(
            LibraryNetwork.getInstance(), LibraryFields.LIBRARY_ID, libraryID);
        if (targetLibrary.isEmpty()) {
            System.out.println("Library not found with ID: " + libraryID);
            return;
        }

        Library selectedLibrary = targetLibrary.get();

        System.out.println("\n## Current Library Information:");
        System.out.println("- Library ID: " + selectedLibrary.getLibraryID());
        System.out.println("- Name: "       + selectedLibrary.getName());
        System.out.println("- Address: "    + selectedLibrary.getAddress());
        System.out.println("- Phone: "      + selectedLibrary.getPhone());
        System.out.println("- Email: "      + selectedLibrary.getEmail());

        System.out.println("\n# Editable Fields:");
        System.out.println("- [1] Name");
        System.out.println("- [2] Address");
        System.out.println("- [3] Phone");
        System.out.println("- [4] Email");

        System.out.print("\nSelect field to edit (1-4): ");
        String choice = scanner.nextLine().trim();

        String newValueStr;
        LibraryFields fieldToEdit;

        switch (choice) {
            case "1" -> {
                fieldToEdit = LibraryFields.NAME;
                System.out.print("Enter new name: ");
                newValueStr = scanner.nextLine().trim();
            }
            case "2" -> {
                fieldToEdit = LibraryFields.ADDRESS;
                System.out.print("Enter new address: ");
                newValueStr = scanner.nextLine().trim();
            }
            case "3" -> {
                fieldToEdit = LibraryFields.PHONE;
                System.out.print("Enter new phone: ");
                newValueStr = scanner.nextLine().trim();
            }
            case "4" -> {
                fieldToEdit = LibraryFields.EMAIL;
                System.out.print("Enter new email: ");
                newValueStr = scanner.nextLine().trim();
            }
            default -> {
                System.out.println("Invalid option");
                return;
            }
        }

        // ── capture old value BEFORE edit ─────────────────────────────────────
        Object capturedOldValue = selectedLibrary.getField(fieldToEdit);
        // ─────────────────────────────────────────────────────────────────────

        if (selectedLibrary.setField(fieldToEdit, newValueStr)) {
            // ── capture new value AFTER successful edit ───────────────────────
            this.lastAction    = ActionType.LIBRARY;
            this.editedLibrary = selectedLibrary;
            this.libraryField  = fieldToEdit;
            this.oldValue      = capturedOldValue;
            this.newValue      = selectedLibrary.getField(fieldToEdit);
            // ─────────────────────────────────────────────────────────────────
            System.out.println("✓ Library updated successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to update library. Please check the details and try again.");
        }
    }


    // ─── undoMe() ─────────────────────────────────────────────────────────────

    @Override
    public void undoMe() {
        if (lastAction == null) {
            System.out.println("Nothing to undo for EditData.");
            return;
        }

        // Restore the field to its value BEFORE the edit
        switch (lastAction) {
            case COPY    -> editedCopy.setField(copyField, oldValue);
            case RECORD  -> editedRecord.setField(recordField, oldValue);
            case LIBRARY -> editedLibrary.setField(libraryField, oldValue);
        }

        System.out.println("Undo: [" + lastAction + "] field ["
            + getFieldName() + "] restored from ["
            + newValue + "] back to [" + oldValue + "].");
    }


    // ─── redoMe() ─────────────────────────────────────────────────────────────

    @Override
    public void redoMe() {
        if (lastAction == null) {
            System.out.println("Nothing to redo for EditData.");
            return;
        }

        // Re-apply the field to its value AFTER the original edit
        switch (lastAction) {
            case COPY    -> editedCopy.setField(copyField, newValue);
            case RECORD  -> editedRecord.setField(recordField, newValue);
            case LIBRARY -> editedLibrary.setField(libraryField, newValue);
        }

        System.out.println("Redo: [" + lastAction + "] field ["
            + getFieldName() + "] changed from ["
            + oldValue + "] back to [" + newValue + "].");
    }


    // ─── Helper ───────────────────────────────────────────────────────────────

    private String getFieldName() {
        if (lastAction == null) return "unknown";
        return switch (lastAction) {
            case COPY    -> copyField.toString();
            case RECORD  -> recordField.toString();
            case LIBRARY -> libraryField.toString();
        };
    }
}