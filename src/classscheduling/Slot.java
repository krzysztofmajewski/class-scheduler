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
        return day.name + ", " + gradeDay.grade.name() + ", " + period + ", " + gradeDay.get(period);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Slot) {
            Slot other = (Slot) o;
            return (other.day.equals(day) && other.gradeDay.equals(gradeDay)) && other.period.equals(period);
        }
        return false;
    }

//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 53 * hash + Objects.hashCode(this.day);
//        hash = 53 * hash + Objects.hashCode(this.gradeDay);
//        hash = 53 * hash + Objects.hashCode(this.period);
//        return hash;
//    }
    
    Course getCourse() {
        char c = gradeDay.get(period);
        if (c == 0) {
            return null;
        }
        return Course.forCode(c);
    }
    
    Grade getGrade() {
        return gradeDay.grade;
    }
    
}
