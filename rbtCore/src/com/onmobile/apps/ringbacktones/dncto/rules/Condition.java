/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto.rules;

import java.util.List;

import com.onmobile.apps.ringbacktones.dncto.DNCTOConstants;
import com.onmobile.apps.ringbacktones.dncto.DNCTOContext;
import com.onmobile.apps.ringbacktones.dncto.DNCTOConstants.ConditionType;
import com.onmobile.dnctoservice.exception.DNCTOException;

/**
 * Condition is {@link Rule} and it will be used for grouping set of rules with
 * specific condition operation defined in {@link DNCTOConstants.ConditionType}.
 * 
 * @author vinayasimha.patil
 */
public class Condition implements Rule
{
	/**
	 * Holds the condition operation type.
	 */
	@RuleAttribute
	private String conditionType = null;

	/**
	 * Holds the list of child rules.
	 */
	private List<Rule> rules = null;

	/**
	 * Returns the conditionType.
	 * 
	 * @return the conditionType
	 */
	public String getConditionType()
	{
		return conditionType;
	}

	/**
	 * Sets the conditionType.
	 * 
	 * @param conditionType
	 *            the conditionType to set
	 */
	public void setConditionType(String conditionType)
	{
		this.conditionType = conditionType;
	}

	/**
	 * Returns the rules.
	 * 
	 * @return the rules
	 */
	public List<Rule> getRules()
	{
		return rules;
	}

	/**
	 * Sets the rules.
	 * 
	 * @param rules
	 *            the rules to set
	 */
	public void setRules(List<Rule> rules)
	{
		this.rules = rules;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.dncto.rules.Rule#applyRule(com.onmobile
	 * .apps.ringbacktones.dncto.DNCTOContext)
	 */
	/**
	 * If condition operation is <b>AND</b> then returns true if all child rules
	 * satisfied or if condition operation is <b>OR</b> then returns true if any
	 * one child rule satisfied
	 */
	@Override
	public boolean applyRule(DNCTOContext dnctoContext) throws DNCTOException
	{
		if (rules == null || rules.size() == 0)
			return true;

		boolean isANDOperation = ConditionType.valueOf(conditionType) == ConditionType.AND;
		boolean isOROperation = ConditionType.valueOf(conditionType) == ConditionType.OR;

		boolean ruleSatisfied = false;
		for (Rule rule : rules)
		{
			ruleSatisfied = rule.applyRule(dnctoContext);
			if (isANDOperation && !ruleSatisfied)
				break;
			else if (isOROperation && ruleSatisfied)
				break;
		}

		return ruleSatisfied;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * Returns the string representation of the object.
	 * 
	 * @return the string representation of the object
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Condition[conditionType = ");
		builder.append(conditionType);
		builder.append(", rules = ");
		builder.append(rules);
		builder.append("]");
		return builder.toString();
	}
}
