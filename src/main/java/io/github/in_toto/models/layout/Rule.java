package io.github.in_toto.models.layout;

import java.util.List;

import io.github.in_toto.exceptions.FormatError;

public class Rule {
    
    /**
     * Convenience method to parse the passed rule string into a list of rules.
     * 
     * @param ruleString An artifact rule string, whose list representation is
     *                   parseable by in_toto.rulelib.unpack_rule
     * 
     * @throws FormatError If the passed rule_string is not a string. If the parsed
     *                     rule_string cannot be unpacked using rulelib.
     */
    public static List<Rule> getRulesFromString(String ruleString) throws FormatError {
        return null;
    }

}
