/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author krzys
 */
// TODO: review throws clauses
public class Schedule {

    public static final int MILLION = 1000000;

    int movesTried;
    int freeSlots = 60;

    final ValidationErrors errors;

    final Stack<Slot> history;

    private ScheduleValidator validator;

    public Schedule() {

        errors = new ValidationErrors();

        history = new Stack<>();

        validator = new ScheduleValidator(this);
    }

    boolean fillSchedule() throws SanityCheckException {
        return fillSchedule(null, initFreeSlotList());
    }

    // TODO make smart decisions based on which constraint failed
    private boolean fillSchedule(Slot lastFilledSlot, List<Slot> freeSlotList) throws SanityCheckException {
        movesTried++;
        // print a status update every once in a while
        if ((movesTried % MILLION) == 1) {
            System.out.println(freeSlots + " free slots left after "
                    + movesTried / MILLION + " million moves:");
            print();
            if (freeSlots == 0) {
                errors.clear();
                validate();
                errors.print();
            }
        }
        // check if we are done
        if (freeSlots == 0) {
            errors.clear();
            // TODO: should we validate the whole schedule?
            validate(lastFilledSlot);
            if (errors.isEmpty()) {
                return true;
            }
        }
        errors.clear();
        // check for correctness of current schedule
        validator.validateCorrectnessConstraints(lastFilledSlot);
        if (errors.hasErrors()) {
            // adding more slots won't help us
            return false;
        }
        // schedule correct but not complete
        for (Slot slot : freeSlotList) {
            Course course = null;
            Course actualCourse = null;
            if (lastFilledSlot == null) {
                fillSlotWithNextAvailableCourse(slot);
            } else {
                char c = lastFilledSlot.gradeDay.get(lastFilledSlot.period);
                course = Course.forCode(c);
                actualCourse = fillSlotWithCourse(slot, course);
                if (!actualCourse.equals(course)) {
                    // nop
                    boolean nop = true;
                }
            }
            List<Slot> smallerFreeSlotList = new ArrayList<>(freeSlotList);
            boolean ok = smallerFreeSlotList.remove(slot);
            if (!ok) {
                throw new SanityCheckException(slot + " not found in free slot list");
            }
            boolean success = fillSchedule(slot, smallerFreeSlotList);
            // if the recursive call succeeded, we are done!
            if (success) {
                return true;
            }
            // no solution from this move, try another slot
            clear(slot);
        }
        // no empty slot yields a winner
        return false;
    }

    void set(Day day, Grade grade, Period period, Course course) throws SanityCheckException {
        GradeDay gd = day.getGradeDay(grade);
        Slot s = new Slot();
        s.day = day;
        s.gradeDay = gd;
        s.period = period;
        set(s, course);
    }

    private void set(Slot slot, Course course) throws SanityCheckException {
        char c = slot.gradeDay.get(slot.period);
        if (c != 0) {
            throw new SanityCheckException(slot + " already has a course: " + c);
        }
        slot.gradeDay.set(slot.period, course.code);
        freeSlots--;
        history.push(slot);
        course.incrementPeriodsScheduled(slot);
    }

    void clear(Slot slot) throws SanityCheckException {
        Course course = slot.getCourse();
        course.decrementPeriodsScheduled(slot);
        slot.gradeDay.clear(slot.period);
        freeSlots++;
        Slot lastFilledSlot = history.pop();
        if (!lastFilledSlot.equals(slot)) {
            throw new SanityCheckException("last filled is " + lastFilledSlot
                    + ", expected " + slot);
        }
    }

    private List<Slot> initFreeSlotList() {
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

    void validate() {
        validator.validateCorrectnessConstraints();
        validator.validateCompletenessConstraints();
    }

    void validate(Slot lastFilledSlot) throws SanityCheckException {
        validator.validateCorrectnessConstraints(lastFilledSlot);
        validator.validateCompletenessConstraints(lastFilledSlot);
    }

    Course todo() {
        Course result = null;
        errors.clear();
        for (Course course : Course.values()) {
            if (!enoughPeriodsPerWeek(course)) {
                result = course;
                break;
            }
        }
        return result;
    }

    Course fillSlotWithNextAvailableCourse(Slot slot) throws SanityCheckException {
        Course c = todo();
        set(slot, c);
        return c;
    }

    Course fillSlotWithCourse(Slot slot, Course course) throws SanityCheckException {
        Course result = course;
        errors.clear();
        if (enoughPeriodsPerWeek(course)) {
            // already enough periods
            result = fillSlotWithNextAvailableCourse(slot);
        } else {
            // not enough periods yet, fill the slot
            set(slot, course);
        }
        return result;
    }

    boolean enoughPeriodsPerWeek(Course course) {
        for (Grade g : Grade.values()) {
            if (course.getPeriodsScheduled(g) < course.periods) {
                errors.add(g + ": not enough periods of " + course.name);
                return false;
            }
        }
        return true;

//        for (Grade g : Grade.values()) {
//            int gradeCount = 0;
//            for (Day day : Day.values()) {
//                GradeDay gd = day.getGradeDay(g);
//                gradeCount += gd.count(course.code);
//            }
//            if (gradeCount < course.periods) {
//                errors.add(g + ": not enough periods of " + course.name);
//                return false;
//            }
//        }
//        return true;
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

}
