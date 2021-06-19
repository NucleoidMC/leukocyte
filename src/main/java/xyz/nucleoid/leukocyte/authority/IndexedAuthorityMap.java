package xyz.nucleoid.leukocyte.authority;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.stimuli.EventSource;
import xyz.nucleoid.stimuli.event.StimulusEvent;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class IndexedAuthorityMap implements AuthorityMap {
    private final AuthorityMap main = new AuthoritySortedHashMap();
    private final Reference2ObjectMap<RegistryKey<World>, DimensionMap> byDimension = new Reference2ObjectOpenHashMap<>();

    public void addDimension(RegistryKey<World> dimension) {
        var source = EventSource.allOf(dimension);

        var dimensionMap = new DimensionMap(dimension);
        for (var authority : this.main) {
            if (authority.getEventFilter().accepts(source)) {
                dimensionMap.add(authority);
            }
        }

        this.byDimension.put(dimension, dimensionMap);
    }

    public void removeDimension(RegistryKey<World> dimension) {
        this.byDimension.remove(dimension);
    }

    public Iterable<Authority> select(RegistryKey<World> dimension, StimulusEvent<?> event) {
        var dimensionMap = this.byDimension.get(dimension);
        if (dimensionMap != null) {
            var map = dimensionMap.byEvent.get(event);
            if (map != null) {
                return map;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void clear() {
        this.main.clear();
        this.byDimension.clear();
    }

    @Override
    public boolean add(Authority authority) {
        if (this.main.add(authority)) {
            this.addToDimension(authority);
            return true;
        }

        return false;
    }

    @Override
    public boolean replace(Authority from, Authority to) {
        if (this.main.replace(from, to)) {
            this.replaceInDimension(from, to);
            return true;
        }

        return false;
    }

    @Override
    @Nullable
    public Authority remove(String key) {
        var authority = this.main.remove(key);
        if (authority != null) {
            this.removeFromDimension(key);
            return authority;
        }
        return null;
    }

    @Override
    @Nullable
    public Authority byKey(String key) {
        return this.main.byKey(key);
    }

    @Override
    public boolean contains(String key) {
        return this.main.contains(key);
    }

    @Override
    public Set<String> keySet() {
        return this.main.keySet();
    }

    @Override
    public int size() {
        return this.main.size();
    }

    @Override
    public Iterable<Object2ObjectMap.Entry<String, Authority>> entries() {
        return this.main.entries();
    }

    @NotNull
    @Override
    public Iterator<Authority> iterator() {
        return this.main.iterator();
    }

    @Override
    public Stream<Authority> stream() {
        return this.main.stream();
    }

    private void addToDimension(Authority authority) {
        var filter = authority.getEventFilter();
        for (var entry : Reference2ObjectMaps.fastIterable(this.byDimension)) {
            var dimension = entry.getKey();
            if (filter.accepts(EventSource.allOf(dimension))) {
                var dimensionMap = entry.getValue();
                dimensionMap.add(authority);
            }
        }
    }

    private void replaceInDimension(Authority from, Authority to) {
        var fromFilter = from.getEventFilter();
        var toFilter = to.getEventFilter();

        for (var dimensionMap : this.byDimension.values()) {
            boolean fromIncluded = fromFilter.accepts(dimensionMap.eventSource);
            boolean toIncluded = toFilter.accepts(dimensionMap.eventSource);
            if (fromIncluded && toIncluded) {
                dimensionMap.replace(from, to);
            } else if (fromIncluded) {
                dimensionMap.remove(from.getKey());
            } else if (toIncluded) {
                dimensionMap.add(to);
            }
        }
    }

    private void removeFromDimension(String key) {
        for (var authorities : this.byDimension.values()) {
            authorities.remove(key);
        }
    }

    static final class DimensionMap {
        final EventSource eventSource;
        final Map<StimulusEvent<?>, AuthorityMap> byEvent = new Reference2ObjectOpenHashMap<>();

        DimensionMap(RegistryKey<World> dimension) {
            this.eventSource = EventSource.allOf(dimension);
        }

        void add(Authority authority) {
            var listeners = authority.getEventListeners();
            for (var event : listeners.getEvents()) {
                this.getMapForEvent(event).add(authority);
            }
        }

        void replace(Authority from, Authority to) {
            var fromEvents = from.getEventListeners().getEvents();
            var toEvents = to.getEventListeners().getEvents();

            for (var event : fromEvents) {
                var map = this.getMapForEvent(event);
                if (toEvents.contains(event)) {
                    map.replace(from, to);
                } else {
                    map.remove(from.getKey());
                }
            }

            for (var event : toEvents) {
                if (!fromEvents.contains(event)) {
                    this.getMapForEvent(event).add(to);
                }
            }
        }

        void remove(String key) {
            for (var map : this.byEvent.values()) {
                map.remove(key);
            }
        }

        private AuthorityMap getMapForEvent(StimulusEvent<?> event) {
            return this.byEvent.computeIfAbsent(event, e -> new AuthoritySortedHashMap());
        }
    }
}
