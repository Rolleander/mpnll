package java.lang.ref;

public class WeakReference<T> extends Reference<T> {

    public WeakReference(T value) {
        super(value);
    }

    public WeakReference(T value, ReferenceQueue<? super T> queue) {
        super(value);
    }
}
