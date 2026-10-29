package net.phoenix.phoenix_fission.integration.emi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FissionEmiUtilsTest {

    @Test
    void formatName_stripsNamespaceAndTitleCases() {
        assertEquals("Infinite Moderator", FissionEmiUtils.formatName("phoenix_fission:infinite_moderator"));
    }

    @Test
    void formatName_worksWithoutNamespace() {
        assertEquals("Infinite Moderator", FissionEmiUtils.formatName("infinite_moderator"));
    }

    @Test
    void formatName_handlesSingleWord() {
        assertEquals("Water", FissionEmiUtils.formatName("water"));
    }

    @Test
    void formatName_handlesEmptyAndNull() {
        assertEquals("", FissionEmiUtils.formatName(""));
        assertEquals("", FissionEmiUtils.formatName(null));
    }

    @Test
    void formatName_collapsesRepeatedUnderscores() {
        assertEquals("Odd Name", FissionEmiUtils.formatName("odd__name"));
    }

    @Test
    void tierColor_clampsToValidRange() {
        assertEquals(FissionEmiUtils.tierColor(1), FissionEmiUtils.tierColor(0));
        assertEquals(FissionEmiUtils.tierColor(1), FissionEmiUtils.tierColor(-5));

        assertEquals(FissionEmiUtils.tierColor(6), FissionEmiUtils.tierColor(99));
    }

    @Test
    void tierColor_isDistinctPerTier() {
        int t1 = FissionEmiUtils.tierColor(1);
        int t2 = FissionEmiUtils.tierColor(2);
        int t3 = FissionEmiUtils.tierColor(3);
        int t4 = FissionEmiUtils.tierColor(4);
        int t5 = FissionEmiUtils.tierColor(5);
        int t6 = FissionEmiUtils.tierColor(6);

        var colors = java.util.Set.of(t1, t2, t3, t4, t5, t6);
        assertEquals(6, colors.size(), "each tier should have its own distinct color");
    }
}
