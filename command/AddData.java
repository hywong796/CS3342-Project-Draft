package command;

import main.Main;
import data.Library;
import data.Library.LibraryFields;
import data.LibraryNetwork;
import utilities.Searching;

import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;
import java.util.Scanner;

public class AddData extends RecordedCommand {
    
    private static Scanner scanner = new Scanner(System.in);
    private static Library targetLibrary = Main.getCurrentLibrary();
    private static LibraryNetwork libraryNetwork = new LibraryNetwork();

    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length < 2) {
            System.out.println("Invalid command format. Usage: ADD [COPY|RECORD|LIBRARY]");
            return;
        }

        switch (commandParts[1].toUpperCase()) {
            case "COPY" -> addCopy();
            case "RECORD" -> addRecord();
            case "LIBRARY" -> addLibrary();
            default -> System.out.println("Invalid option. Please use COPY, RECORD, or LIBRARY");
        }
    }

    private void addCopy() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }
        System.out.println("=== Add Book Copy ===");
        
        // Get Library ID
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
        
        // Get ISBN
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();
        
        // Get Acquisition Date
        System.out.print("Enter Acquisition Date (yyyy-MM-dd): ");
        String dateStr = scanner.nextLine().trim();
        LocalDate acquisitionDate;
        try {
            acquisitionDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd");
            return;
        }
        
        // Get Acquisition Price
        System.out.print("Enter Acquisition Price: ");
        double acquisitionPrice;
        try {
            acquisitionPrice = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format. Please enter a valid number");
            return;
        }
        
        // Add the copy
        if (targetLibrary.addCopy(isbn, acquisitionDate, acquisitionPrice)) {
            System.out.println("Book copy added successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to add book copy. Please check the details and try again.");
        }
    }

    private void addRecord() {
        if (LibraryNetwork.getInstance().isEmpty()) {
            System.out.println("No library exists!");
            return;
        }

        System.out.println("=== Add Book Record ===");
        
        // Get Library ID
        System.out.print("Enter Library ID: ");
        String libraryID = scanner.nextLine().trim();
        
        Optional<Library> targetLibrary = findLibraryByID(libraryID);
        if (targetLibrary.isEmpty()) {
            System.out.println("Library not found with ID: " + libraryID);
            return;
        }
        
        // Get ISBN
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();
        
        // Get Title
        System.out.print("Enter Title: ");
        String title = scanner.nextLine().trim();
        
        // Get Author
        System.out.print("Enter Author: ");
        String author = scanner.nextLine().trim();
        
        // Get Language
        System.out.print("Enter Language: ");
        String language = scanner.nextLine().trim();
        
        // Get Category
        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();
        
        // Get Publishing Year
        System.out.print("Enter Publishing Year (yyyy): ");
        Year publishingYear;
        try {
            publishingYear = Year.parse(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid year format. Please enter a valid year (yyyy)");
            return;
        }
        
        // Add the record
        if (targetLibrary.get().addRecord(isbn, title, author, language, category, publishingYear)) {
            System.out.println("Book record added successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to add book record. Please check the details and try again.");
        }
    }

    private void addLibrary() {
        System.out.println("=== Add Library ===");
        
        // Get Library ID
        System.out.print("Enter Library ID: ");
        String libraryID = scanner.nextLine().trim();
        
        // Get Name
        System.out.print("Enter Library Name: ");
        String name = scanner.nextLine().trim();
        
        // Get Address
        System.out.print("Enter Address: ");
        String address = scanner.nextLine().trim();
        
        // Get Email
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        
        // Add the library
        if (libraryNetwork.addLibrary(libraryID, name, address, email)) {
            System.out.println("Library added successfully!");
            addUndoCommand(this);
            clearRedoList();
        } else {
            System.out.println("Failed to add library. Please check the details and try again.");
        }
    }

    private Optional<Library> findLibraryByID(String libraryID) {
        return LibraryNetwork.getInstance().stream()
            .filter(lib -> lib.getLibraryID().equals(libraryID))
            .findFirst();
    }

    @Override
    public void undoMe() {
        // TODO: Implement undo functionality
        System.out.println("Undo functionality not yet implemented for AddData");
    }

    @Override
    public void redoMe() {
        // TODO: Implement redo functionality
        System.out.println("Redo functionality not yet implemented for AddData");
    }
    
}
