package org.snipsniper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.snipsniper.utils.Version;

public class VersionTest {
    @Test
    public void test() {
        assertTrue(new Version("1.0.0").isNewerThen(new Version("0.9.5")));
        assertFalse(new Version("1.5.10").isNewerThen(new Version("1.5.10")));
    }
}
