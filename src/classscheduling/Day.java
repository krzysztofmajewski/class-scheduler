/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Grade.EIGHT;
import static classscheduling.Grade.NINE;
import static classscheduling.Grade.SEVEN;

/**
 *
 * @author krzys
 */
class Day {    
    final GradeDay grade7;
    final GradeDay grade8;
    final GradeDay grade9;
    
    final String name;
    
    public Day(String name) {
        this.name = name;
        
        grade7 = new GradeDay(SEVEN);
        grade8 = new GradeDay(EIGHT);
        grade9 = new GradeDay(NINE);
    }
    
    public GradeDay getGradeDay(Grade g) {
        switch (g) {
            case SEVEN:
                return grade7;
            case EIGHT:
                return grade8;
            default: 
                return grade9;
        }
    }
    
    public int count(char course) {
        int result = 0;
        for (Period p : Period.values()) {
            if (grade7.hasCourse(course, p)
                    || grade8.hasCourse(course, p)
                    || grade9.hasCourse(course, p)) {
                result++;
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        return name;
    }

}
