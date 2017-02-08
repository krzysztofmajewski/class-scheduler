/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author krzys
 */
// TODO: review throws clauses
public class Schedule {

    public static final String VERSION = "v2";
    
    public static final int MILLION = 1000000;

    long movesTried;
    int freeSlots = 60;

    final ValidationErrors errors;

    final Stack<Slot> history;

    private ScheduleValidator validator;

    public Schedule() {

        errors = new ValidationErrors();

        history = new Stack<>();

        validator = new ScheduleValidator(this);

    }

    // TODO make smart decisions based on which constraint failed
    boolean fillSchedule(Slot lastFilledSlot, List<Slot> freeSlotList, int coursesFilled) throws SanityCheckException {
        movesTried++;
        // print a status update every once in a while
        if ((movesTried % MILLION) == 1) {
            System.out.println("[" + VERSION + "] " + freeSlots + " free slots left after "
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
//            validate(lastFilledSlot);
            validate();
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
        if (freeSlotList == null) {
            freeSlotList = initFreeSlotList();
        }
        // schedule correct but not complete
        for (Slot slot : freeSlotList) {
            slot = tryNextFreeSlotForCurrentCourse(lastFilledSlot, slot, coursesFilled);
            if (slot == null) {
                // no more courses left, but still slots left
                // TODO: can this ever happen?
                return false;
            }
            // slot not null
            List<Slot> smallerFreeSlotList = new ArrayList<>(freeSlotList);
            boolean ok = smallerFreeSlotList.remove(slot);
            if (!ok) {
                throw new SanityCheckException(slot + " not found in free slot list");
            }
            int newCoursesFilled = coursesFilled;
            boolean sameCourse = lastFilledSlot.getCourse().equals(slot.getCourse());
            if (!sameCourse) {
                newCoursesFilled++;
            }
            boolean success = fillSchedule(slot, smallerFreeSlotList, newCoursesFilled);
            // if the recursive call succeeded, we are done!
            if (success) {
                return true;
            }
            // no solution from this move, try another slot
            clear(slot);
        }
        // no slot can be filled
        return false;
    }

    Slot set(Day day, Grade grade, Period period, Course course) throws SanityCheckException {
        GradeDay gd = day.getGradeDay(grade);
        Slot s = new Slot();
        s.day = day;
        s.gradeDay = gd;
        s.period = period;
        return set(s, course);
    }

    private Slot set(Slot slot, Course course) throws SanityCheckException {
        char c = slot.gradeDay.get(slot.period);
        if (c != 0) {
            throw new SanityCheckException(slot + " already has a course: " + c);
        }
        slot.gradeDay.set(slot.period, course.code);
        freeSlots--;
        history.push(slot);
        course.incrementPeriodsScheduled(slot);
        return slot;
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

    private List<Course> initFreeCourseList() {
        ArrayList<Course> result = new ArrayList<>();
        result.addAll(Arrays.asList(Course.values()));
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

    private Slot tryNextFreeSlotForCurrentCourse(Slot lastFilledSlot,
            Slot slotToFill,
            int coursesFilled) throws SanityCheckException {
        Course currentCourse = lastFilledSlot.getCourse();
        if (enoughPeriodsPerWeek(currentCourse)) {
            coursesFilled++;
            if (coursesFilled >= Course.values().length) {
                return null;
            }
            currentCourse = Course.values()[coursesFilled];
        }
        return set(slotToFill, currentCourse);
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
