package command;

import data.BookCopy;
import data.BookRecord;
import data.Library;
import utilities.Searching;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RemoveData extends RecordedCommand {
    private final Library library;
    private BookRecord removedRecord;
    private ArrayList<BookCopy> removedCopies;
    private boolean executed;

    public RemoveData(Library library) {
        this.library = library;
    }

    @Override
    public void execute(String[] commandParts) {
        if (commandParts == null || commandParts.length < 2) {
            System.out.println("Usage: remove <ISBN>");
            return;
        }

        String isbn = commandParts[1].trim();
        if (isbn.isEmpty()) {
            System.out.println("Usage: remove <ISBN>");
            return;
        }

        Optional<BookRecord> recordOpt = findRecord(isbn);
        if (recordOpt.isEmpty()) {
            System.out.println("No record matched for ISBN: " + isbn);
            return;
        }

        removedRecord = recordOpt.get();
        removedCopies = findCopies(isbn);

        if (!library.removeRecord(removedRecord)) {
            System.out.println("Failed to remove record for ISBN: " + isbn);
            return;
        }

        clearRedoList();
        addUndoCommand(this);
        executed = true;
        System.out.println("Removed record " + isbn);
    }

    @Override
    public void undoMe() {
        if (!executed || removedRecord == null) {
            System.out.println("Nothing to undo");
            return;
        }

        if (findRecord(removedRecord.getIsbn()).isPresent()) {
            System.out.println("Undo failed: record already exists");
            return;
        }

        if (restoreRecord(removedRecord, removedCopies)) {
            addRedoCommand(this);
            System.out.println("Undo successful: restored record " + removedRecord.getIsbn());
        } else {
            System.out.println("Undo failed");
        }
    }

    @Override
    public void redoMe() {
        if (removedRecord == null) {
            System.out.println("Nothing to redo");
            return;
        }

        Optional<BookRecord> recordOpt = findRecord(removedRecord.getIsbn());
        if (recordOpt.isEmpty()) {
            System.out.println("Redo failed: record not found");
            return;
        }

        if (library.removeRecord(recordOpt.get())) {
            addUndoCommand(this);
            System.out.println("Redo successful: removed record " + removedRecord.getIsbn());
        } else {
            System.out.println("Redo failed");
        }
    }

    private Optional<BookRecord> findRecord(String isbn) {
        ArrayList<BookRecord> recordCollection = getPrivateRecordCollection();
        if (recordCollection == null) return Optional.empty();
        return Searching.searchSingle(recordCollection, BookRecord.RecordFields.ISBN, isbn);
    }

    private ArrayList<BookCopy> findCopies(String isbn) {
        ArrayList<BookCopy> copyCollection = getPrivateCopyCollection();
        if (copyCollection == null) return new ArrayList<>();

        List<BookCopy> copies = Searching.searchMultiple(copyCollection, BookCopy.CopyFields.ISBN, isbn);
        return new ArrayList<>(copies);
    }

    private boolean restoreRecord(BookRecord record, ArrayList<BookCopy> copies) {
        ArrayList<BookRecord> recordCollection = getPrivateRecordCollection();
        ArrayList<BookCopy> copyCollection = getPrivateCopyCollection();
        if (recordCollection == null || copyCollection == null) return false;

        recordCollection.add(record);
        if (copies != null && !copies.isEmpty()) {
            copyCollection.addAll(copies);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<BookRecord> getPrivateRecordCollection() {
        return (ArrayList<BookRecord>) getPrivateField("bookRecordCollection");
    }

    @SuppressWarnings("unchecked")
    private ArrayList<BookCopy> getPrivateCopyCollection() {
        return (ArrayList<BookCopy>) getPrivateField("bookCopyCollection");
    }

    private Object getPrivateField(String fieldName) {
        try {
            Field field = library.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(library);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println("Unable to access library collection: " + fieldName);
            return null;
        }
    }
}
