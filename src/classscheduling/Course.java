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
public enum Course {
    
    MATH("Math", 'M', 4, 1),
    ENGLISH("English", 'E', 4, 1),
    FRENCH("French", 'F', 5, 0),
    GEOGRAPHY("Geography", 'G', 2, 0),
    ART("Art", 'A', 2, 2),
    MUSIC("Music", 'U', 3, 1);
    

    final String name;
    final char code;
    final int periods;
    final int daysOff;
    
    private Course(String name, char code, int periods, int daysOff) {
        this.name = name;
        this.code = code;
        this.periods = periods;
        this.daysOff = daysOff;
    }
    
    public static Course forCode(char c) {
        switch(c) {
            case 'M' :
                return MATH;
            case 'E' :
                return ENGLISH;
            case 'F' :
                return FRENCH;
            case 'G' :
                return GEOGRAPHY;
            case 'A' :
                return ART;
            case 'U' :
                return MUSIC;
            default :
                return null;
        }
    }
    
}
