/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author krzys
 */
// TODO: review throws clauses
public class Schedule {

    public static final String VERSION = "v4";

    public static final int MILLION = 1000 * 1000;

    final List<Slot> freeSlotList;

    int freeSlots = 60;
    int smallestNumberOfFreeSlots = 60;
    int largestNumberOfFreeSlotsWhenBacktracking = 0;
    long movesSeenInThisGame = 0;
    long movesFailedVetting = 0;

    double[] avgIllegalMovesAtDepth;
    int[] samplesAtDepth;

    HopelessPartialSchedules hopelessPartialSchedules;

    final ScheduleValidator validator;

    public Schedule() {
        validator = new ScheduleValidator(this);
        freeSlotList = initFreeSlotList();
        Course.reset();
        avgIllegalMovesAtDepth = new double[60];
        samplesAtDepth = new int[60];
        hopelessPartialSchedules = new HopelessPartialSchedules(this);
    }

    void print() {
        for (Grade g : Grade.values()) {
            System.out.printf("%10s", g.name() + " |");
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    char c = day.getGradeDay(g).get(period);
                    if (c == 0) {
                        c = '?';
                    }
                    System.out.print(c);
                }
                System.out.print("|");
            }
            System.out.println();
        }
    }

    // returns true if it finds a solution
    boolean scheduleCourses(MovesIterator iterator) throws SanityCheckException {
        movesSeenInThisGame++;
        boolean knownBadMove = !hopelessPartialSchedules.vetThisMove(iterator.depth);
        if (knownBadMove) {
            movesFailedVetting++;
            return false;
        }
        while (iterator.notDone()) {
            Slot slot = (Slot) iterator.move();
            // TODO: should we vet first, then check correctness?
            // check for correctness of current schedule
            validator.reset();
            validator.validateCorrectnessConstraints(slot);
            if (validator.hasErrors()) {
                // no solution from this move, try another slot
                movesSeenInThisGame++;
//                iterator.markMoveAsIllegal();
                iterator.retreat(slot);
                continue;
            }
            // valid move
            boolean vetted = hopelessPartialSchedules.vetThisMove(iterator.depth);
            if (!vetted) {
                movesFailedVetting++;
                iterator.retreat(slot);
                continue;
            }
            if (freeSlots < smallestNumberOfFreeSlots) {
                smallestNumberOfFreeSlots = freeSlots;
                System.out.println("Best move so far: " + slot);
                System.out.println(freeSlots + " free slots left");
                print();
                System.out.println();
                if (freeSlots == 0) {
                    validator.validate();
                    if (validator.hasNoErrors()) {
                        return true;
                    } else {
                        throw new SanityCheckException("If this move is no good, why am I here?");
                    }
                }
            }
            if (enoughPeriodsPerWeek(iterator.currentCourse)) {
                iterator.selectNextCourse();
            }
            MovesIterator subproblemIterator = new MovesIterator(iterator);
            boolean hasSolution = scheduleCourses(subproblemIterator);
            if (hasSolution) {
                return true;
            }
            iterator.badMovesSeen++;
            retreatAndPrintInfoIfNeeded(iterator, subproblemIterator, slot);
        }
        if (freeSlots > 0) {
            // tried all possible moves, did not find solution
            iterator.markMoveAsIllegal();
            return false;
        }
        // zero free slots left
        validator.validate();
        if (validator.hasErrors()) {
            throw new SanityCheckException("Is this situation possible?");
        }
        return true;
    }

    void retreatAndPrintInfoIfNeeded(MovesIterator iterator, MovesIterator subproblemIterator,
            Slot slot) throws SanityCheckException {
        boolean printInfo = false;
        if (freeSlots > largestNumberOfFreeSlotsWhenBacktracking) {
            largestNumberOfFreeSlotsWhenBacktracking = freeSlots;
            System.out.println("\nRetreating from: " + slot);
            printInfo = true;
        }
        iterator.retreat(slot);
        if (printInfo) {
            System.out.print("Retreated from hopeless move, ");
            System.out.println(freeSlots + " free slots");
            printStats();
            print();
            if (iterator.currentCourse == null) {
                System.out.println("This iterator has attempted all of its moves");
            } else {
                System.out.println("Next move for this iterator will try to schedule course: " + iterator.currentCourse);
            }
        }
    }

    void printStats() {
        System.out.println(movesSeenInThisGame + " moves seen in this game");
        System.out.println(movesFailedVetting + " moves failed vetting in this game");
        System.out.println(hopelessPartialSchedules.numAdded
                + " partial schedules added to lookup table");
        System.out.println(hopelessPartialSchedules.numPurged
                + " partial schedules purged from lookup table");
        System.out.println(hopelessPartialSchedules.numOverflowed
                + " partial schedules exceeded lookup table capacity");
        System.out.println(hopelessPartialSchedules.numElements
                + " partial schedules remaining in lookup table");
        System.out.println(hopelessPartialSchedules.maxElements
                + " partial schedules in lookup table at its fullest");
    }

    boolean enoughPeriodsPerWeek(Course course
    ) {
        for (Grade g : Grade.values()) {
            if (course.getPeriodsScheduled(g) < course.periods) {
                return false;
            }
        }
        return true;
    }

    Slot set(Day day, Grade grade,
            Period period, Course course) throws SanityCheckException {
        GradeDay gd = day.getGradeDay(grade);
        Slot s = new Slot();
        s.day = day;
        s.gradeDay = gd;
        s.period = period;
        return set(s, course);
    }

    private ArrayList<Slot> initFreeSlotList() {
        ArrayList<Slot> result = new ArrayList<>();
        for (Grade g : Grade.values()) {
            for (Day day : Day.values()) {
                day.reset();
                for (Period p : Period.values()) {
                    if (day.getGradeDay(g).get(p) == 0) {
                        Slot slot = new Slot();
                        slot.day = day;
                        slot.gradeDay = day.getGradeDay(g);
                        slot.period = p;
                        result.add(slot);
                    }
                }
            }
        }
        return result;
    }

    // TODO: remove sanity check
    void clear(Slot slot) throws SanityCheckException {
        Course course = slot.getCourse();
        course.decrementPeriodsScheduled(slot);
        slot.gradeDay.clear(slot.period);
        freeSlots++;
        if (freeSlotList.contains(slot)) {
            throw new SanityCheckException(slot + " should not be in free slot list");
        }
        freeSlotList.add(slot);
    }

    Slot set(Slot slot, Course course) throws SanityCheckException {
        char c = slot.gradeDay.get(slot.period);
        if (c != 0) {
            throw new SanityCheckException(slot + " already has a course: " + c);
        }
        slot.gradeDay.set(slot.period, course.code);
        freeSlots--;
        if (!freeSlotList.remove(slot)) {
            throw new SanityCheckException(slot + " not found in free slot list");
        }
        course.incrementPeriodsScheduled(slot);
        return slot;
    }

}
