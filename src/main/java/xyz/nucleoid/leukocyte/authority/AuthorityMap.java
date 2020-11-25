package xyz.nucleoid.leukocyte.authority;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;

public final class AuthorityMap implements Iterable<Authority> {
    private final SortedSet<Authority> authorities = new ObjectRBTreeSet<>();
    private final Object2ObjectMap<String, Authority> byKey = new Object2ObjectOpenHashMap<>();

    public void clear() {
        this.authorities.clear();
        this.byKey.clear();
    }

    public boolean add(Authority authority) {
        if (this.byKey.put(authority.key, authority) == null) {
            this.authorities.add(authority);
            return true;
        }
        return false;
    }

    public boolean replace(Authority from, Authority to) {
        if (from.key.equals(to.key) && this.byKey.replace(from.key, from, to)) {
            this.authorities.remove(from);
            this.authorities.add(to);
            return true;
        }
        return false;
    }

    @Nullable
    public Authority remove(String key) {
        Authority authority = this.byKey.remove(key);
        if (authority != null) {
            this.authorities.remove(authority);
            return authority;
        }
        return null;
    }

    @Nullable
    public Authority byKey(String key) {
        return this.byKey.get(key);
    }

    public boolean contains(String key) {
        return this.byKey.containsKey(key);
    }

    public Set<String> keySet() {
        return this.byKey.keySet();
    }

    public Iterable<Object2ObjectMap.Entry<String, Authority>> entries() {
        return Object2ObjectMaps.fastIterable(this.byKey);
    }

    public boolean isEmpty() {
        return this.authorities.isEmpty();
    }

    @Override
    public Iterator<Authority> iterator() {
        return this.authorities.iterator();
    }

    public Stream<Authority> stream() {
        return this.authorities.stream();
    }
}
