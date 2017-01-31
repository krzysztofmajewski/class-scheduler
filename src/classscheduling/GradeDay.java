/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

/**
 *
 * @author krzys
 */
public class GradeDay {
    public static final int PERIODS_PER_DAY = 4;
    
    final private char[] periods;
    
    String name;
    
    public GradeDay(String name) {
        this.name = name;
        periods = new char[PERIODS_PER_DAY];
    }
    
    public void set(int period, char course) throws Exception {
        if ((period < 1) || (period > PERIODS_PER_DAY)) {
            String msg = String.format("Period must be between %d and %d", 1, PERIODS_PER_DAY);
            throw new Exception(msg);
        }
        periods[period - 1] = course;
    }
    
    public char get(int period) throws Exception {
         if ((period < 1) || (period > PERIODS_PER_DAY)) {
            String msg = String.format("Period must be between %d and %d", 1, PERIODS_PER_DAY);
            throw new Exception(msg);
        }
        return periods[period - 1];
    }

    public void clear(int period) throws Exception {
        if (get(period) == 0) {
            String msg = String.format("Period " + period + " already cleared");
            throw new Exception(msg);
        }
        periods[period - 1] = 0;
    }
    
    public int count(char course) {
        int result = 0;
        for (char c : periods) {
            if (c == course) {
                result++;
            }
        }
        return result;
    }
    
    public boolean hasCourse(char course, int period) {
        return (periods[period - 1] == course);
    }


}
