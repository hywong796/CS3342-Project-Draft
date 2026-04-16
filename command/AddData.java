package command;


import main.Main;
import data.BookCopy;
import data.BookRecord;
import data.Library;
import data.Library.LibraryFields;
import data.LibraryNetwork;
import utilities.Searching;


import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;


public class AddData extends RecordedCommand {

    private static Scanner scanner = new Scanner(System.in);
    private static LibraryNetwork libraryNetwork = new LibraryNetwork();


    // ─── Undo/Redo State ──────────────────────────────────────────────────────

    private enum ActionType { COPY, RECORD, LIBRARY }
    private ActionType lastAction;

    // COPY
    private Library   copyTargetLib;
    private BookCopy  addedCopy;
    private String    savedIsbn;
    private LocalDate savedAcquisitionDate;
    private double    savedAcquisitionPrice;

    // RECORD
    private Library    recordTargetLib;
    private BookRecord addedRecord;
    private String     savedRecordIsbn;
    private String     savedTitle;
    private String     savedAuthor;
    private String     savedLanguage;
    private String     savedCategory;
    private Year       savedPublishingYear;

    // LIBRARY
    private Library addedLibrary;
    private String  savedLibID;
    private String  savedLibName;
    private String  savedLibAddress;
    private String  savedLibEmail;


