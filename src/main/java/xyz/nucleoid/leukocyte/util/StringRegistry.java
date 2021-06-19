package xyz.nucleoid.leukocyte.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

public final class StringRegistry<T> implements Codec<T>, Iterable<T> {
    private final BiMap<String, T> map = HashBiMap.create();

    public void register(String identifier, T value) {
        this.map.put(identifier, value);
    }

    @Nullable
    public T get(String identifier) {
        return this.map.get(identifier);
    }

    @Nullable
    public String getIdentifier(T value) {
        return this.map.inverse().get(value);
    }

    public boolean containsKey(String identifier) {
        return this.map.containsKey(identifier);
    }

    @Override
    public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> ops, U input) {
        return Codec.STRING.decode(ops, input)
                .flatMap(pair -> {
                    if (!this.containsKey(pair.getFirst())) {
                        return DataResult.error("Unknown registry key: " + pair.getFirst());
                    }
                    return DataResult.success(pair.mapFirst(this::get));
                });
    }

    @Override
    public <U> DataResult<U> encode(T input, DynamicOps<U> ops, U prefix) {
        var identifier = this.getIdentifier(input);
        if (identifier == null) {
            return DataResult.error("Unknown registry element " + input);
        }
        return ops.mergeToPrimitive(prefix, ops.createString(identifier));
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.map.values().iterator();
    }
}
