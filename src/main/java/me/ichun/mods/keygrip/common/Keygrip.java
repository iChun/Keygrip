package me.ichun.mods.keygrip.common;

import me.ichun.mods.ichunutil.common.core.Logger;
import me.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import me.ichun.mods.keygrip.client.core.EventHandlerClient;
import me.ichun.mods.keygrip.client.core.ResourceHelper;
import me.ichun.mods.keygrip.common.core.Config;
import me.ichun.mods.keygrip.common.core.EventHandlerServer;
import me.ichun.mods.keygrip.common.core.ProxyCommon;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod(modid = Keygrip.MOD_ID, name = Keygrip.MOD_NAME,
        version = Keygrip.VERSION,
        guiFactory = "me.ichun.mods.ichunutil.common.core.config.GenericModGuiFactory",
        dependencies = "required-after:ichunutil@[" + iChunUtil.VERSION_MAJOR + ".0.0," + (iChunUtil.VERSION_MAJOR + 1) + ".0.0)",
        acceptableRemoteVersions = "[" + iChunUtil.VERSION_MAJOR + ".0.0," + iChunUtil.VERSION_MAJOR + ".1.0)"
)
public class Keygrip
{
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".0.0";
    public static final String MOD_NAME = "Keygrip";
    public static final String MOD_ID = "keygrip";

    public static final Logger LOGGER = Logger.createLogger(MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static Keygrip instance;

    @SidedProxy(clientSide = "me.ichun.mods.keygrip.client.core.ProxyClient", serverSide = "me.ichun.mods.keygrip.common.core.ProxyCommon")
    public static ProxyCommon proxy;

    public static Config config;

    public static PacketChannel channel;

    public static EventHandlerServer eventHandlerServer;
    public static EventHandlerClient eventHandlerClient;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit();

        if(FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            config = ConfigHandler.registerConfig(new Config(new File(ResourceHelper.getConfigDir(), "config.cfg")));
        }

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, false));
    }
}
