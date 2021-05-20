package xyz.nucleoid.leukocyte.authority;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface AuthorityMap extends Iterable<Authority> {
    void clear();

    boolean add(Authority authority);

    boolean replace(Authority from, Authority to);

    @Nullable
    Authority remove(String key);

    @Nullable
    Authority byKey(String key);

    boolean contains(String key);

    Set<String> keySet();

    int size();

    default boolean isEmpty() {
        return this.size() == 0;
    }

    Iterable<Object2ObjectMap.Entry<String, Authority>> entries();

    default Stream<Authority> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}
