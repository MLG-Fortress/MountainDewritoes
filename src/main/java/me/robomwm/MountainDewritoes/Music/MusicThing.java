package me.robomwm.MountainDewritoes.Music;

/**
 * Created by RoboMWM on 11/26/2016.
 */
public class MusicThing
{
    private String soundName;
    private String URL;
    private String description;
    private long length; //Stored in ticks
    private long startTime;
    private int priority = 0;

    public MusicThing(String URL, int seconds)
    {
        this(URL, seconds, false);
    }

    public MusicThing(String URLorName, long seconds, boolean isName, String description)
    {
        this(URLorName, seconds, isName);
        this.description = description;
    }

    public MusicThing(String URLorName, long seconds, boolean isName)
    {
        if (isName)
            this.soundName = URLorName;
        else
            this.URL = URLorName;
        this.length = seconds * 20L; //autoconvert seconds to length
        this.startTime = System.currentTimeMillis();
    }

    public MusicThing(MusicThing song)
    {
        this.soundName = song.soundName;
        this.URL = song.URL;
        this.length = song.length;

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

    /**
     * @return length of song in ticks
     */
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

    public String getDescription()
    {
        return description;
    }

    public boolean equals(MusicThing song)
    {
        return song.getStartTime() == this.startTime;
    }
    public boolean hasHigherPriority(MusicThing song)
    {
        return this.priority > song.getPriority();
    }

    public boolean hasEqualOrHigherPriority(MusicThing song)
    {
        return this.priority > song.getPriority();
    }
}
