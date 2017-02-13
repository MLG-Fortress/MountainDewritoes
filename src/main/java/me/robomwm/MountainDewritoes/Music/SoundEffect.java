package me.robomwm.MountainDewritoes.Music;

/**
 * Created by RoboMWM on 2/12/2017.
 */
class SoundEffect
{
    private String effectName;
    public SoundEffect(String name, int seconds)
    {
        this.effectName = name;
    }
    public String getEffectName()
    {
        return this.effectName;
    }
}
