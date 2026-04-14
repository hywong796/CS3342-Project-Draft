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
        
        Main.setCurrentBookCopy(foundCopy.get());
        System.out.println("Current book copy set to: Copy ID " + copyID + " (ISBN: " + foundCopy.get().getIsbn() + ")");
        addUndoCommand(this);
        clearRedoList();
    }

    @Override
    public void undoMe() {
        System.out.println("Undo functionality not yet implemented for intoCurrent");
    }

    @Override
    public void redoMe() {
        System.out.println("Redo functionality not yet implemented for intoCurrent");
    }
}