    // ─── execute() ────────────────────────────────────────────────────────────

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: ADD [COPY|RECORD|LIBRARY]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "COPY"    -> addCopy();
            case "RECORD"  -> addRecord();
            case "LIBRARY" -> addLibrary();
            default -> System.out.println("Invalid option. Please use COPY, RECORD, or LIBRARY");
        }
    }


    // ─── addCopy() ────────────────────────────────────────────────────────────

    private void addCopy() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }
        System.out.println("=== Add Book Copy ===");

        Library currentLib = Main.getCurrentLibrary();
        if (currentLib == null) {
            System.out.print("Enter Library ID: ");
            String libraryID = scanner.nextLine().trim().toUpperCase();

            Optional<Library> foundLibrary = Searching.searchSingle(
                LibraryNetwork.getInstance(), LibraryFields.LIBRARY_ID, libraryID);
            if (foundLibrary.isEmpty()) {
                System.out.println("Library not found with ID: " + libraryID);
                return;
            }
            currentLib = foundLibrary.get();
        } else {
            System.out.println("Using current library: " + currentLib.getName());
        }

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();

        System.out.print("Enter Acquisition Date (yyyy-MM-dd): ");
        LocalDate acquisitionDate;
        try {
            acquisitionDate = LocalDate.parse(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd");
            return;
        }

        System.out.print("Enter Acquisition Price: ");
        double acquisitionPrice;
        try {
            acquisitionPrice = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format. Please enter a valid number");
            return;
        }

        if (currentLib.addCopy(isbn, acquisitionDate, acquisitionPrice)) {
            // ── capture undo state ────────────────────────────────────────
            lastAction            = ActionType.COPY;
            copyTargetLib         = currentLib;
            ArrayList<BookCopy> copies = currentLib.getBookCopyCollection();
            addedCopy             = copies.get(copies.size() - 1);
            savedIsbn             = isbn;
            savedAcquisitionDate  = acquisitionDate;
            savedAcquisitionPrice = acquisitionPrice;
            // ─────────────────────────────────────────────────────────────
            System.out.println("Book copy added successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to add book copy. Please check the details and try again.");
        }
    }


    // ─── addRecord() ──────────────────────────────────────────────────────────

    private void addRecord() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }
        System.out.println("=== Add Book Record ===");

        Library currentLib = Main.getCurrentLibrary();
        if (currentLib == null) {
            System.out.print("Enter Library ID: ");
            String libraryID = scanner.nextLine().trim();

            Optional<Library> foundLibrary = findLibraryByID(libraryID);
            if (foundLibrary.isEmpty()) {
                System.out.println("Library not found with ID: " + libraryID);
                return;
            }
            currentLib = foundLibrary.get();
        } else {
            System.out.println("Using current library: " + currentLib.getName());
        }

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Enter Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Enter Language: ");
        String language = scanner.nextLine().trim();
        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();

        System.out.print("Enter Publishing Year (yyyy): ");
        Year publishingYear;
        try {
            publishingYear = Year.parse(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid year format. Please enter a valid year (yyyy)");
            return;
        }

        if (currentLib.addRecord(isbn, title, author, language, category, publishingYear)) {
            // ── capture undo state ────────────────────────────────────────
            lastAction          = ActionType.RECORD;
            recordTargetLib     = currentLib;
            ArrayList<BookRecord> records = currentLib.getBookRecordCollection();
            addedRecord         = records.get(records.size() - 1);
            savedRecordIsbn     = isbn;
            savedTitle          = title;
            savedAuthor         = author;
            savedLanguage       = language;
            savedCategory       = category;
            savedPublishingYear = publishingYear;
            // ─────────────────────────────────────────────────────────────
            System.out.println("Book record added successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to add book record. Please check the details and try again.");
        }
    }


    // ─── addLibrary() ─────────────────────────────────────────────────────────

    private void addLibrary() {
        System.out.println("=== Add Library ===");

        System.out.print("Enter Library ID: ");
        String libraryID = scanner.nextLine().trim();
        System.out.print("Enter Library Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Address: ");
        String address = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();

        if (libraryNetwork.addLibrary(libraryID, name, address, email)) {
            // ── capture undo state ────────────────────────────────────────
            lastAction      = ActionType.LIBRARY;
            ArrayList<Library> libs = LibraryNetwork.getInstance();
            addedLibrary    = libs.get(libs.size() - 1);
            savedLibID      = libraryID;
            savedLibName    = name;
            savedLibAddress = address;
            savedLibEmail   = email;
            // ─────────────────────────────────────────────────────────────
            System.out.println("Library added successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to add library. Please check the details and try again.");
        }
    }


    // ─── findLibraryByID() ────────────────────────────────────────────────────

    private Optional<Library> findLibraryByID(String libraryID) {
        return LibraryNetwork.getInstance().stream()
            .filter(lib -> lib.getLibraryID().equals(libraryID))
            .findFirst();
    }


    // ─── undoMe() ─────────────────────────────────────────────────────────────

    @Override
    public void undoMe() {
        if (lastAction == null) {
            System.out.println("Nothing to undo for AddData.");
            return;
        }
        switch (lastAction) {
            case COPY -> {
                // Step 1: Remove the physical copy from the library's collection.
                copyTargetLib.removeCopy(addedCopy);

                // Step 2: Library.removeCopy() does not update BookRecord counters
                // on its own, so we decrement them manually here.
                Searching.searchSingle(
                    copyTargetLib.getBookRecordCollection(),
                    BookRecord.RecordFields.ISBN,
                    addedCopy.getIsbn()
                ).ifPresent(record -> record.removeCopy());

                System.out.println("Undo: Removed book copy ["
                    + addedCopy.getCopyID() + "] from \""
                    + copyTargetLib.getName() + "\".");
            }
            case RECORD -> {
                // NOTE: Library.removeRecord() has a known bug — it returns false
                // when the record has 0 copies (which is always the case for a
                // freshly added record that has not yet had copies assigned).
                // We therefore remove directly from the record collection to
                // bypass that guard correctly.
                recordTargetLib.getBookRecordCollection().remove(addedRecord);

                System.out.println("Undo: Removed book record ["
                    + addedRecord.getIsbn() + "] from \""
                    + recordTargetLib.getName() + "\".");
            }
            case LIBRARY -> {
                // Remove the library directly from the network's list.
                LibraryNetwork.getInstance().remove(addedLibrary);

                System.out.println("Undo: Removed library \""
                    + addedLibrary.getName() + "\".");
            }
        }
    }


    // ─── redoMe() ─────────────────────────────────────────────────────────────

    @Override
    public void redoMe() {
        if (lastAction == null) {
            System.out.println("Nothing to redo for AddData.");
            return;
        }
        switch (lastAction) {
            case COPY -> {
                // Re-add the copy using the originally captured input data.
                // The new copy will receive the next sequential copyID because
                // BookRecord's copyCounter is monotonically increasing and is
                // never decremented when a copy is removed — this is by design.
                if (copyTargetLib.addCopy(savedIsbn, savedAcquisitionDate, savedAcquisitionPrice)) {
                    ArrayList<BookCopy> copies = copyTargetLib.getBookCopyCollection();
                    addedCopy = copies.get(copies.size() - 1); // refresh reference for next undo
                    System.out.println("Redo: Re-added book copy for ISBN ["
                        + savedIsbn + "] to \""
                        + copyTargetLib.getName() + "\".");
                } else {
                    System.out.println("Redo failed: could not re-add book copy for ISBN [" + savedIsbn + "].");
                }
            }
            case RECORD -> {
                if (recordTargetLib.addRecord(savedRecordIsbn, savedTitle, savedAuthor,
                        savedLanguage, savedCategory, savedPublishingYear)) {
                    ArrayList<BookRecord> records = recordTargetLib.getBookRecordCollection();
                    addedRecord = records.get(records.size() - 1); // refresh reference for next undo
                    System.out.println("Redo: Re-added book record ["
                        + savedRecordIsbn + "] to \""
                        + recordTargetLib.getName() + "\".");
                } else {
                    System.out.println("Redo failed: could not re-add book record [" + savedRecordIsbn + "].");
                }
            }
            case LIBRARY -> {
                if (libraryNetwork.addLibrary(savedLibID, savedLibName, savedLibAddress, savedLibEmail)) {
                    ArrayList<Library> libs = LibraryNetwork.getInstance();
                    addedLibrary = libs.get(libs.size() - 1); // refresh reference for next undo
                    System.out.println("Redo: Re-added library \""
                        + savedLibName + "\".");
                } else {
                    System.out.println("Redo failed: could not re-add library \"" + savedLibName + "\".");
                }
            }
        }
    }

}