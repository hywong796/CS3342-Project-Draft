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

public class SelectCurrent extends RecordedCommand {

    private static Scanner scanner = new Scanner(System.in);

    // ─── Undo/Redo State ──────────────────────────────────────────────────────

    private enum ActionType { LIBRARY, RECORD, COPY }
    private ActionType lastAction;

    private Library previousLibrary;
    private Library newLibrary;

    private BookRecord previousRecord;
    private BookRecord newRecord;

    private BookCopy previousCopy;
    private BookCopy newCopy;

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: SELECT [LIBRARY|RECORD|COPY]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "LIBRARY" -> setCurrentLibrary();
            case "RECORD" -> setCurrentRecord();
            case "COPY" -> setCurrentCopy();
            default -> System.out.println("Invalid option. Please use LIBRARY, RECORD, or COPY");
        }
    }

    private void setCurrentLibrary() {
        System.out.println("\n=== Set Current Library ===");

        System.out.print("Enter Library ID: ");
        String libraryID = scanner.nextLine().trim().toUpperCase();

        if (libraryID.isEmpty()) {
            System.out.println("Library ID cannot be empty");
            return;
        }

        Optional<Library> foundLibrary = Searching.searchSingle(
            LibraryNetwork.getInstance(),
            LibraryFields.LIBRARY_ID,
            libraryID
        );

        if (foundLibrary.isEmpty()) {
            System.out.println("Library not found with ID: " + libraryID);
            return;
        }

        // Save undo/redo state
        this.lastAction = ActionType.LIBRARY;
        this.previousLibrary = Main.getCurrentLibrary();
        this.newLibrary = foundLibrary.get();

        Main.setCurrentLibrary(foundLibrary.get());
        System.out.println("Current library set to: " + foundLibrary.get().getName() + " (" + libraryID + ")");
        addUndoCommand(this);
        clearRedoList();
    }

    private void setCurrentRecord() {
        System.out.println("\n=== Set Current Book Record ===");

        Library currentLibrary = Main.getCurrentLibrary();
        if (currentLibrary == null) {
            System.out.println("No library is currently set. Please set a library first using: INTO LIBRARY");
            return;
        }

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();

        if (isbn.isEmpty()) {
            System.out.println("ISBN cannot be empty");
            return;
        }

        Optional<BookRecord> foundRecord = Searching.searchSingle(
            currentLibrary.getBookRecordCollection(),
            RecordFields.ISBN,
            isbn
        );

        if (foundRecord.isEmpty()) {
            System.out.println("Book record not found with ISBN: " + isbn);
            return;
        }

        // Save undo/redo state
        this.lastAction = ActionType.RECORD;
        this.previousRecord = Main.getCurrentBookRecord();
        this.newRecord = foundRecord.get();

        Main.setCurrentBookRecord(foundRecord.get());
        System.out.println("Current book record set to: " + foundRecord.get().getTitle() + " (ISBN: " + isbn + ")");
        addUndoCommand(this);
        clearRedoList();
    }

    private void setCurrentCopy() {
        System.out.println("\n=== Set Current Book Copy ===");

        Library currentLibrary = Main.getCurrentLibrary();
        if (currentLibrary == null) {
            System.out.println("No library is currently set. Please set a library first using: INTO LIBRARY");
            return;
        }

        System.out.print("Enter Copy ID: ");
        String copyID = scanner.nextLine().trim();

        if (copyID.isEmpty()) {
            System.out.println("Copy ID cannot be empty");
            return;
        }

        Optional<BookCopy> foundCopy = Searching.searchSingle(
            currentLibrary.getBookCopyCollection(),
            CopyFields.COPY_ID,
            copyID
        );

        if (foundCopy.isEmpty()) {
            System.out.println("Book copy not found with ID: " + copyID);
            return;
        }

        // Save undo/redo state
        this.lastAction = ActionType.COPY;
        this.previousCopy = Main.getCurrentBookCopy();
        this.newCopy = foundCopy.get();

        Main.setCurrentBookCopy(foundCopy.get());
        System.out.println("Current book copy set to: Copy ID " + copyID + " (ISBN: " + foundCopy.get().getIsbn() + ")");
        addUndoCommand(this);
        clearRedoList();
    }

    @Override
    public void undoMe() {
        if (lastAction == null) {
            System.out.println("Nothing to undo for SelectCurrent");
            return;
        }

        switch (lastAction) {
            case LIBRARY -> {
                Main.setCurrentLibrary(previousLibrary);
                System.out.println("Undo: current library restored.");
            }
            case RECORD -> {
                Main.setCurrentBookRecord(previousRecord);
                System.out.println("Undo: current book record restored.");
            }
            case COPY -> {
                Main.setCurrentBookCopy(previousCopy);
                System.out.println("Undo: current book copy restored.");
            }
        }
    }

    @Override
    public void redoMe() {
        if (lastAction == null) {
            System.out.println("Nothing to redo for SelectCurrent");
            return;
        }

        switch (lastAction) {
            case LIBRARY -> {
                Main.setCurrentLibrary(newLibrary);
                System.out.println("Redo: current library selected again.");
            }
            case RECORD -> {
                Main.setCurrentBookRecord(newRecord);
                System.out.println("Redo: current book record selected again.");
            }
            case COPY -> {
                Main.setCurrentBookCopy(newCopy);
                System.out.println("Redo: current book copy selected again.");
            }
        }
    }
}