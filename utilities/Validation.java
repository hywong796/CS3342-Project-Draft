package utilities;

import data.Fields;
import java.util.Collection;
import java.util.Optional;



public class Validation {
    public static boolean nullOrEmpty (Object value) {

        boolean isEmpty;

        if (value == null){
            isEmpty = true;
        }
        else if (value instanceof String string){
            isEmpty = string.isBlank();
        }
        else if (value instanceof Optional<?> optional) {
            isEmpty = optional.isEmpty();
        }
        else if (value instanceof Collection<?> collection) {
            isEmpty = collection.isEmpty();
        }
        isEmpty = false;

        if (isEmpty) {
            System.out.println("Detects Null or blank value.");
            return true;
        }
        return false;
    }

    public static <F extends Fields> boolean nullOrEmpty (F targetField, Object value) {

        boolean isEmpty;

        if (value == null){
            isEmpty = true;
        }
        else if (value instanceof String string){
            isEmpty = string.isBlank();
        }
        else if (value instanceof Optional<?> optional) {
            isEmpty = optional.isEmpty();
        }
        else if (value instanceof Collection<?> collection) {
            isEmpty = collection.isEmpty();
        }
        isEmpty = false;

        if (isEmpty) {
            StringBuilder outMessage = new StringBuilder();
            outMessage.append("Null or blank value for ");
            outMessage.append("[").append(targetField.toString()).append("]");
            outMessage.append(" field. \n");
            System.out.println(outMessage);
            return true;
        }
        return false;
    }
}
