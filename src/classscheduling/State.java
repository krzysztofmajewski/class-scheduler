/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.FRENCH;
import static classscheduling.Util.computeIndex;
import java.util.Arrays;

/**
 *
 * @author krzys
 */
public class State {

    int depth;
    char board[];
    int hits;

    State() {
        board = new char[60];
    }

    public State(State bs) {
        board = new char[60];
        int index = 0;
        for (Grade grade : Grade.values()) {
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    char c = bs.getCourse(grade, day, period);
                    if (c != 0) {
                        depth++;
                        board[index] = c;
                    }
                    index++;
                }
            }
        }
    }

    // TODO: optimize?
    char getCourse(Grade grade, Day day, Period period) {
        int index = computeIndex(grade, day, period);
        return board[index];
    }

    boolean isScheduled(Course course,
            Grade grade,
            Day day,
            Period period) {
        Course actualCourse = Course.forCode(getCourse(grade, day, period));
        return (actualCourse != null) && (actualCourse.equals(course));
    }

    char getCourse(Move move) {
        return getCourse(move.slot.grade, move.slot.day, move.slot.period);
    }

    // TODO: remove sanity check
    void setCourse(Grade grade, Day day, Period period, Course course) throws SanityCheckException {
        char c = getCourse(grade, day, period);
        if (c != 0) {
            Slot slot = new Slot(grade, day, period);
            Course oldCourse = Course.forCode(c);
            throw new SanityCheckException(slot + " already contains course " + oldCourse);
        }
        int index = computeIndex(grade, day, period);
        board[index] = course.code;
        depth++;
        if (!course.equals(FRENCH)) {
            course.incrementPeriodsScheduled(grade, day);
        } else {
            incrementFrenchPeriodsScheduled(grade, day, period);
        }
    }

    void setCourse(Move move) throws SanityCheckException {
        setCourse(move.slot.grade, move.slot.day, move.slot.period, move.course);
    }

    void clear(Grade grade, Day day, Period period) throws SanityCheckException {
        char c = getCourse(grade, day, period);
        if (c == 0) {
            Slot slot = new Slot(grade, day, period);
            throw new SanityCheckException(slot + " already cleared");
        }
        int index = computeIndex(grade, day, period);
        board[index] = 0;
        depth--;
        Course course = Course.forCode(c);
        if (!course.equals(FRENCH)) {
            course.decrementPeriodsScheduled(grade, day);
        } else {
            decrementFrenchPeriodsScheduled(grade, day, period);
        }
    }

    void clearCourse(Slot slot) throws SanityCheckException {
        clear(slot.grade, slot.day, slot.period);
    }

    @Override
    public boolean equals(Object aThat) {
        if (this == aThat) {
            return true;
        }
        if (!(aThat instanceof State)) {
            return false;
        }
        State that = (State) aThat;
        return (that.depth == this.depth)
                && Arrays.equals(that.board, this.board);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.hashCode(this.board);
        return hash;
    }

    boolean isSubPatternOf(State bs) {
        for (int i = 0; i < board.length; i++) {
            char c = board[i];
            if (c != 0) {
                if (bs.board[i] != c) {
                    return false;
                }
            }
        }
        return true;
    }

    void print() {
        for (Grade grade : Grade.values()) {
            System.out.printf("%10s", grade.name() + " |");
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    char c = getCourse(grade, day, period);
                    if (c == 0) {
                        System.out.print('?');
                    } else {
                        System.out.print(c);
                    }
                }
                System.out.print("|");
            }
            System.out.println();
        }
        System.out.println();
    }

    private void incrementFrenchPeriodsScheduled(Grade grade, Day day, Period period) throws SanityCheckException {
        boolean alreadyScheduledInThisPeriod = false;
        for (Grade g : Grade.values()) {
            char c = getCourse(g, day, period);
            Course course = Course.forCode(c);
            if ((course != null) && course.equals(FRENCH)) {
                alreadyScheduledInThisPeriod = true;
                break;
            }
        }
        if (!alreadyScheduledInThisPeriod) {
            FRENCH.incrementFrenchPeriodsScheduled(day);
        }
        FRENCH.incrementFrenchPeriodsScheduled(grade, day);
    }
    
    private void decrementFrenchPeriodsScheduled(Grade grade, Day day, Period period) throws SanityCheckException {
        boolean stillScheduledInThisPeriod = false;
        for (Grade g : Grade.values()) {
            char c = getCourse(g, day, period);
            Course course = Course.forCode(c);
            if ((course != null) && course.equals(FRENCH)) {
                stillScheduledInThisPeriod = true;
                break;
            }
        }
        if (!stillScheduledInThisPeriod) {
            FRENCH.decrementFrenchPeriodsScheduled(day);
        }
        FRENCH.decrementFrenchPeriodsScheduled(grade, day);
    }

}