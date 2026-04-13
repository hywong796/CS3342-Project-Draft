import data.BookCopy;
import data.BookRecord;
import data.Library;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        //start by loading data from files
        while (true) {
            System.out.println("If you are user, please type 1; Otherwise, please type 2");
            Scanner in = new Scanner(System.in);
            int userCode = in.nextInt();
            if (!(userCode == 1 || userCode == 2)) {
                System.out.println("Invalid user type, please type again");
            }
            else {
                while (true) {
                    System.out.print("Please type your command: ");
                    String userCommand = in.nextLine();
                    switch (userCommand) {
                        //command handling block
                        case "exit" -> {
                            break;
                        }

                        default -> {
                            System.out.println("Invalid user command. Please type again");
                        }
                    }

                }
            }
        }
    }
}
