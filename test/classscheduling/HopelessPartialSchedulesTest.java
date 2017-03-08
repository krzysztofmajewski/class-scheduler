/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.ART;
import static classscheduling.Course.ENGLISH;
import static classscheduling.Course.FRENCH;
import static classscheduling.Course.GEOGRAPHY;
import static classscheduling.Course.MATH;
import static classscheduling.Course.MUSIC;
import static classscheduling.Day.FRIDAY;
import static classscheduling.Day.MONDAY;
import static classscheduling.Day.THURSDAY;
import static classscheduling.Day.TUESDAY;
import static classscheduling.Day.WEDNESDAY;
import static classscheduling.Grade.EIGHT;
import static classscheduling.Grade.NINE;
import static classscheduling.Grade.SEVEN;
import static classscheduling.Period.FIRST;
import static classscheduling.Period.FOURTH;
import static classscheduling.Period.SECOND;
import static classscheduling.Period.THIRD;
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
        clear(MONDAY, NINE, SECOND);
        // TODO: all this manipulating of the depth is annoying
        int depth = 60 - schedule.freeSlots;
        // add the sub-pattern -- this should trigger a purge
        BoardState subPattern = instance.addThisPartialSchedule(depth);
        // assert that one element got purged, since it was a super-pattern
        assertEquals(instance.numElements, 1);
        assertEquals(instance.numPurged, 1);
        assertEquals(instance.numAdded, 2);
        // find initial state, to make sure we purged the right thing
        BoardState found = instance.findThisPatternOrSubpatternThereof(subPattern);
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
        BoardState superPattern = instance.addThisPartialSchedule(depth);
        assertEquals(instance.numElements, 2);
        assertEquals(instance.numAdded, 2);
        assertEquals(instance.numPurged, 0);
        BoardState found = instance.findThisPatternOrSubpatternThereof(superPattern);
        // find() should return the sub-pattern
        assertEquals(found, bs);
    }

    @Test
    public void testVetThisMoveReturnsFalse() throws SanityCheckException {
        System.out.println("vetThisMove returns false");
        // add one BoardState
        HopelessPartialSchedules instance = addOneFindOne();
        int depth = 60 - schedule.freeSlots;
        boolean expResult = false;
        boolean result = instance.vetThisMove(depth);
        assertEquals(expResult, result);
    }

    @Test
    public void testVetThisMoveReturnsTrue() throws SanityCheckException {
        System.out.println("vetThisMove returns true");
        boolean expResult = true;
        // create an instance containing one partial schedule...
        HopelessPartialSchedules instance = new HopelessPartialSchedules(schedule);
        // ...construct a move that is not a super-pattern of that partial schedule
        clear(MONDAY, NINE, SECOND);
        int depth = 60 - schedule.freeSlots;
        boolean result = instance.vetThisMove(depth);
        assertEquals(expResult, result);
    }

    @Test
    public void testAddAndPurgeUpdateLinksCorrectly() throws SanityCheckException {
        HopelessPartialSchedules instance = addOneFindOne();
        int depth = 60 - schedule.freeSlots;
        assertEquals(depth, 2);
        BoardState tmpMathMusic = new BoardState(schedule.freeSlotList, depth);
        BoardState mathMusic = instance.findThisPatternOrSubpatternThereof(tmpMathMusic);

        clear(TUESDAY, EIGHT, FOURTH);
        depth = 60 - schedule.freeSlots;
        assertEquals(depth, 1);
        schedule.set(WEDNESDAY, SEVEN, THIRD, FRENCH);
        depth = 60 - schedule.freeSlots;
        assertEquals(depth, 2);
        BoardState mathFrench = instance.addThisPartialSchedule(depth);

        schedule.set(WEDNESDAY, SEVEN, FOURTH, ART);
        depth = 60 - schedule.freeSlots;
        assertEquals(depth, 3);
        BoardState mathFrenchArt = instance.addThisPartialSchedule(depth);

        clear(WEDNESDAY, SEVEN, THIRD);
        depth = 60 - schedule.freeSlots;
        assertEquals(depth, 2);
        BoardState mathArt = instance.addThisPartialSchedule(depth);

        // should have been purged when adding mathArt
        BoardState found = instance.findThisPatternOrSubpatternThereof(mathFrenchArt);
        assertTrue(found.equals(mathArt) || found.equals(mathFrench));

        assertEquals(mathArt.depth, 2);
        assertEquals(mathFrench.depth, 2);
        assertEquals(mathMusic.depth, 2);

        // unschedule ART and add some other courses
        clear(WEDNESDAY, SEVEN, FOURTH);
        // now it's just MATH
        schedule.set(THURSDAY, NINE, FIRST, GEOGRAPHY);
        schedule.set(FRIDAY, NINE, FIRST, MUSIC);
        depth = 60 - schedule.freeSlots;
        assertEquals(depth, 3);
        // unrealistic because a sub-pattern already exists
        //   but at least this way, the super-pattern won't get purged
        //   since purging happens only when adding a sub-pattern
        //   the goal here is just to validate the links
        BoardState mathGeographyMusic = instance.addThisPartialSchedule(depth);
        assertEquals(mathGeographyMusic.depth, 3);

        // unschedule MUSIC
        clear(FRIDAY, NINE, FIRST);
        // unschedule GEOGRAPHY
        clear(THURSDAY, NINE, FIRST);
        // unschedule MATH
        clear(MONDAY, NINE, SECOND);
        // schedule ENGLISH
        schedule.set(MONDAY, EIGHT, FOURTH, ENGLISH);
        depth = 60 - schedule.freeSlots;
        assertEquals(depth, 1);
        BoardState english = instance.addThisPartialSchedule(depth);
        assertEquals(english.depth, 1);
    }

    private void clear(Day day, Grade grade, Period period) throws SanityCheckException {
        Slot slot = new Slot();
        slot.day = day;
        slot.gradeDay = day.getGradeDay(grade);
        slot.period = period;
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
        BoardState bs = instance.addThisPartialSchedule(depth);
        BoardState found = instance.findThisPatternOrSubpatternThereof(bs);
        if (null == found) {
            fail("BoardState added but not found.");
        } else if (!found.equals(bs)) {
            fail("BoardState added but find() found the wrong one.");
        }
        return instance;
    }

}
