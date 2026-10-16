package io.github.kosmx.bendylib;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BendyLib.MODID)
public class BendyLib {

    public static final String MODID = "bendylib";
    public static final Logger LOGGER = LoggerFactory.getLogger("bendy-lib");

    public BendyLib(IEventBus modEventBus, ModContainer modContainer) {
    }
}
