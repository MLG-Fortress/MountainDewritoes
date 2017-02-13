package me.robomwm.MountainDewritoes.Music;

/**
 * Created by RoboMWM on 11/26/2016.
 */
class MusicThing
{
    private String soundName;
    private String URL;
    private long length; //Stored in ticks
    public MusicThing(String URL, int seconds)
    {
        this.URL = URL;
        this.length = seconds * 20L; //autoconvert seconds to length
    }
    public String getURL()
    {
        return this.URL;
    }
    public String getSoundName()
    {
        return this.soundName;
    }
    public long getLength()
    {
        return this.length;
    }
}
