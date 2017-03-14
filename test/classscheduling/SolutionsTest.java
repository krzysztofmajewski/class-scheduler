/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.MATH;
import static classscheduling.Course.MUSIC;
import static classscheduling.Day.MONDAY;
import static classscheduling.Day.TUESDAY;
import static classscheduling.Grade.EIGHT;
import static classscheduling.Grade.NINE;
import static classscheduling.Period.FOURTH;
import static classscheduling.Period.SECOND;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author krzys
 */
public class SolutionsTest {

    private static State state;

    public SolutionsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws SanityCheckException {
        state = testState();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddUnique() throws Exception {
        System.out.println("add unique state");
        Solutions instance = new Solutions();
        Boolean result = instance.addIfNotDuplicate(state);
        assertEquals(result, true);
    }

    @Test
    public void testAddDuplicate() {
        System.out.println("add duplicate state");
        Solutions instance = new Solutions();
        Boolean result = instance.addIfNotDuplicate(state);
        assertEquals(result, true);
        result = instance.addIfNotDuplicate(state);
        assertEquals(result, false);
    }

    @Test
    public void testOverflow() {
        final int savedMax = Solutions.MAX_ENTRIES;
        Solutions.MAX_ENTRIES = 1;
        System.out.println("add too many states");
        Solutions instance = new Solutions();
        Boolean result = instance.addIfNotDuplicate(state);
        assertEquals(result, true);
        result = instance.addIfNotDuplicate(state);
        assertEquals(result, null);
        Solutions.MAX_ENTRIES = savedMax;
    }

    private static State testState() throws SanityCheckException {
        State result = new State();
        result.setCourse(NINE, MONDAY, SECOND, MATH);
        result.setCourse(EIGHT, TUESDAY, FOURTH, MUSIC);
        return result;
    }

}
