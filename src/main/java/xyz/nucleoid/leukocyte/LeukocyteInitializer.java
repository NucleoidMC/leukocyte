package xyz.nucleoid.leukocyte;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import xyz.nucleoid.leukocyte.command.ProtectCommand;
import xyz.nucleoid.leukocyte.command.ShapeCommand;
import xyz.nucleoid.leukocyte.rule.enforcer.LeukocyteRuleEnforcer;
import xyz.nucleoid.leukocyte.shape.*;
import xyz.nucleoid.stimuli.Stimuli;

public final class LeukocyteInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        ProtectionShape.register("universal", UniversalShape.CODEC);
        ProtectionShape.register("dimension", DimensionShape.CODEC);
        ProtectionShape.register("box", BoxShape.CODEC);
        ProtectionShape.register("union", UnionShape.CODEC);

        Leukocyte.registerRuleEnforcer(LeukocyteRuleEnforcer.INSTANCE);

        Stimuli.registerSelector(new LeukocyteEventListenerSelector());

        ServerWorldEvents.LOAD.register((server, world) -> Leukocyte.get(server).onWorldLoad(world));
        ServerWorldEvents.UNLOAD.register((server, world) -> Leukocyte.get(server).onWorldUnload(world));

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            ProtectCommand.register(dispatcher);
            ShapeCommand.register(dispatcher);
        });
    }
}
