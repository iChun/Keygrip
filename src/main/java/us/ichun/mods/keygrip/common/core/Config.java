package us.ichun.mods.keygrip.common.core;

import org.lwjgl.input.Keyboard;
import us.ichun.mods.ichunutil.client.keybind.KeyBind;
import us.ichun.mods.ichunutil.common.core.config.ConfigBase;
import us.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import us.ichun.mods.ichunutil.common.core.config.annotations.IntBool;

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

    public Config(File file, String... unhide)
    {
        super(file, unhide);
    }

    @Override
    public String getModId()
    {
        return "keygrip";
    }

    @Override
    public String getModName()
    {
        return "Keygrip";
    }
}
