package com.github.dfauth.stomp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class MaybeLaterTest {

    private static final Logger logger = LoggerFactory.getLogger(MaybeLaterTest.class);

    @Test
    public void testIt() {
        AtomicBoolean a = new AtomicBoolean(false);
        Consumer<AtomicBoolean> consumer = _a -> _a.set(true);
        {
            MaybeLater<AtomicBoolean> maybe = new MaybeLater<>();
            maybe.accept(a);
            assertFalse(a.get());
        }
        {
            MaybeLater<AtomicBoolean> maybe = new MaybeLater<>();
            maybe.accept(consumer);
            assertFalse(a.get());
        }
        {
            MaybeLater<AtomicBoolean> maybe = new MaybeLater<>();
            maybe.accept(a);
            maybe.accept(consumer);
            assertTrue(a.get());
        }
        {
            MaybeLater<AtomicBoolean> maybe = new MaybeLater<>();
            maybe.accept(consumer);
            maybe.accept(a);
            assertTrue(a.get());
        }
    }
}
