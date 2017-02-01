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
public class ValidationException extends Exception {
    
    private String s;

    public ValidationException(String s) {
        this.s = s;
    }
    
}
