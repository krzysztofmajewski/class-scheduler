/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

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

    void setCourse(Grade grade, Day day, Period period, Course course) {
        int index = computeIndex(grade, day, period);
        board[index] = course.code;
        depth++;
        course.incrementPeriodsScheduled(this, grade, day, period);
    }

    void setCourse(Move move) {
        setCourse(move.slot.grade, move.slot.day, move.slot.period, move.course);
    }

    void clear(Grade grade, Day day, Period period) {
        char c = getCourse(grade, day, period);
        int index = computeIndex(grade, day, period);
        board[index] = 0;
        depth--;
        Course course = Course.forCode(c);
        course.decrementPeriodsScheduled(this, grade, day, period);
    }

    void clearCourse(Slot slot) {
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

}
