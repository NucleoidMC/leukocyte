package xyz.nucleoid.leukocyte;

import com.google.common.collect.AbstractIterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.authority.Authority;
import xyz.nucleoid.stimuli.EventSource;
import xyz.nucleoid.stimuli.event.StimulusEvent;
import xyz.nucleoid.stimuli.selector.EventListenerSelector;

import java.util.Collection;
import java.util.Iterator;

final class LeukocyteEventListenerSelector implements EventListenerSelector {
    @Override
    public <T> Iterator<T> selectListeners(MinecraftServer server, StimulusEvent<T> event, EventSource source) {
        Leukocyte leukocyte = Leukocyte.get(server);

        Iterable<Authority> authorities = this.getBroadAuthoritiesFor(leukocyte, event, source);
        return new ListenerIterator<>(event, source, authorities.iterator());
    }

    private Iterable<Authority> getBroadAuthoritiesFor(Leukocyte leukocyte, StimulusEvent<?> event, EventSource source) {
        RegistryKey<World> dimension = source.getDimension();
        if (dimension != null) {
            return leukocyte.selectAuthorities(dimension, event);
        } else {
            return leukocyte.getAuthorities();
        }
    }

    static final class ListenerIterator<T> extends AbstractIterator<T> {
        private final StimulusEvent<T> event;
        private final EventSource source;

        private final Iterator<Authority> authorityIterator;
        private Iterator<T> listenerIterator;

        ListenerIterator(StimulusEvent<T> event, EventSource source, Iterator<Authority> authorityIterator) {
            this.event = event;
            this.source = source;
            this.authorityIterator = authorityIterator;
        }

        @Override
        protected T computeNext() {
            Iterator<T> listenerIterator = this.listenerIterator;
            while (listenerIterator == null || !listenerIterator.hasNext()) {
                Iterator<Authority> authorityIterator = this.authorityIterator;
                if (!authorityIterator.hasNext()) {
                    return this.endOfData();
                }

                Authority authority = authorityIterator.next();
                if (authority.getEventFilter().accepts(this.source)) {
                    Collection<T> listeners = authority.getEventListeners().get(this.event);
                    if (!listeners.isEmpty()) {
                        this.listenerIterator = listenerIterator = listeners.iterator();
                    }
                }
            }

            return listenerIterator.next();
        }
    }
}
