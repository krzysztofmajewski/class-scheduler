/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author krzys
 */
class BoardState {

    final int depth;
    char board[];
    int hits;

    public BoardState(List<Slot> freeSlotList, int depth) {
        board = new char[60];
        this.depth = depth;
        int index = 0;
        for (Grade grade : Grade.values()) {
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    Course course = day.getGradeDay(grade).getCourse(period);
                    if (course != null) {
                        board[index] = course.code;
                    }
                    index++;
                }
            }
        }
    }

    @Override
    public boolean equals(Object aThat) {
        if (this == aThat) {
            return true;
        }
        if (!(aThat instanceof BoardState)) {
            return false;
        }
        BoardState that = (BoardState) aThat;
        return (that.depth == this.depth)
                && Arrays.equals(that.board, this.board);
    }

    @Override
    public int hashCode() {
        int hash = 7;
//        hash = 97 * hash + this.depth;
        hash = 97 * hash + Arrays.hashCode(this.board);
        return hash;
    }

    boolean isSubPatternOf(BoardState bs) {
        for (int i = 0; i < 60; i++) {
            char c = board[i];
            if (c != 0) {
                if (bs.board[i] != c) {
                    return false;
                }
            }
        }
        return true;
    }
}
