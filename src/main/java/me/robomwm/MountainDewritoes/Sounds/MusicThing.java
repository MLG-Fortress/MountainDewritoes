package me.robomwm.MountainDewritoes.Sounds;

/**
 * Created by RoboMWM on 11/26/2016.
 */
class MusicThing
{
    private String soundName;
    private long length; //Stored in ticks
    public MusicThing(String name, int seconds)
    {
        this.soundName = name;
        this.length = seconds * 20L; //autoconvert seconds to length
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
