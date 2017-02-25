/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.YesNoMaybe.MAYBE;
import static classscheduling.YesNoMaybe.NO;
import static classscheduling.YesNoMaybe.YES;
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
    int largestNumberOfFreeSlotsWhenBacktracking = 0;
    long largestNumberOfMovesPrunedInAWhileLoop = 0;

    final ScheduleValidator validator;

    public Schedule() {
        validator = new ScheduleValidator(this);
        freeSlotList = initFreeSlotList();
        Course.reset();
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
        while (iterator.notDone()) {
            if (iterator.takingTooLong()) {
                return false;
            }
            Slot slot = (Slot) iterator.move();
            // check for correctness of current schedule
            validator.reset();
            validator.validateCorrectnessConstraints(slot);
            if (validator.hasErrors()) {
                // no solution from this move, try another slot
                iterator.illegalMovesTried++;
                iterator.retreat(slot);
                continue;
            }
            // valid move
//            System.out.println("Valid move: " + slot);
//            print();
            if (enoughPeriodsPerWeek(iterator.currentCourse)) {
                iterator.selectNextCourse();
            }
            MovesIterator subproblemIterator = new MovesIterator(iterator);
            boolean hasSolution = scheduleCourses(subproblemIterator);
            if (hasSolution) {
                return true;
            }
            // exhaustive search failed, or move took too long
            if (!subproblemIterator.takingTooLong()) {
                iterator.illegalMovesTried++;
            }
            retreatAndPrintInfoIfNeeded(iterator, subproblemIterator, slot);
        }
        if (freeSlots > 0) {
            if (Course.values().length == 5) {
                return false;
            }
        }
        // no free slots left, or else we are trying a partial schedule
        validator.validate();
        if (validator.hasErrors()) {
            iterator.illegalMovesTried++;
            return false;
        }
        return true;
    }

    void retreatAndPrintInfoIfNeeded(MovesIterator iterator, MovesIterator subproblemIterator, Slot slot) throws SanityCheckException {
        boolean printInfo = false;
        if (freeSlots > largestNumberOfFreeSlotsWhenBacktracking) {
            largestNumberOfFreeSlotsWhenBacktracking = freeSlots;
            System.out.println("\nRetreating from: " + slot);
            printInfo = true;
        }
        iterator.retreat(slot);
        if (printInfo) {
            if (subproblemIterator.takingTooLong()) {
                System.out.print("Retreated from slow move, ");
            } else {
                System.out.print("Retreated from hopeless move, ");
            }
            System.out.println(freeSlots + " free slots");
            print();
            if (iterator.currentCourse == null) {
                System.out.println("This iterator has attempted all of its moves");
            } else {
                System.out.println("Next move for this iterator will try to schedule course: " + iterator.currentCourse);
            }
        }
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

//    private void printProgress(Slot slot, Course course, MovesIterator iterator) {
//        // print a status update every once in a while
////        if ((movesTried % MILLION) == 1) {
//        System.out.println("[" + VERSION + "] " + freeSlots + " free slots left after "
//                + legalMovesTried + " legal moves:");
//        print();
////            validator.reset();
////            validator.validate();
////            validator.printErrors();
////            System.out.println(iterator.repeatedBadMoves + " known bad moves made on this solution path (" 
////            + iterator.totalMoves + " total)");
//        System.out.println(slot);
////    }
//    }
}
