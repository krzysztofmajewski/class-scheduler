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
    public static final int PERIODS_PER_DAY = Period.values().length;
    
    Grade grade;
    Day day;

    final private char[] periods;
        
    public GradeDay(Grade grade, Day day) {
        this.grade = grade;
        this.day = day;
        periods = new char[PERIODS_PER_DAY];
    }
    
    public void set(Period period, char course) {
        periods[period.ordinal()] = course;
    }
    
    public char get(Period period) {
        return periods[period.ordinal()];
    }
    
    public Course getCourse(Period period) {
        char c = get(period);
        return Course.forCode(c);
    }

    public void clear(Period period) throws SanityCheckException {
        if (get(period) == 0) {
            String msg = String.format(period + " already cleared");
            throw new SanityCheckException(msg);
        }
        periods[period.ordinal()] = 0;
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
    
    public boolean hasCourse(char course, Period period) {
        return (periods[period.ordinal()] == course);
    }


}
