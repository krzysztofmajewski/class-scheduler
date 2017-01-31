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
class Day {    
    final Grade grade7;
    final Grade grade8;
    final Grade grade9;
    
    final String name;
    
    public Day(String name) {
        this.name = name;
        
        grade7 = new Grade();
        grade8 = new Grade();
        grade9 = new Grade();
    }
    
    public int count(char course) {
        int result = 0;
        for (int period = 1; period <= Grade.PERIODS_PER_DAY; period++) {
            if (grade7.hasCourse(course, period)
                    || grade8.hasCourse(course, period)
                    || grade9.hasCourse(course, period)) {
                result++;
            }
        }
        return result;
    }

}
