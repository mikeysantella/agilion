package com.agilion.utils;

/**
 * Created by Alex_Lappy_486 on 3/6/18.
 */
public class SleepyTime
{
    public static void sleepForSeconds(int s)
    {
        try
        {
            Thread.sleep(1000 * s);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
