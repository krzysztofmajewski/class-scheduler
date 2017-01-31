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
public enum Grade {
    
    SEVEN("Grade 7"),
    EIGHT("Grade 8"),
    NINE("Grade 9");
    
    private final String name;
    
    private Grade(String name) {
        this.name = name;
    }
}
