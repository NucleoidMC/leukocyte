package xyz.nucleoid.leukocyte.region;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public final class RegionMap implements Iterable<ProtectionRegion> {
    private final SortedSet<ProtectionRegion> regions = new ObjectRBTreeSet<>();
    private final Map<String, ProtectionRegion> byKey = new Object2ObjectOpenHashMap<>();

    public void clear() {
        this.regions.clear();
        this.byKey.clear();
    }

    public boolean add(ProtectionRegion region) {
        if (this.byKey.put(region.key, region) == null) {
            this.regions.add(region);
            return true;
        }
        return false;
    }

    @Nullable
    public ProtectionRegion remove(String key) {
        ProtectionRegion region = this.byKey.remove(key);
        if (region != null) {
            this.regions.remove(region);
            return region;
        }
        return null;
    }

    @Nullable
    public ProtectionRegion byKey(String key) {
        return this.byKey.get(key);
    }

    @Override
    public Iterator<ProtectionRegion> iterator() {
        return this.regions.iterator();
    }

    public Set<String> getKeys() {
        return this.byKey.keySet();
    }

    public boolean replace(ProtectionRegion from, ProtectionRegion to) {
        if (this.byKey.replace(from.key, from, to)) {
            this.regions.remove(from);
            this.regions.add(to);
            return true;
        }
        return false;
    }
}
