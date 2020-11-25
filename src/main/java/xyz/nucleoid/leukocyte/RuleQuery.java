package xyz.nucleoid.leukocyte;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class RuleQuery {
    private final RegistryKey<World> dimension;
    private final BlockPos pos;
    private final PlayerEntity source;

    RuleQuery(RegistryKey<World> dimension, BlockPos pos, PlayerEntity source) {
        this.dimension = dimension;
        this.pos = pos;
        this.source = source;
    }

    public static RuleQuery at(World world, BlockPos pos) {
        return new RuleQuery(world.getRegistryKey(), pos, null);
    }

    public static RuleQuery at(RegistryKey<World> dimension, BlockPos pos) {
        return new RuleQuery(dimension, pos, null);
    }

    public static RuleQuery in(World world) {
        return new RuleQuery(world.getRegistryKey(), null, null);
    }

    public static RuleQuery in(RegistryKey<World> dimension) {
        return new RuleQuery(dimension, null, null);
    }

    public static RuleQuery forPlayer(PlayerEntity player) {
        return new RuleQuery(player.world.getRegistryKey(), player.getBlockPos(), player);
    }

    public static RuleQuery forPlayerAt(PlayerEntity player, BlockPos pos) {
        return new RuleQuery(player.world.getRegistryKey(), pos, player);
    }

    @Nullable
    public RegistryKey<World> getDimension() {
        return this.dimension;
    }

    @Nullable
    public BlockPos getPos() {
        return this.pos;
    }

    @Nullable
    PlayerEntity getSource() {
        return this.source;
    }
}
