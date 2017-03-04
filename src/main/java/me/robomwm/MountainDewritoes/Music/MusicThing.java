package me.robomwm.MountainDewritoes.Music;

/**
 * Created by RoboMWM on 11/26/2016.
 */
class MusicThing
{
    private String soundName;
    private String URL;
    private long length; //Stored in ticks
    private long startTime;
    private int priority = 0;

    public MusicThing(String URL, int seconds)
    {
        this.URL = URL;
        this.length = seconds * 20L; //autoconvert seconds to length
        this.startTime = System.currentTimeMillis();
    }

    public MusicThing setPriority(int priority)
    {
        this.priority = priority;
        return this;
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
    public long getStartTime()
    {
        return startTime;
    }
    public int getPriority()
    {
        return priority;
    }

    public boolean equals(MusicThing song)
    {
        return song.getStartTime() == this.startTime;
    }
    public boolean hasHigherPriority(MusicThing song)
    {
        return song.getPriority() > this.priority;
    }
}
