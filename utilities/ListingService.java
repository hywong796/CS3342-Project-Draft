package utilities;

import java.util.ArrayList;

import data.AccessibleRecord;

public class ListingService {
    
    /**
     * Display a collection of items with clean markdown-style formatting
     * @param collection ArrayList of elements implementing AccessibleRecord
     */
    public static <T extends AccessibleRecord<?>> void listing(ArrayList<T> collection) {
        if (collection == null || collection.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }
        
        System.out.println();
        for (int i = 0; i < collection.size(); i++) {
            T element = collection.get(i);
            displayElement(i + 1, element.toString());
        }
        System.out.println();
    }
    
    /**
     * Display a single element with clean markdown-style formatting
     */
    private static void displayElement(int index, String elementStr) {
        // Item header with markdown style
        System.out.println("## " + index + ". Item");
        
        // Content
        if (elementStr == null || elementStr.isEmpty()) {
            System.out.println("No content");
        } else {
            System.out.println(elementStr);
        }
        
        System.out.println();
    }
    
    /**
     * Alternative method for displaying a collection with compact formatting
     */
    public static <T extends AccessibleRecord<?>> void listingCompact(ArrayList<T> collection) {
        if (collection == null || collection.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }
        
        System.out.println("\n# Items List (" + collection.size() + " total)\n");
        
        for (int i = 0; i < collection.size(); i++) {
            T element = collection.get(i);
            String elementStr = element.toString();
            
            System.out.printf("- [%d] %s%n", i + 1, elementStr);
        }
        
        System.out.println();
    }
}
