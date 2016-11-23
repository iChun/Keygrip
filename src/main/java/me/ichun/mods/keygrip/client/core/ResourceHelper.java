package me.ichun.mods.keygrip.client.core;

import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;

public class ResourceHelper
{
    private static File workRoot;
    private static File scenesDir;
    private static File actionsDir;
    private static File configDir;
    private static File tempDir;

    public static void init()
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            tempDir = new File("/mods/keygrip/temp");
            tempDir.mkdirs();
            return;
        }

        workRoot = new File(me.ichun.mods.ichunutil.common.core.util.ResourceHelper.getModsFolder(), "keygrip");
        scenesDir = new File(workRoot, "scenes");
        actionsDir = new File(workRoot, "actions");
        configDir = new File(workRoot, "config");
        tempDir = new File(workRoot, "temp");

        scenesDir.mkdirs();
        actionsDir.mkdirs();
        configDir.mkdirs();
        tempDir.mkdirs();
    }

    public static File getWorkRoot()
    {
        return workRoot;
    }

    public static File getScenesDir()
    {
        return scenesDir;
    }

    public static File getActionsDir()
    {
        return actionsDir;
    }

    public static File getConfigDir()
    {
        return configDir;
    }

    public static File getTempDir()
    {
        return tempDir;
    }
}
