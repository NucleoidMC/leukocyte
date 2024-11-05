package xyz.nucleoid.leukocyte.rule.enforcer;

import xyz.nucleoid.leukocyte.rule.ProtectionRuleMap;
import xyz.nucleoid.leukocyte.rule.RuleResult;
import xyz.nucleoid.stimuli.event.EventRegistrar;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface ProtectionRuleEnforcer {
    void applyTo(ProtectionRuleMap rules, EventRegistrar events);

    default ForRule forRule(EventRegistrar events, RuleResult result) {
        return new ForRule(events, result);
    }

    interface ListenerFactory<T> {
        T createListener(EventResult rule);
    }

    record ForRule(EventRegistrar events, RuleResult result) {
        public <T> void applySimple(StimulusEvent<T> event, ListenerFactory<T> factory) {
            if (this.result.isDefinitive()) {
                this.events.listen(event, factory.createListener(this.result.asEventResult()));
            }
        }
    }
}
