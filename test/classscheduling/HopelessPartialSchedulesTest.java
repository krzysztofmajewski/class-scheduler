/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.ART;
import static classscheduling.Course.MATH;
import static classscheduling.Course.MUSIC;
import static classscheduling.Day.MONDAY;
import static classscheduling.Day.TUESDAY;
import static classscheduling.Day.WEDNESDAY;
import static classscheduling.Grade.EIGHT;
import static classscheduling.Grade.NINE;
import static classscheduling.Period.FOURTH;
import static classscheduling.Period.SECOND;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author krzys
 */
public class HopelessPartialSchedulesTest {

    private static Schedule schedule;

    public HopelessPartialSchedulesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws SanityCheckException {
        schedule = testSchedule();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddThisPartialScheduleAndFindIt() throws Exception {
        System.out.println("add and find");
        addOneFindOne();
    }

    @Test
    public void testPurge() throws SanityCheckException {
        System.out.println("purge");
        HopelessPartialSchedules instance = addOneFindOne();
        // clear a slot to create a sub-pattern of current BoardState
        clearMondayGradeNineSecondPeriod();
        // TODO: all this manipulating of the depth is annoying
        int depth = 60 - schedule.freeSlots;
        BoardState subPattern = new BoardState(schedule.freeSlotList, depth);
        // add the sub-pattern -- this should trigger a purge
        instance.addThisPartialSchedule(depth);
        // assert that one element got purged, since it was a super-pattern
        assertEquals(instance.numElements, 1);
        assertEquals(instance.numPurged, 1);
        assertEquals(instance.numAdded, 2);
        // find initial state, to make sure we purged the right thing
        BoardState found = instance.find(subPattern);
        assertEquals(found, subPattern);
    }

    @Test
    public void testFindReturnsSubpattern() throws SanityCheckException {
        System.out.println("find returns sub-pattern");
        HopelessPartialSchedules instance = addOneFindOne();
        int depth = 60 - schedule.freeSlots;
        // save initial state
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        // fill a slot
        schedule.set(WEDNESDAY, NINE, SECOND, ART);
        depth = 60 - schedule.freeSlots;
        // add the super-pattern
        instance.addThisPartialSchedule(depth);
        BoardState superPattern = new BoardState(schedule.freeSlotList, depth);
        assertEquals(instance.numElements, 2);
        assertEquals(instance.numAdded, 2);
        assertEquals(instance.numPurged, 0);
        BoardState found = instance.find(superPattern);
        // find() should return the sub-pattern
        assertEquals(found, bs);
    }

    /**
     * Test of vetThisMove method returning false
     *
     * @throws classscheduling.SanityCheckException
     */
    @Test
    public void testVetThisMoveFails() throws SanityCheckException {
        System.out.println("vetThisMove returns false");
        // add one BoardState
        HopelessPartialSchedules instance = addOneFindOne();
        int depth = 60 - schedule.freeSlots;
        boolean expResult = false;
        boolean result = instance.vetThisMove(depth);
        assertEquals(expResult, result);
    }

    /**
     * Test of vetThisMove method returning true
     *
     * @throws classscheduling.SanityCheckException
     */
    @Test
    public void testVetThisMoveSucceeds() throws SanityCheckException {
        System.out.println("vetThisMove returns true");
        boolean expResult = true;
        // create an instance containing one partial schedule...
        HopelessPartialSchedules instance = new HopelessPartialSchedules(schedule);
        // ...construct a move that is not a super-pattern of that partial schedule 
        clearMondayGradeNineSecondPeriod();
        int depth = 60 - schedule.freeSlots;
        boolean result = instance.vetThisMove(depth);
        assertEquals(expResult, result);
    }

    // clear the slot set in this schedule
    private void clearMondayGradeNineSecondPeriod() throws SanityCheckException {
        Slot slot = new Slot();
        Day day = MONDAY;
        slot.day = day;
        Grade grade = NINE;
        slot.gradeDay = day.getGradeDay(grade);
        slot.period = SECOND;
        schedule.clear(slot);
    }

    private static Schedule testSchedule() throws SanityCheckException {
        Schedule result = new Schedule();
        result.set(MONDAY, NINE, SECOND, MATH);
        result.set(TUESDAY, EIGHT, FOURTH, MUSIC);
        return result;
    }

    private HopelessPartialSchedules addOneFindOne() throws SanityCheckException {
        HopelessPartialSchedules instance = new HopelessPartialSchedules(schedule);
        final int depth = 60 - schedule.freeSlots;
        instance.addThisPartialSchedule(depth);
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        BoardState found = instance.find(bs);
        if (null == found) {
            fail("BoardState added but not found.");
        } else if (!found.equals(bs)) {
            fail("BoardState added but find() found the wrong one.");
        }
        return instance;
    }

}
