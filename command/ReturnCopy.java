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


    // ─── Undo/Redo State ──────────────────────────────────────────────────────

    private Library  savedTargetLibrary;
    private BookCopy returnedCopy;
    private String   savedBorrowerID;   // borrowerID captured BEFORE returnMe() clears it


    // ─── execute() ────────────────────────────────────────────────────────────

    @Override
    public void execute(String[] commandParts) {
        returnCopyByBorrowerID();
    }


    // ─── returnCopyByBorrowerID() ─────────────────────────────────────────────

    private void returnCopyByBorrowerID() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Return Book Copy ===");

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

        System.out.print("Enter Borrower ID: ");
        String borrowerID = scanner.nextLine().trim().toUpperCase();

        if (borrowerID.isEmpty()) {
            System.out.println("Borrower ID cannot be empty");
            return;
        }

        List<BookCopy> borrowedByUser = targetLibrary.getBookCopyCollection().stream()
            .filter(copy -> BookCopy.Status.isBorrowed(copy) &&
                            copy.getBorrowerID() != null &&
                            copy.getBorrowerID().equalsIgnoreCase(borrowerID))
            .toList();

        if (borrowedByUser.isEmpty()) {
            System.out.println("No borrowed copies found for Borrower ID: " + borrowerID);
            return;
        }

        System.out.println("\n# Borrowed Books for " + borrowerID
            + " (" + borrowedByUser.size() + " total):");

        for (int i = 0; i < borrowedByUser.size(); i++) {
            BookCopy copy = borrowedByUser.get(i);

            Optional<data.BookRecord> record = Searching.searchSingle(
                targetLibrary.getBookRecordCollection(),
                data.BookRecord.RecordFields.ISBN,
                copy.getIsbn()
            );

            String title = "Unknown Title";
            if (record.isPresent()) {
                title = (String) record.get().getField(data.BookRecord.RecordFields.TITLE);
            }

            System.out.println("- [" + (i + 1) + "] " + title
                + " | Copy ID: " + copy.getCopyID()
                + " | Last Borrowed: " + copy.getLastBorrowingDate());
        }

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

        // ── capture pre-return state BEFORE returnMe() clears borrowerID ──────
        String preBorrowerID = selectedCopy.getBorrowerID();
        // ─────────────────────────────────────────────────────────────────────

        if (targetLibrary.returnCopy(selectedCopy)) {
            // ── commit undo state only on success ─────────────────────────────
            savedTargetLibrary = targetLibrary;
            returnedCopy       = selectedCopy;
            savedBorrowerID    = preBorrowerID;
            // ─────────────────────────────────────────────────────────────────
            System.out.println("✓ Book copy returned successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("✗ Failed to return book copy. Please check the details and try again.");
        }
    }


    // ─── undoMe() ─────────────────────────────────────────────────────────────

    @Override
    public void undoMe() {
        if (returnedCopy == null) {
            System.out.println("Nothing to undo for ReturnCopy.");
            return;
        }

        // Reverse returnMe(): restore status to BORROWED and put borrowerID back.
        returnedCopy.undoReturn(savedBorrowerID);

        System.out.println("Undo: Copy [" + returnedCopy.getCopyID()
            + "] is marked as BORROWED again for [" + savedBorrowerID + "].");
    }


    // ─── redoMe() ─────────────────────────────────────────────────────────────

    @Override
    public void redoMe() {
        if (returnedCopy == null) {
            System.out.println("Nothing to redo for ReturnCopy.");
            return;
        }

        // Re-apply the return: copy must currently be BORROWED for this to succeed.
        if (savedTargetLibrary.returnCopy(returnedCopy)) {
            System.out.println("Redo: Copy [" + returnedCopy.getCopyID()
                + "] has been returned again.");
        } else {
            System.out.println("Redo failed: Copy [" + returnedCopy.getCopyID()
                + "] is not in a BORROWED state.");
        }
    }
}