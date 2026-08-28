package java.lang.ref;

public class SoftReference<T> extends Reference<T> {

    public SoftReference(T value) {
        super(value);
    }

    public SoftReference(T value, ReferenceQueue<? super T> queue) {
        super(value);
    }
}
