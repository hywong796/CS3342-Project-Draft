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


    // ─── Undo/Redo State ──────────────────────────────────────────────────────

    private enum ActionType { COPY, RECORD, LIBRARY }
    private ActionType lastAction;

    // COPY removal
    private Library  copyTargetLibrary;
    private BookCopy removedCopy;

    // RECORD removal (record + all its associated copies removed together)
    private Library          recordTargetLibrary;
    private BookRecord       removedRecord;
    private List<BookCopy>   removedCopies; // snapshot of all copies deleted with the record

    // LIBRARY removal
    private Library removedLibrary;


    // ─── execute() ────────────────────────────────────────────────────────────

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: REMOVE [COPY|RECORD|LIBRARY]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "COPY"    -> removeCopy();
            case "RECORD"  -> removeRecord();
            case "LIBRARY" -> removeLibrary();
            default -> System.out.println("Invalid option. Please use COPY, RECORD, or LIBRARY");
        }
    }


    // ─── removeCopy() ─────────────────────────────────────────────────────────

    private void removeCopy() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Remove Book Copy ===");

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

        System.out.println("\n## Copy Information:");
        System.out.println("- ISBN: "             + selectedCopy.getIsbn());
        System.out.println("- Copy ID: "          + selectedCopy.getCopyID());
        System.out.println("- Status: "           + selectedCopy.getStatus());
        System.out.println("- Acquisition Date: " + selectedCopy.getAcquisitionDate());

        System.out.print("\nAre you sure you want to remove this copy? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("yes")) {
            System.out.println("Operation cancelled.");
            return;
        }

        if (library.removeCopy(selectedCopy)) {
            // ── commit undo state ─────────────────────────────────────────────
            this.lastAction        = ActionType.COPY;
            this.copyTargetLibrary = library;
            this.removedCopy       = selectedCopy;
            // ─────────────────────────────────────────────────────────────────
            System.out.println("✓ Book copy removed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to remove book copy. Please check the details and try again.");
        }
    }


    // ─── removeRecord() ───────────────────────────────────────────────────────

    private void removeRecord() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Remove Book Record ===");

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

        List<BookCopy> associatedCopies = library.getBookCopyCollection().stream()
            .filter(copy -> copy.getIsbn().equals(isbn))
            .toList();

        System.out.println("\n## Book Record Information:");
        System.out.println("- ISBN: "              + selectedRecord.getIsbn());
        System.out.println("- Title: "             + selectedRecord.getTitle());
        System.out.println("- Author: "            + selectedRecord.getAuthor());
        System.out.println("- Associated Copies: " + associatedCopies.size());

        if (!associatedCopies.isEmpty()) {
            System.out.println("\n# This will also remove the following copies:");
            for (int i = 0; i < associatedCopies.size(); i++) {
                BookCopy copy = associatedCopies.get(i);
                System.out.println("- [" + (i + 1) + "] Copy ID: "
                    + copy.getCopyID() + " | Status: " + copy.getStatus());
            }
        }

        System.out.print("\nAre you sure you want to remove this record and all associated copies? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("yes")) {
            System.out.println("Operation cancelled.");
            return;
        }

        // ── snapshot the copies BEFORE removal (new list so the references survive) ──
        List<BookCopy> copiesSnapshot = new ArrayList<>(associatedCopies);
        // ─────────────────────────────────────────────────────────────────────────────

        if (library.removeRecord(selectedRecord)) {
            // ── commit undo state ─────────────────────────────────────────────
            this.lastAction           = ActionType.RECORD;
            this.recordTargetLibrary  = library;
            this.removedRecord        = selectedRecord;
            this.removedCopies        = copiesSnapshot;
            // ─────────────────────────────────────────────────────────────────
            System.out.println("✓ Book record and associated copies removed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to remove book record. Please check the details and try again.");
        }
    }


    // ─── removeLibrary() ──────────────────────────────────────────────────────

    private void removeLibrary() {
        System.out.println("=== Remove Library ===");

        System.out.print("Enter Library ID: ");
        String libraryID = scanner.nextLine().trim().toUpperCase();

        Optional<Library> targetLibrary = Searching.searchSingle(
            LibraryNetwork.getInstance(), LibraryFields.LIBRARY_ID, libraryID);
        if (targetLibrary.isEmpty()) {
            System.out.println("Library not found with ID: " + libraryID);
            return;
        }

        Library selectedLibrary = targetLibrary.get();

        int recordCount = selectedLibrary.getBookRecordCollection().size();
        int copyCount   = selectedLibrary.getBookCopyCollection().size();

        System.out.println("\n## Library Information:");
        System.out.println("- Library ID: " + selectedLibrary.getLibraryID());
        System.out.println("- Name: "       + selectedLibrary.getName());
        System.out.println("- Address: "    + selectedLibrary.getAddress());
        System.out.println("- Records: "    + recordCount);
        System.out.println("- Book Copies: "+ copyCount);

        System.out.print("\n⚠ WARNING: This will remove the entire library and all its data!");
        System.out.print("\nAre you sure? Type the library ID to confirm: ");
        String confirmation = scanner.nextLine().trim().toUpperCase();

        if (!confirmation.equals(libraryID)) {
            System.out.println("Operation cancelled.");
            return;
        }

        ArrayList<Library> librariesToRemove = new ArrayList<>();
        librariesToRemove.add(selectedLibrary);

        if (LibraryNetwork.getInstance().removeAll(librariesToRemove)) {
            // ── commit undo state ─────────────────────────────────────────────
            this.lastAction     = ActionType.LIBRARY;
            this.removedLibrary = selectedLibrary;
            // ─────────────────────────────────────────────────────────────────
            System.out.println("✓ Library removed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to remove library. Please check the details and try again.");
        }
    }


    // ─── undoMe() ─────────────────────────────────────────────────────────────

    @Override
    public void undoMe() {
        if (lastAction == null) {
            System.out.println("Nothing to undo for RemoveData.");
            return;
        }

        switch (lastAction) {

            case COPY -> {
                // Re-add the single copy back into the library's collection.
                // Also restore the corresponding BookRecord's copy counters.
                copyTargetLibrary.getBookCopyCollection().add(removedCopy);
                Searching.searchSingle(
                    copyTargetLibrary.getBookRecordCollection(),
                    BookRecord.RecordFields.ISBN,
                    removedCopy.getIsbn()
                ).ifPresent(record -> record.addCopy());

                System.out.println("Undo: Copy [" + removedCopy.getCopyID()
                    + "] has been restored.");
            }

            case RECORD -> {
                // Re-add the record, then re-add all its associated copies.
                // The record's counters are already intact inside the
                // removedRecord object since we kept the reference.
                recordTargetLibrary.getBookRecordCollection().add(removedRecord);
                recordTargetLibrary.getBookCopyCollection().addAll(removedCopies);

                System.out.println("Undo: Record [" + removedRecord.getIsbn()
                    + "] and " + removedCopies.size()
                    + " associated copies have been restored.");
            }

            case LIBRARY -> {
                // Re-add the entire library back into the network.
                // The Library object still holds all its records and copies.
                LibraryNetwork.getInstance().add(removedLibrary);

                System.out.println("Undo: Library [" + removedLibrary.getLibraryID()
                    + "] has been restored.");
            }
        }
    }


    // ─── redoMe() ─────────────────────────────────────────────────────────────

    @Override
    public void redoMe() {
        if (lastAction == null) {
            System.out.println("Nothing to redo for RemoveData.");
            return;
        }

        switch (lastAction) {

            case COPY -> {
                // Remove the copy again and undo the BookRecord counter.
                copyTargetLibrary.getBookCopyCollection().remove(removedCopy);
                Searching.searchSingle(
                    copyTargetLibrary.getBookRecordCollection(),
                    BookRecord.RecordFields.ISBN,
                    removedCopy.getIsbn()
                ).ifPresent(record -> record.removeCopy());

                System.out.println("Redo: Copy [" + removedCopy.getCopyID()
                    + "] has been removed again.");
            }

            case RECORD -> {
                // Remove the copies then the record again.
                recordTargetLibrary.getBookCopyCollection().removeAll(removedCopies);
                recordTargetLibrary.getBookRecordCollection().remove(removedRecord);

                System.out.println("Redo: Record [" + removedRecord.getIsbn()
                    + "] and " + removedCopies.size()
                    + " associated copies have been removed again.");
            }

            case LIBRARY -> {
                // Remove the library from the network again.
                ArrayList<Library> toRemove = new ArrayList<>();
                toRemove.add(removedLibrary);
                LibraryNetwork.getInstance().removeAll(toRemove);

                System.out.println("Redo: Library [" + removedLibrary.getLibraryID()
                    + "] has been removed again.");
            }
        }
    }
}
