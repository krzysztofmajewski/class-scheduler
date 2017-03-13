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

    private static State state;

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
        state = testState();
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
    public void testFindSubPatternAndPurge() throws SanityCheckException {
        System.out.println("purge super-pattern and find sub-pattern");
        HopelessPartialSchedules instance = addOneFindOne();
        // save the state because it will change
        State savedState = new State(state);
        // clear a slot to create a sub-pattern
        state.clear(NINE, MONDAY, SECOND);
        // add the sub-pattern -- this should trigger a purge
        instance.markThisPartialScheduleAsHopeless(state);
        // assert that one element got purged, since it was a super-pattern
        assertEquals(instance.numElements, 1);
        assertEquals(instance.numPurged, 1);
        assertEquals(instance.numAdded, 2);
        // find saved state, to make sure we purged the right thing
        State found = instance.findThisPatternOrSubPatternThereof(savedState);
        assertEquals(found, state);
    }

    @Test
    public void testFindReturnsSubPattern() throws SanityCheckException {
        System.out.println("find returns sub-pattern");
        HopelessPartialSchedules instance = addOneFindOne();
        // save state
        State subPattern = new State(state);
        // fill a slot
        state.setCourse(NINE, WEDNESDAY, SECOND, ART);
        // add the current state
        instance.markThisPartialScheduleAsHopeless(state);
        assertEquals(instance.numElements, 2);
        assertEquals(instance.numAdded, 2);
        assertEquals(instance.numPurged, 0);
        State found = instance.findThisPatternOrSubPatternThereof(state);
        // find() should return the sub-pattern
        assertEquals(found, subPattern);
    }

    @Test
    public void testFindReturnsNull() throws SanityCheckException {
        System.out.println("find returns null");
        // create an instance containing one partial schedule...
        HopelessPartialSchedules instance = addOneFindOne();
        // ...construct a state that is not a super-pattern of that partial schedule
        state.clear(NINE, MONDAY, SECOND);
        State result = instance.findThisPatternOrSubPatternThereof(state);
        assertNull(result);
    }

    @Test
    public void testAddAndPurge() throws SanityCheckException {
        System.out.println("add and purge");
        HopelessPartialSchedules instance = addOneFindOne();
        State mathMusic = new State(state);

        state.clear(EIGHT, TUESDAY, FOURTH);
        state.setCourse(SEVEN, WEDNESDAY, THIRD, FRENCH);
        
        State mathFrench = new State(state);
        instance.markThisPartialScheduleAsHopeless(mathFrench);

        state.setCourse(SEVEN, WEDNESDAY, FOURTH, ART);
        State mathFrenchArt = new State(state);
        instance.markThisPartialScheduleAsHopeless(mathFrenchArt);

        state.clear(SEVEN, WEDNESDAY, THIRD);
        State mathArt = new State(state);
        instance.markThisPartialScheduleAsHopeless(mathArt);

        // should have been purged when adding mathArt
        State found = instance.findThisPatternOrSubPatternThereof(mathFrenchArt);
        assertTrue(found.equals(mathArt) || found.equals(mathFrench));

        // unschedule ART and add some other courses
        state.clear(SEVEN, WEDNESDAY, FOURTH);
        // now it's just MATH
        state.setCourse(NINE, THURSDAY, FIRST, GEOGRAPHY);
        state.setCourse(NINE, FRIDAY, FIRST, MUSIC);
        // unrealistic because a sub-pattern already exists
        //   but at least this way, the super-pattern won't get purged
        //   since purging happens only when adding a sub-pattern
        State mathGeographyMusic = new State(state);
        instance.markThisPartialScheduleAsHopeless(mathGeographyMusic);
        
        found = instance.findThisPatternOrSubPatternThereof(mathGeographyMusic);
        assertEquals(found, mathGeographyMusic);

        // unschedule MUSIC
        state.clear(NINE, FRIDAY, FIRST);
        // unschedule GEOGRAPHY
        state.clear(NINE, THURSDAY, FIRST);
        // unschedule MATH
        state.clear(NINE, MONDAY, SECOND);
        // schedule ENGLISH
        state.setCourse(EIGHT, MONDAY, FOURTH, ENGLISH);
        State english = new State(state);
        instance.markThisPartialScheduleAsHopeless(english);
        
        found = instance.findThisPatternOrSubPatternThereof(english);
        assertEquals(found, english);
    }

    private static State testState() throws SanityCheckException {
        State result = new State();
        result.setCourse(NINE, MONDAY, SECOND, MATH);
        result.setCourse(EIGHT, TUESDAY, FOURTH, MUSIC);
        return result;
    }

    private HopelessPartialSchedules addOneFindOne() throws SanityCheckException {
        HopelessPartialSchedules instance = new HopelessPartialSchedules();
        instance.markThisPartialScheduleAsHopeless(state);
        State found = instance.findThisPatternOrSubPatternThereof(state);
        if (null == found) {
            fail("BoardState added but not found.");
        } else if (!found.equals(state)) {
            fail("BoardState added but find() found the wrong one.");
        }
        return instance;
    }

}
