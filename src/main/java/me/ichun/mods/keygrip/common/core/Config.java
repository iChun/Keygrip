package me.ichun.mods.keygrip.common.core;

import me.ichun.mods.ichunutil.client.keybind.KeyBind;
import me.ichun.mods.ichunutil.common.core.config.ConfigBase;
import me.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import me.ichun.mods.ichunutil.common.core.config.annotations.IntBool;
import me.ichun.mods.keygrip.common.Keygrip;
import org.lwjgl.input.Keyboard;

import java.io.File;

public class Config extends ConfigBase
{
    @ConfigProp
    @IntBool
    public int playbackSceneWhileRecording = 1;

    @ConfigProp
    public KeyBind toggleScenePlayback = new KeyBind(Keyboard.KEY_F9);

    @ConfigProp
    public KeyBind startStopRecord = new KeyBind(Keyboard.KEY_F10);

    @ConfigProp
    public KeyBind toggleSceneRecorder = new KeyBind(Keyboard.KEY_F12);

    public Config(File file)
    {
        super(file);
    }

    @Override
    public String getModId()
    {
        return Keygrip.MOD_ID;
    }

    @Override
    public String getModName()
    {
        return "Keygrip";
    }
}
