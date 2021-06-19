package xyz.nucleoid.leukocyte.authority;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;

public final class AuthoritySortedHashMap implements AuthorityMap {
    private final SortedSet<Authority> authorities = new ObjectRBTreeSet<>();
    private final Object2ObjectMap<String, Authority> byKey = new Object2ObjectOpenHashMap<>();

    @Override
    public void clear() {
        this.authorities.clear();
        this.byKey.clear();
    }

    @Override
    public boolean add(Authority authority) {
        if (this.byKey.put(authority.getKey(), authority) == null) {
            this.authorities.add(authority);
            return true;
        }
        return false;
    }

    @Override
    public boolean replace(Authority from, Authority to) {
        if (from.getKey().equals(to.getKey()) && this.byKey.replace(from.getKey(), from, to)) {
            this.authorities.remove(from);
            this.authorities.add(to);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public Authority remove(String key) {
        var authority = this.byKey.remove(key);
        if (authority != null) {
            this.authorities.remove(authority);
            return authority;
        }
        return null;
    }

    @Override
    @Nullable
    public Authority byKey(String key) {
        return this.byKey.get(key);
    }

    @Override
    public boolean contains(String key) {
        return this.byKey.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return this.byKey.keySet();
    }

    @Override
    public Iterable<Object2ObjectMap.Entry<String, Authority>> entries() {
        return Object2ObjectMaps.fastIterable(this.byKey);
    }

    @Override
    public int size() {
        return this.authorities.size();
    }

    @Override
    public boolean isEmpty() {
        return this.authorities.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<Authority> iterator() {
        return this.authorities.iterator();
    }

    @Override
    public Stream<Authority> stream() {
        return this.authorities.stream();
    }
}
