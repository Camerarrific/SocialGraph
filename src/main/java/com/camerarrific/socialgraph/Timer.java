    
package com.camerarrific.socialgraph;

/**
 *
 * @author adam
 */


public class Timer {
    
   static long APITimer;
    
    
    Timer(){
        this.APITimer = System.currentTimeMillis();
    }
    
    

public long timerEnd() {
    long ms = System.currentTimeMillis()-Timer.APITimer;
    Timer.APITimer  = System.currentTimeMillis();
    return ms;
}

private static String unixtime() {
    return Long.toString(System.currentTimeMillis() / 1000L);
}
    
}
