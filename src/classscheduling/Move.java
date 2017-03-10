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
public class Move {
    
    Slot slot;
    Course course;
    
    public Move(Slot slot, Course course) {
        this.slot = slot;
        this.course = course;
    }
    
}
