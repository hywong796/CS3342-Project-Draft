package main;
import data.BookCopy;
import data.BookRecord;
import data.Library;
import java.util.*;

import command.AddData;
import command.BorrowCopy;
import command.EditData;
import command.ListingData;
import command.RecordedCommand;
import command.RemoveData;
import command.ReturnCopy;
import command.SearchingData;
import command.SelectCurrent;
import command.SortingData;

public class Main {
    private static Library currentLibrary;
    private static BookRecord currentBookRecord;
    private static BookCopy currentBookCopy;
    private Scanner in;

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    private void run() {
        in = new Scanner(System.in);
        //start by loading data from files
        data.Repository.loadFromFiles();
        boolean running = true;
        while (running) {
            System.out.print("If you are user, please type 1; Otherwise, please type 2: ");
            int userCode;
            String userCodeLine = in.nextLine();
            try {
                userCode = Integer.parseInt(userCodeLine.trim());
            } catch (NumberFormatException e) {
                userCode = -1;
            }
            System.out.println();
            if (!(userCode == 1 || userCode == 2)) {
                System.out.println("Invalid user type, please type again");
            }
            else {
                boolean innerLoop = true;
                while (innerLoop) {
                    System.out.print("Please type your command: ");
                    String userCommand = in.nextLine();
                    if (userCommand.isEmpty() || userCommand.isBlank()) continue;
                    String[] commandParts = userCommand.split(" ");
                    switch (commandParts[0].toUpperCase()) {
                        //command handling block
                        case "ADD" -> {
                            (new AddData()).execute(commandParts);
                        }
                        case "EDIT" -> {
                            (new EditData()).execute(commandParts);
                        }
                        case "REMOVE" -> {
                            (new RemoveData()).execute(commandParts);
                        }
                        case "BORROW" -> {
                            (new BorrowCopy()).execute(commandParts);
                        }
                        case "RETURN" -> {
                            (new ReturnCopy()).execute(commandParts);
                        }
                        case "SEARCH" -> {
                            (new SearchingData()).execute(commandParts);
                        }
                        case "SORT" -> {
                            (new SortingData()).execute(commandParts);
                        }
                        case "LIST" -> {
                            (new ListingData()).execute(commandParts);
                        }
                        case "SELECT" -> {
                            (new SelectCurrent()).execute(commandParts);
                        }
                        case "UNDO" -> {
                            RecordedCommand.undoOneCommand();
                        }
                        case "REDO" -> {
                            RecordedCommand.redoOneCommand();
                        }
                        case "EXIT" -> {
                            data.Repository.storeToFiles();
                            innerLoop = false;
                            running = false;
                        }

                        default -> {
                            System.out.println("Invalid user command. Please type again");
                        }
                    }
                }
            }
        }
        in.close();
    }

    // Getters
    public static Library getCurrentLibrary() {
        return currentLibrary;
    }

    public static BookRecord getCurrentBookRecord() {
        return currentBookRecord;
    }

    public static BookCopy getCurrentBookCopy() {
        return currentBookCopy;
    }

    // Setters
    public static void setCurrentLibrary(Library currentLibrary) {
        Main.currentLibrary = currentLibrary;
    }

    public static void setCurrentBookRecord(BookRecord currentBookRecord) {
        Main.currentBookRecord = currentBookRecord;
    }

    public static void setCurrentBookCopy(BookCopy currentBookCopy) {
        Main.currentBookCopy = currentBookCopy;
    }
}
