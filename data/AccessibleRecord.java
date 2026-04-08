package data;

public interface AccessibleRecord <F extends Fields> {
    abstract Object getField(String target);
    abstract Object getField(F target);
    abstract boolean setField(F targetField, Object value);
}
