package utilities;

import data.BookCopy;
import data.BookRecord;
import data.Library;
import java.util.*;

public class Sorting {

    interface SortRules<T> {
        Comparator<T> getComparator();
    }

    // BookRecord sorting rules
    enum BookRecordSortRules implements SortRules<BookRecord> {
        ISBN, TITLE, AUTHOR, LANGUAGE, CATEGORY, PUBLISHING_YEAR;

        @Override
        public Comparator<BookRecord> getComparator() {
            return switch (this) {
                case ISBN -> Comparator.comparing(BookRecord::getIsbn);
                case TITLE -> Comparator.comparing(BookRecord::getTitle);
                case AUTHOR -> Comparator.comparing(BookRecord::getAuthor);
                case LANGUAGE -> Comparator.comparing(BookRecord::getLanguage);
                case CATEGORY -> Comparator.comparing(BookRecord::getCategory);
                case PUBLISHING_YEAR -> Comparator.comparing(BookRecord::getPublishingYear,
                        Comparator.nullsLast(Comparator.naturalOrder()));
            };
        }
    }

    // BookCopy sorting rules
    enum BookCopySortRules implements SortRules<BookCopy> {
        ISBN, COPYID, ACQUISITION_DATE, ACQUISITION_PRICE;

        @Override
        public Comparator<BookCopy> getComparator() {
            return switch (this) {
                case ISBN -> Comparator.comparing(BookCopy::getIsbn);
                case COPYID -> Comparator.comparing(BookCopy::getCopyID);
                case ACQUISITION_DATE -> Comparator.comparing(BookCopy::getAcquisitionDate);
                case ACQUISITION_PRICE -> Comparator.comparing(BookCopy::getAcquisitionPrice);
            };
        }
    }

    // Library sorting rules
    enum LibrarySortRules implements SortRules<Library> {
        NAME, ADDRESS, PHONE, EMAIL;

        @Override
        public Comparator<Library> getComparator() {
            return switch (this) {
                case NAME -> Comparator.comparing(Library::getName);
                case ADDRESS -> Comparator.comparing(Library::getAddress);
                case PHONE -> Comparator.comparing(Library::getPhone);
                case EMAIL -> Comparator.comparing(Library::getEmail);
            };
        }
    }

    public static <T> Comparator<T> createComparator(SortRules<T>... rules) {
        return Arrays.stream(rules)
            .map(SortRules::getComparator)
            .reduce(Comparator::thenComparing)
            .orElse((a, b) -> 0);
    }

    public static <T> ArrayList<T> sort (ArrayList<T> collection, ArrayList<SortRules<T>> rules) {
        ArrayList<T> sorted = new ArrayList<>(collection);
        Collections.sort(sorted, createComparator(rules.toArray(SortRules[]::new)));
        return sorted;
    }

    // Convenience methods for common patterns
    /* public static Comparator<BookRecord> byTitle() {
        return createComparator(BookRecordSortRules.TITLE);
    }

    public static Comparator<BookRecord> byAuthorThenTitle() {
        return createComparator(BookRecordSortRules.AUTHOR, BookRecordSortRules.TITLE);
    }

    public static Comparator<BookCopy> byCopyId() {
        return createComparator(BookCopySortRules.COPYID);
    }

    // Demo/test main
    public static void main(String[] args) {
        System.out.println("SortUtils ready for Library/BookRecord sorting");
        System.out.println("Example: Collections.sort(records, SortUtils.byTitle())");
    } */
}