package com.broll.mpnll.test;

import static org.junit.Assert.assertArrayEquals;

import com.broll.mpnll.server.site.PackageReceiver;
import com.broll.mpnll.server.utils.AnnotationScanner;

import org.junit.Test;

public class ScannerTest {

    @Test
    public void scansInheritedAndOverriddenMethods() {
        assertArrayEquals(
            new String[]{A.class.getName() + ":test"},
            scanPackageReceivers(new A())
        );
        assertArrayEquals(
            new String[]{B.class.getName() + ":test", B.class.getName() + ":test2"},
            scanPackageReceivers(new B())
        );
        assertArrayEquals(
            new String[]{A.class.getName() + ":test", C.class.getName() + ":test2"},
            scanPackageReceivers(new C())
        );
    }

    private String[] scanPackageReceivers(Object target) {
        return AnnotationScanner.findAnnotatedMethods(target, PackageReceiver.class).stream()
            .map(method -> method.getDeclaringClass().getName() + ":" + method.getName())
            .sorted()
            .toArray(String[]::new);
    }

    public static class A {

        @PackageReceiver
        void test() {
        }
    }

    public static class B extends A {

        @Override
        @PackageReceiver
        void test() {
        }

        @PackageReceiver
        void test2() {
        }
    }

    public static class C extends A {

        @PackageReceiver
        void test2() {
        }
    }
}
