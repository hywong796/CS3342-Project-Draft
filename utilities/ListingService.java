package utilities;

import java.util.ArrayList;

import data.AccessibleRecord;

public class ListingService {
    
    /**
     * Display a collection of items in a formatted table layout
     * @param collection ArrayList of elements implementing AccessibleRecord
     */
    public static <T extends AccessibleRecord<?>> void listing(ArrayList<T> collection) {
        if (collection == null || collection.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }
        
        // Create header using toString of the first element
        System.out.println("\n" + createTableHeader(collection.size()));
        
        // Display each element using toString()
        for (int i = 0; i < collection.size(); i++) {
            T element = collection.get(i);
            System.out.printf("| %-3d | %s%n", i + 1, formatElementForTable(element.toString()));
        }
        
        System.out.println(createTableFooter());
        System.out.println();
    }
    
    /**
     * Create a formatted table header
     */
    private static String createTableHeader(int itemCount) {
        StringBuilder header = new StringBuilder();
        header.append("╔════════════════════════════════════════════════════════════════════════════════╗\n");
        header.append("║ Item | Details                                                                 ║\n");
        header.append("╠════╪════════════════════════════════════════════════════════════════════════════╣\n");
        return header.toString();
    }
    
    /**
     * Create a formatted table footer
     */
    private static String createTableFooter() {
        return "╚════╧════════════════════════════════════════════════════════════════════════════════╝";
    }
    
    /**
     * Format element string to fit in table (pad or truncate as needed)
     */
    private static String formatElementForTable(String elementStr) {
        final int MAX_WIDTH = 74; // Width available for content
        
        if (elementStr == null) {
            elementStr = "N/A";
        }
        
        // If string is shorter than max width, pad it
        if (elementStr.length() < MAX_WIDTH) {
            return String.format("%-" + MAX_WIDTH + "s", elementStr);
        }
        
        // If string is longer, truncate and add ellipsis
        return elementStr.substring(0, MAX_WIDTH - 3) + "...";
    }
    
    /**
     * Alternative method for displaying a collection with custom formatting
     */
    public static <T extends AccessibleRecord<?>> void listingWithLineNumbers(ArrayList<T> collection) {
        if (collection == null || collection.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }
        
        System.out.println("\n┌─────────────────────────────────────────────────────────────────────────────┐");
        System.out.printf("│ Total Items: %-67d │%n", collection.size());
        System.out.println("├─────────────────────────────────────────────────────────────────────────────┤");
        
        for (int i = 0; i < collection.size(); i++) {
            T element = collection.get(i);
            String elementStr = element.toString();
            final int MAX_WIDTH = 73;
            
            if (elementStr.length() > MAX_WIDTH) {
                elementStr = elementStr.substring(0, MAX_WIDTH - 3) + "...";
            }
            
            System.out.printf("│ %-3d │ %-" + (MAX_WIDTH - 6) + "s │%n", i + 1, elementStr);
        }
        
        System.out.println("└─────────────────────────────────────────────────────────────────────────────┘\n");
    }
}
