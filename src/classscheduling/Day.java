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
    final GradeDay grade7;
    final GradeDay grade8;
    final GradeDay grade9;
    
    final String name;
    
    public Day(String name) {
        this.name = name;
        
        grade7 = new GradeDay("Grade 7");
        grade8 = new GradeDay("Grade 8");
        grade9 = new GradeDay("Grade 9");
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
