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
public class SanityCheckException extends Exception {
    
    String s;

    public SanityCheckException(String s) {
        this.s = s;
    }
    
    @Override
    public String getMessage() {
        return s;
    }
    
}
