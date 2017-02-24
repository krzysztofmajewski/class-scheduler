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
    YesNoMaybe scheduleCourses(MovesIterator iterator) throws SanityCheckException {
        while (iterator.notDone()) {
            Slot slot = (Slot) iterator.move();
            // check for correctness of current schedule
            validator.reset();
            validator.validateCorrectnessConstraints(slot);
            if (validator.hasErrors()) {
                // no solution from this move, try another slot
                iterator.retreat(slot);
                continue;
            }
            // valid move
            if (enoughPeriodsPerWeek(iterator.currentCourse)) {
                iterator.selectNextCourse();
            }
            MovesIterator subproblemIterator = new MovesIterator(iterator);
            YesNoMaybe hasSolution = scheduleCourses(subproblemIterator);
            processResult(hasSolution, iterator, subproblemIterator, slot);
            if (hasSolution.equals(YES)) {
                return YES;
            }
        }
        if (iterator.totalMovesFromMaybes > 0) {
            // now we can add these guys back
            iterator.totalMovesTried += iterator.totalMovesFromMaybes;
        }
        if (iterator.takingTooLong()) {
            // this iterator has searched too many moves
            return MAYBE;
        }
        // this iterator searched all of its candidates moves
        // none of them returned MAYBE
        // iterator tried all its moves
        if (freeSlots > 0) {
            if (Course.values().length == 5) {
                return NO;
            }
        }
        // no free slots left, or else we are in test mode
        validator.validate();
        if (validator.hasErrors()) {
            return NO;
        }
        return YES;
    }

    void processResult(YesNoMaybe hasSolution, MovesIterator iterator, MovesIterator subproblemIterator, Slot slot) throws SanityCheckException {
        switch (hasSolution) {
            case YES:
                iterator.totalMovesTried += subproblemIterator.totalMovesTried;
                break;
            case MAYBE:
                // do not add these to our moves count yet
                // we want to continue trying the rest of our candidate moves
                iterator.totalMovesFromMaybes += subproblemIterator.totalMovesTried;
                iterator.retreat(slot);
                break;
            case NO:
                iterator.totalMovesTried += subproblemIterator.totalMovesTried;
                boolean printRetreatInfo = false;
                if (freeSlots > largestNumberOfFreeSlotsWhenBacktracking) {
                    largestNumberOfFreeSlotsWhenBacktracking = freeSlots;
                    System.out.println("\nRetreating from: " + slot);
                    printRetreatInfo = true;
                }
                iterator.retreat(slot);
                if (printRetreatInfo) {
                    System.out.println("Retreated from hopeless move, " + freeSlots + " free slots");
                    print();
                    System.out.println(subproblemIterator.totalMovesTried
                            + " legal and illegal moves exhaustively searched in this subproblem");
                    System.out.println(iterator.totalMovesTried
                            + " legal and illegal moved exhaustively searched in this bounded game");
                    if (iterator.currentCourse == null) {
                        System.out.println("This iterator has attempted all of its moves");
                    } else {
                        System.out.println("Next move for this iterator will try to schedule course: " + iterator.currentCourse);
                    }
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
