/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;

/**
 *
 * @author krzys
 */
public class ValidationErrors {
    
    // TODO: performance test the exception throwing
    boolean strict = true;
    
    private final ArrayList<String> errors;
    
    public ValidationErrors() {
        errors = new ArrayList<>();
    }
    
    public void add(String s) throws ValidationException {
        errors.add(s);
        if (strict) {
            throw new ValidationException(s);
        }
    }
    
    public boolean isEmpty() {
        return errors.isEmpty();
    }
    
    public boolean hasErrors() {
        return !isEmpty();
    }

    void print() {
        errors.forEach((s) -> {
            System.out.println(s);
        });
    }

    void clear() {
        errors.clear();
    }
    
}
