package command;


import main.Main;
import data.Library;
import data.Library.LibraryFields;
import data.BookCopy;
import data.BookCopy.CopyFields;
import data.BookRecord;
import data.LibraryNetwork;
import utilities.Searching;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class BorrowCopy extends RecordedCommand {

    private static Scanner scanner = new Scanner(System.in);


    // ─── Undo/Redo State ──────────────────────────────────────────────────────

    private Library    savedTargetLibrary;
    private BookCopy   borrowedCopy;
    private BookRecord correspondingRecord;
    private String     savedBorrowerID;
    private LocalDate  savedLastBorrowingDate; // lastBorrowingDate value BEFORE borrow


    // ─── execute() ────────────────────────────────────────────────────────────

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: BORROW [BYISBN|BYSEARCHINGNAME]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "BYISBN"          -> borrowCopyByIsbn();
            case "BYSEARCHINGNAME" -> borrowCopyBySearchingName();
            default -> System.out.println("Invalid option. Please use BYISBN or BYSEARCHINGNAME");
        }
    }


    // ─── borrowCopyByIsbn() ───────────────────────────────────────────────────

    private void borrowCopyByIsbn() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Borrow Book Copy ===");

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

        if (targetLibrary.getBookCopyCollection().isEmpty()) {
            System.out.println("No any copy collection in this library!");
            return;
        }

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();

        List<BookCopy> allCopies = Searching.searchMultiple(
            targetLibrary.getBookCopyCollection(), CopyFields.ISBN, isbn);

        if (allCopies.isEmpty()) {
            System.out.println("No copies found for ISBN: " + isbn);
            return;
        }

        List<BookCopy> availableForBorrow = allCopies.stream()
            .filter(copy -> BookCopy.Status.isAvailable(copy))
            .toList();

        if (availableForBorrow.isEmpty()) {
            System.out.println("No available copies for ISBN: " + isbn);
            return;
        }

        System.out.println("Available copies:");
        for (int i = 0; i < availableForBorrow.size(); i++) {
            System.out.println((i + 1) + ". Copy ID: " + availableForBorrow.get(i).getCopyID());
        }

        System.out.print("Select copy number: ");
        int copyIndex;
        try {
            copyIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (copyIndex < 0 || copyIndex >= availableForBorrow.size()) {
                System.out.println("Invalid selection");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number");
            return;
        }

        BookCopy selectedCopy = availableForBorrow.get(copyIndex);

        System.out.print("Enter Borrower ID: ");
        String borrowerID = scanner.nextLine().trim();

        // ── capture pre-borrow state BEFORE calling borrowCopy() ─────────────
        LocalDate preBorrowDate = selectedCopy.getLastBorrowingDate();
        BookRecord record = Searching.searchSingle(
            targetLibrary.getBookRecordCollection(),
            BookRecord.RecordFields.ISBN,
            selectedCopy.getIsbn()
        ).orElse(null);
        // ─────────────────────────────────────────────────────────────────────

        if (targetLibrary.borrowCopy(selectedCopy, borrowerID)) {
            // ── commit undo state only on success ─────────────────────────────
            savedTargetLibrary     = targetLibrary;
            borrowedCopy           = selectedCopy;
            correspondingRecord    = record;
            savedBorrowerID        = borrowerID;
            savedLastBorrowingDate = preBorrowDate;
            // ─────────────────────────────────────────────────────────────────
            System.out.println("Book copy borrowed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to borrow book copy. Please check the details and try again.");
        }
    }


    // ─── borrowCopyBySearchingName() ──────────────────────────────────────────

    private void borrowCopyBySearchingName() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Borrow Book Copy by Title/Author ===");

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

        System.out.print("Enter Title or Author name: ");
        String searchTerm = scanner.nextLine().trim();

        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty");
            return;
        }

        System.out.print("Enter Borrower ID: ");
        String borrowerID = scanner.nextLine().trim();

        if (borrowerID.isEmpty()) {
            System.out.println("Borrower ID cannot be empty");
            return;
        }

        List<BookCopy> foundCopies = library.getBookCopyCollection().stream()
            .filter(copy -> {
                Optional<BookRecord> record = Searching.searchSingle(
                    library.getBookRecordCollection(),
                    BookRecord.RecordFields.ISBN,
                    copy.getIsbn()
                );
                if (record.isEmpty()) return false;

                String title  = (String) record.get().getField(BookRecord.RecordFields.TITLE);
                String author = (String) record.get().getField(BookRecord.RecordFields.AUTHOR);

                return (title  != null && title.toLowerCase().contains(searchTerm.toLowerCase())) ||
                       (author != null && author.toLowerCase().contains(searchTerm.toLowerCase()));
            })
            .filter(copy -> BookCopy.Status.isAvailable(copy))
            .toList();

        if (foundCopies.isEmpty()) {
            System.out.println("No available copies found matching: " + searchTerm);
            return;
        }

        System.out.println("Available copies found:");
        for (int i = 0; i < foundCopies.size(); i++) {
            BookCopy copy = foundCopies.get(i);
            Optional<BookRecord> rec = Searching.searchSingle(
                library.getBookRecordCollection(),
                BookRecord.RecordFields.ISBN,
                copy.getIsbn()
            );
            if (rec.isPresent()) {
                String title = (String) rec.get().getField(BookRecord.RecordFields.TITLE);
                System.out.println((i + 1) + ". Title: " + title + " | Copy ID: " + copy.getCopyID());
            }
        }

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

        // ── capture pre-borrow state BEFORE calling borrowCopy() ─────────────
        LocalDate preBorrowDate = selectedCopy.getLastBorrowingDate();
        BookRecord record = Searching.searchSingle(
            library.getBookRecordCollection(),
            BookRecord.RecordFields.ISBN,
            selectedCopy.getIsbn()
        ).orElse(null);
        // ─────────────────────────────────────────────────────────────────────

        if (library.borrowCopy(selectedCopy, borrowerID)) {
            // ── commit undo state only on success ─────────────────────────────
            savedTargetLibrary     = library;
            borrowedCopy           = selectedCopy;
            correspondingRecord    = record;
            savedBorrowerID        = borrowerID;
            savedLastBorrowingDate = preBorrowDate;
            // ─────────────────────────────────────────────────────────────────
            System.out.println("Book copy borrowed successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to borrow book copy. Please check the details and try again.");
        }
    }


    // ─── undoMe() ─────────────────────────────────────────────────────────────

    @Override
    public void undoMe() {
        if (borrowedCopy == null) {
            System.out.println("Nothing to undo for BorrowCopy.");
            return;
        }
        // Reverse BookCopy state
        borrowedCopy.undoBorrow(savedLastBorrowingDate);
        // Reverse BookRecord counters
        if (correspondingRecord != null) {
            correspondingRecord.undoBorrowCopy();
        }
        System.out.println("Undo: Copy [" + borrowedCopy.getCopyID() + "] is no longer borrowed.");
    }

    @Override
    public void redoMe() {
        if (borrowedCopy == null) {
            System.out.println("Nothing to redo for BorrowCopy.");
            return;
        }
        if (savedTargetLibrary.borrowCopy(borrowedCopy, savedBorrowerID)) {
            System.out.println("Redo: Copy [" + borrowedCopy.getCopyID() + "] borrowed again by " + savedBorrowerID + ".");
        } else {
            System.out.println("Redo failed: could not re-borrow copy [" + borrowedCopy.getCopyID() + "].");
        }
    }
}