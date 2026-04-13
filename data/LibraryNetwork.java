package data;

import java.util.ArrayList;

import data.Library.LibraryFields;

import static utilities.Searching.searchSingle;
import utilities.Validation;

public class LibraryNetwork {
    private static ArrayList<Library> Libraries;

    public static ArrayList<Library> getInstance() {
        if (Libraries == null) {
            Libraries = new ArrayList<>();
        };

        return Libraries;
    }

    //addLibrary
    public boolean addLibrary (
        String libraryID, String name, String address, String email
    ) {
        //null check 
        if (Validation.nullOrEmpty(libraryID)) return false;
        if (Validation.nullOrEmpty(name)) return false;
        if (Validation.nullOrEmpty(address)) return false;
        if (Validation.nullOrEmpty(email)) return false;

        //duplication check
        if (searchSingle(Libraries, Library.LibraryFields.LIBRARY_ID, libraryID).isPresent()) return false;

        return Libraries.add(new Library(libraryID, name, address, address, email));
    }

    //editLibrary
    public boolean editLibrary (Library libraryToEdit, LibraryFields targetField, Object newValue) {
        if (Validation.nullOrEmpty(Libraries)) return false;
        if (Validation.nullOrEmpty(libraryToEdit)) return false;
        if (Validation.nullOrEmpty(targetField)) return false;
        if (Validation.nullOrEmpty(newValue)) return false;

        return libraryToEdit.setField(targetField, newValue);
    }

    //removeLibrary
    public boolean removeLibrary (ArrayList<Library> librariesToRemove) {
        if (Validation.nullOrEmpty(librariesToRemove)) return false;
        return Libraries.removeAll(librariesToRemove);
    }

    //load data
}
 