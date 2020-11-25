package xyz.nucleoid.leukocyte.region;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.leukocyte.rule.ProtectionRule;
import xyz.nucleoid.leukocyte.rule.RuleResult;

public interface RuleReader {
    Iterable<ProtectionRegion> sample(RegistryKey<World> dimension);

    Iterable<ProtectionRegion> sample(RegistryKey<World> dimension, BlockPos pos);

    default Iterable<ProtectionRegion> sample(World world) {
        return this.sample(world.getRegistryKey());
    }

    default Iterable<ProtectionRegion> sample(World world, BlockPos pos) {
        return this.sample(world.getRegistryKey(), pos);
    }

    // TODO: clean up handling of source
    RuleResult test(RegistryKey<World> dimension, ProtectionRule rule, @Nullable ServerPlayerEntity source);

    RuleResult test(RegistryKey<World> dimension, BlockPos pos, ProtectionRule rule, @Nullable ServerPlayerEntity source);

    default RuleResult test(World world, ProtectionRule rule, @Nullable ServerPlayerEntity source) {
        return this.test(world.getRegistryKey(), rule, source);
    }

    default RuleResult test(World world, BlockPos pos, ProtectionRule rule, @Nullable ServerPlayerEntity source) {
        return this.test(world.getRegistryKey(), pos, rule, source);
    }
}
