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
class Slot {
    
    Day day;
    GradeDay gradeDay;
    Period period;
    
    @Override
    public String toString() {
        return "Slot: " + day.name + ", " + gradeDay.grade.name() + ", " + period + ", " + gradeDay.get(period);
    }
    
}
