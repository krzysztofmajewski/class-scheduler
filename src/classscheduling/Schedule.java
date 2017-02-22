/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;

/**
 *
 * @author krzys
 */
// TODO: review throws clauses
public class Schedule {

    public static final String VERSION = "v4";

    public static final int MILLION = 1000000;

    long movesTried;
    int freeSlots = 60;

    final ScheduleValidator validator;

    public Schedule() {
        validator = new ScheduleValidator(this);
    }

    void print() {
        for (Grade g : Grade.values()) {
            System.out.printf("%10s", g.name() + " |");
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    System.out.print(day.getGradeDay(g).get(period));
                }
                System.out.print("|");
            }
            System.out.println();
        }
    }

    // returns true if it finds a solution
    boolean scheduleCourses(MovesIterator iterator) throws SanityCheckException {
        // suppose there exists a valid solution S which schedules course C in configuration K
        //   then it should not matter if C is the first course to be scheduled, or the second, or third, etc.
        //   but this is only true if we can iterate over all configurations of a given course
        // returns a slot filled with a course that has not yet been tried in that slot
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
            printProgress(slot, slot.getCourse(), iterator);
            if (iterator.takingTooLong()) {
                iterator.retreat(slot);
                continue;
            }
            // make a copy of the iterator, run recursive call with that
            // this way we discard the bad moves when we backtrack
            boolean hasSolution = scheduleCourses(new MovesIterator(iterator));
            if (hasSolution) {
                return true;
            }
            // no solution from this configuration
            // roll back
            iterator.retreat(slot);
        }
        validator.validate();
        return validator.hasNoErrors();
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

    ArrayList<Slot> initFreeSlotList() {
        ArrayList<Slot> result = new ArrayList<>();
        for (Day day : Day.values()) {
            for (Grade g : Grade.values()) {
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
    }

    Slot set(Slot slot, Course course) throws SanityCheckException {
        char c = slot.gradeDay.get(slot.period);
        if (c != 0) {
            throw new SanityCheckException(slot + " already has a course: " + c);
        }
        slot.gradeDay.set(slot.period, course.code);
        freeSlots--;
        course.incrementPeriodsScheduled(slot);
        return slot;
    }

    private void printProgress(Slot slot, Course course, MovesIterator iterator) {
        movesTried++;
        // print a status update every once in a while
        int THOUSAND = MILLION / 1000;
        if ((movesTried % MILLION) == 1) {
            System.out.println("[" + VERSION + "] " + freeSlots + " free slots left after "
                    + movesTried / MILLION + " million legal moves:");
            print();
            validator.reset();
            validator.validate();
            validator.printErrors();
            System.out.println(iterator.repeatedBadMoves + " known bad moves made on this solution path (" 
            + iterator.totalMoves + " total)");
            System.out.println(slot);
        }
    }

}
