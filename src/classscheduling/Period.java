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
public enum Period {
    
    FIRST,
    SECOND,
    THIRD,
    FOURTH;
    
    @Override
    public String toString() {
        switch (this) {
            case FIRST : 
                return "First period";
            case SECOND :
                return "Second period";
            case THIRD :
                return "Third period";
            default :
                return "Fourth period";
        }
    }
    
}
