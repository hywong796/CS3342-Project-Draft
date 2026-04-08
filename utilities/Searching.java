package utilities;

import data.AccessibleRecord;
import data.Fields;
import java.util.*;
import java.util.stream.Collectors;

public class Searching {

    public static <F extends Fields, T extends AccessibleRecord<F>> Optional<T> searchSingle(
            ArrayList<T> collection,
            F targetField,
            Object targetValue
    ) {
        //null check
        if (collection == null || collection.isEmpty()) return Optional.empty();
        if (targetField == null) return Optional.empty();
        if (targetValue == null) return Optional.empty();

        //
        return collection.stream()
        .filter(target -> {
            Object v = target.getField(targetField);
            return v != null && v.equals(targetValue);
        })
        .findFirst();
    }

    public static <F extends Fields, T extends AccessibleRecord<F>> Optional<T> searchSingle(
            ArrayList<T> collection,
            String targetField,
            Object targetValue
    ) {
        //null check
        if (collection == null || collection.isEmpty()) return Optional.empty();
        if (targetField == null) return Optional.empty();
        if (targetValue == null) return Optional.empty();

        //
        return collection.stream()
        .filter(target -> {
            Object v = target.getField(targetField);
            return v != null && v.equals(targetValue);
        })
        .findFirst();
    }

    public static <F extends Fields, T extends AccessibleRecord<F>> List<T> searchMultiple(
            ArrayList<T> collection,
            F targetField,
            Object targetValue
    ) {
        //null check
        if (collection == null || collection.isEmpty()) return Collections.emptyList();
        if (targetField == null) return Collections.emptyList();
        if (targetValue == null) return Collections.emptyList();

        List<T> outList = 
            collection.stream()
            .filter(target -> {
                Object matchingValue = target.getField(targetField);
                return matchingValue != null && matchingValue.equals(targetValue);
            })
            .collect(Collectors.toList());

        ArrayList<T> outArrayList = new ArrayList<>(outList);
        if (outArrayList.isEmpty()) {
            System.out.println("No matched results");
            return Collections.emptyList();
        }
        return outArrayList;
    }

    public static <F extends Fields, T extends AccessibleRecord<F>> Optional<ArrayList<T>> searchMultiple(
            ArrayList<T> collection,
            String targetField,
            Object targetValue
    ) {
        //null check
        if (collection == null || collection.isEmpty()) return Optional.empty();
        if (targetField == null) return Optional.empty();
        if (targetValue == null) return Optional.empty();

        List<T> outList = 
            collection.stream()
            .filter(target -> {
                Object matchingValue = target.getField(targetField);
                return matchingValue != null && matchingValue.equals(targetValue);
            })
            .collect(Collectors.toList());

        ArrayList<T> outArrayList = new ArrayList<>(outList);
        if (outArrayList.isEmpty()) {
            System.out.println("No matched results");
            return Optional.empty();
        }
        return Optional.of(outArrayList);
    }
}
