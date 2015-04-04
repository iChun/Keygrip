package us.ichun.mods.keygrip.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import us.ichun.mods.ichunutil.common.core.config.ConfigHandler;
import us.ichun.mods.ichunutil.common.core.network.PacketChannel;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionChecker;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionInfo;
import us.ichun.mods.ichunutil.common.iChunUtil;
import us.ichun.mods.keygrip.client.core.ResourceHelper;
import us.ichun.mods.keygrip.common.core.CommonProxy;
import us.ichun.mods.keygrip.common.core.Config;
import us.ichun.mods.keygrip.common.core.EventHandler;

import java.io.File;

@Mod(modid = "Keygrip", name = "Keygrip",
        version = Keygrip.version,
        dependencies = "required-after:iChunUtil@["+ iChunUtil.versionMC + ".2.0,)"
)
public class Keygrip
{
    public static final String version = iChunUtil.versionMC + ".0.0";

    private static final Logger logger = LogManager.getLogger("Keygrip");

    @Mod.Instance("Keygrip")
    public static Keygrip instance;

    @SidedProxy(clientSide = "us.ichun.mods.keygrip.client.core.ClientProxy", serverSide = "us.ichun.mods.keygrip.common.core.CommonProxy")
    public static CommonProxy proxy;

    public static Config config;

    public static PacketChannel channel;

    @Mod.EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        proxy.preInit();

        EventHandler handler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(handler);

        if(FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            config = (Config)ConfigHandler.registerConfig(new Config(new File(ResourceHelper.getConfigDir(), "config.cfg")));
        }

        ModVersionChecker.register_iChunMod(new ModVersionInfo("Keygrip", iChunUtil.versionOfMC, version, false));
    }

    public static void console(String s, boolean warning)
    {
        StringBuilder sb = new StringBuilder();
        logger.log(warning ? Level.WARN : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }
}
