package net.snipsniper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import net.snipsniper.utils.Version;

public class VersionTest {
    @Test
    public void test() {
        assertTrue(new Version("1.0.0").isNewerThan(new Version("0.9.5")));
        assertFalse(new Version("1.5.10").isNewerThan(new Version("1.5.10")));

        assertTrue(new Version("10.0.57").equals(new Version("10.0.57")));
        assertFalse(new Version("10.0.57").equals(new Version("10.3.57")));
    }
}
