/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.Objects;

/**
 *
 * @author krzys
 */
class Slot {

    final Grade grade;
    final Day day;
    final Period period;
    
    public Slot(Grade grade, Day day, Period period) {
        this.grade = grade;
        this.day = day;
        this.period = period;
    }

    @Override
    public String toString() {
        return day.name + ", " + grade + ", " + period;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Slot) {
            Slot other = (Slot) o;
            return other.day.equals(day) && other.grade.equals(grade) && other.period.equals(period);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.day);
        hash = 53 * hash + Objects.hashCode(this.grade);
        hash = 53 * hash + Objects.hashCode(this.period);
        return hash;
    }

}
