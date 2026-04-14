import data.BookCopy;
import data.BookRecord;
import data.Library;
import java.util.*;

import command.AddData;
import command.RecordedCommand;
import command.RemoveData;

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
        boolean running = true;
        while (running) {
            System.out.println("If you are user, please type 1; Otherwise, please type 2");
            int userCode = in.nextInt();
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
                    switch (commandParts[0]) {
                        //command handling block
                        case "add" -> {
                            (new AddData()).execute(commandParts);
                        }
                        case "remove" -> {
                            if (currentLibrary == null && commandParts[1] == "Library") continue;
                            (new RemoveData(currentLibrary)).execute(commandParts);
                        }
                        case "undo" -> {
                            RecordedCommand.undoOneCommand();
                        }
                        case "redo" -> {
                            RecordedCommand.redoOneCommand();
                        }
                        case "exit" -> {
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
