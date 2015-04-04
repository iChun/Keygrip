package us.ichun.mods.keygrip.client.core;

import net.minecraftforge.fml.common.FMLCommonHandler;
import us.ichun.mods.keygrip.common.core.CommonProxy;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        super.preInit();

        tickHandlerClient = new TickHandlerClient();
        FMLCommonHandler.instance().bus().register(tickHandlerClient);
    }
}
