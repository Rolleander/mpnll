package java.lang.ref;

public abstract class Reference<T> {

    private T value;

    protected Reference(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void clear() {
        value = null;
    }

    public boolean enqueue() {
        clear();
        return false;
    }

    public boolean isEnqueued() {
        return false;
    }
}
