package utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import data.Fields;
import utilities.FilterService.Filter.FilterFields;
import data.AccessibleRecord;

public class FilterService {

    public static class Filter<F extends Fields> {
        public enum State {
            FIRST, OR, AND
        }

        public enum FilterFields {
            INDEX (Integer.class), 
            FIELD (Fields.class), 
            VALUE (Object.class), 
            STATE (State.class);

            private final Class<?> fieldType;

            private FilterFields (Class<?> fieldType) {
                this.fieldType = fieldType;
            }

            public boolean isValidValue(Object value) {
                return fieldType.isInstance(value);
            }

            public static Optional<FilterFields> matchField(String target) {
                try {
                    return Optional.of(valueOf(target.toUpperCase()));
                } 
                catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
            }

            public static boolean isValidField(String target) {
                return matchField(target).isPresent();
            }
        }

        private int index;
        private F field;
        private Object value;
        private State state;


        Filter(int index, F field, Object value, State state) {
            this.index = index;
            this.field = field;
            this.value = value;
            this.state = state;
            
        }

        public F getField() {
            return field;
        }

        public void setField(F field) {
            this.field = field;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex (int index) {
            this.index = index;
        }

        public Object getField(String targetField) {
            if (FilterFields.isValidField(targetField)) {
                return switch ((FilterFields.matchField(targetField)).get()) {
                    case INDEX -> getIndex();
                    case FIELD -> getField();
                    case VALUE -> getValue();
                    case STATE -> getState();
                };
            } else {
                System.out.println("Unmatched field");
                return Optional.empty();
            }
        }

        public boolean setField (FilterFields targetField, Object newValue) {
            if (targetField.isValidValue(newValue)) {
                switch (targetField) {
                    case INDEX -> setIndex((int) newValue);
                    case FIELD -> setField((F) newValue);
                    case VALUE -> setValue(newValue);
                    case STATE -> {
                        System.out.println("State cannot be changed");
                        return false;
                    }
                }
                return true;
            }
            else {
                StringBuilder outMessage = new StringBuilder();
                outMessage.append("Invalid value for ");
                outMessage.append("[").append(targetField.toString()).append("]");
                outMessage.append(" field. \n");
                System.out.println(outMessage);
                return false;
            }
        }
    } 

    public static <F extends Fields> boolean addFilter(
            ArrayList<Filter<F>> filterCollection, F field, Object value, Filter.State state) {

        if (Validation.nullOrEmpty(field)) return false;
        if (Validation.nullOrEmpty(value)) return false;
        if (Validation.nullOrEmpty(state)) return false;

        if (filterCollection.isEmpty()) {
            state = Filter.State.FIRST;
        }

        return filterCollection.add(new Filter<F>(filterCollection.size() + 1, field, value, state));
    }

    public static <F extends Fields> boolean editFilter(ArrayList<Filter<F>> filterCollection,Filter<F> filterToEdit, FilterFields fieldToEdit, Object newValue){
        if (Validation.nullOrEmpty(filterCollection)) return false;
        if (Validation.nullOrEmpty(filterToEdit)) return false;
        if (Validation.nullOrEmpty(fieldToEdit)) return false;
        if (Validation.nullOrEmpty(newValue)) return false;

        if (fieldToEdit.isValidValue(newValue)) {
            for (Filter<F> target : filterCollection) {
                if (target == filterToEdit) {
                    return target.setField(fieldToEdit, newValue);
                }
            }
        }
        return false;
    };

    public static <F extends Fields> boolean removeFilter(ArrayList<Filter<F>> filterCollection, ArrayList<Filter<F>> filtersToRemove){
        if (Validation.nullOrEmpty(filterCollection)) return false;
        if (Validation.nullOrEmpty(filtersToRemove)) return false;

        return filterCollection.removeAll(filtersToRemove);
    };
    public static <F extends Fields> boolean cleanFilterList(ArrayList<Filter<F>> filterCollection){
        if (Validation.nullOrEmpty(filterCollection)) return false;
        filterCollection.clear();
        return true;
    };

    public static <F extends Fields, T extends AccessibleRecord<F>> ArrayList<T> filterAll(ArrayList<T> collection, ArrayList<Filter<F>> filters) {
        ArrayList<T> result = new ArrayList<>();
        
        if (Validation.nullOrEmpty(collection)) return new ArrayList<>();
        if (Validation.nullOrEmpty(filters)) return new ArrayList<>(collection);

        for (Filter<F> filter : filters) {
            switch (filter.getState()) {
                case FIRST -> {
                    List<T> outList = 
                        collection.stream()
                        .filter(target -> 
                            filter.getValue().equals(target.getField(filter.getField()))  
                        )
                        .collect(Collectors.toList());

                        result.addAll(outList);
                }
                case AND -> {                    
                    List<T> toKeep = result.stream()
                        .filter(target -> 
                                filter.getValue().equals(target.getField(filter.getField()))  
                            )
                        .collect(Collectors.toCollection(ArrayList::new));
                    result.clear();
                    result.addAll(toKeep);
                }
                case OR -> {
                    List<T> outList = collection.stream()
                        .filter(target -> filter.getValue().equals(target.getField(filter.getField())))
                        .filter(target -> !result.contains(target))
                        .collect(Collectors.toList());
                    result.addAll(outList);
                }
            }
        }
        return result;
    }
}