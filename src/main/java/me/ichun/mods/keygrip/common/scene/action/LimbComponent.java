package me.ichun.mods.keygrip.common.scene.action;

import com.google.gson.annotations.SerializedName;
import me.ichun.mods.keygrip.common.scene.Scene;

public class LimbComponent
{
    public LimbComponent(double... changes)
    {
        actionChange = new int[changes.length];
        for(int i = 0; i < changes.length; i++)
        {
            actionChange[i] = (int)Math.round(changes[i] * (double)Scene.PRECISION);
        }
    }

    @SerializedName("a")
    public int[] actionChange; // multiplied by precision and rounded off for storage.
}
