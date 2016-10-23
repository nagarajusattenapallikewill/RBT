package com.onmobile.apps.ringbacktones.sms.ruleengine;

import org.easyrules.api.RuleListener;
import org.easyrules.api.RulesEngine;
import org.easyrules.core.RulesEngineBuilder;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;

import static org.easyrules.core.RulesEngineBuilder.aNewRulesEngine;
import static org.easyrules.util.Utils.DEFAULT_ENGINE_NAME;
import static org.easyrules.util.Utils.DEFAULT_RULE_PRIORITY_THRESHOLD;


public class RulesEngineFactoryBean implements FactoryBean<RulesEngine> {

    private String name = DEFAULT_ENGINE_NAME;

    private int priorityThreshold = DEFAULT_RULE_PRIORITY_THRESHOLD;
    
    private boolean skipOnFirstAppliedRule;
    
    private boolean skipOnFirstFailedRule;
    
    private boolean silentMode;
    
    private List<Object> rules;

    private List<RuleListener> ruleListeners;

    @Override
    public RulesEngine getObject() {
        RulesEngineBuilder rulesEngineBuilder = aNewRulesEngine()
                .named(name)
                .withSkipOnFirstAppliedRule(skipOnFirstAppliedRule)
                .withSkipOnFirstFailedRule(skipOnFirstFailedRule)
                .withRulePriorityThreshold(priorityThreshold)
                .withSilentMode(silentMode);
        registerRuleListeners(rulesEngineBuilder);
        RulesEngine rulesEngine = rulesEngineBuilder.build();
        registerRules(rulesEngine);
        return rulesEngine;
    }

    @Override
    public Class<RulesEngine> getObjectType() {
        return RulesEngine.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    private void registerRules(RulesEngine rulesEngine) {
        if (rules != null && !rules.isEmpty()) {
            for (Object rule : rules) {
                rulesEngine.registerRule(rule);
            }
        }
    }

    private void registerRuleListeners(RulesEngineBuilder rulesEngineBuilder) {
        if (ruleListeners != null && !ruleListeners.isEmpty()) {
            for (RuleListener ruleListener : ruleListeners) {
                rulesEngineBuilder.withRuleListener(ruleListener);
            }
        }
    }

    /*
     * Setters for dependency injection. 
     */

    public void setRuleListeners(List<RuleListener> ruleListeners) {
        this.ruleListeners = ruleListeners;
    }

    public void setRules(List<Object> rules) {
        this.rules = rules;
    }

    public void setPriorityThreshold(int priorityThreshold) {
        this.priorityThreshold = priorityThreshold;
    }

    public void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public void setSkipOnFirstAppliedRule(boolean skipOnFirstAppliedRule) {
        this.skipOnFirstAppliedRule = skipOnFirstAppliedRule;
    }

    public void setSkipOnFirstFailedRule(boolean skipOnFirstFailedRule) {
        this.skipOnFirstFailedRule = skipOnFirstFailedRule;
    }

    public void setName(String name) {
        this.name = name;
    }
}