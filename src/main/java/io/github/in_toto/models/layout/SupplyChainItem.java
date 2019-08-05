package io.github.in_toto.models.layout;

import java.util.List;

import io.github.in_toto.exceptions.FormatError;

public abstract class SupplyChainItem {
    private String name;
    private List<Rule> expectedMaterials;
    private List<Rule> expectedProducts;
    
    public SupplyChainItem(String name, List<Rule> expectedMaterials, List<Rule> expectedProducts) {
        super();
        this.name = name;
        this.expectedMaterials = expectedMaterials;
        this.expectedProducts = expectedProducts;
    }
    
    /**
     * Convenience method to parse the passed rule string into a list and append it
     * to the item's list of expected_materials.
     * 
     * @param ruleString An artifact rule string, whose list representation is
     *                   parseable by in_toto.rulelib.unpack_rule
     * 
     */
    public void addMaterialRuleFromString(String ruleString) {
        this.expectedMaterials.addAll(Rule.getRulesFromString(ruleString));
    }
    
    /**
     * Convenience method to parse the passed rule string into a list and append it
     * to the item's list of expected_materials.
     * 
     * @param ruleString An artifact rule string, whose list representation is
     *                   parseable by in_toto.rulelib.unpack_rule
     * 
     * @throws FormatError If the passed rule_string is not a string. If the parsed
     *                     rule_string cannot be unpacked using rulelib.
     */
    public void addProductRuleFromString(String ruleString) throws FormatError {
        this.expectedProducts.addAll(Rule.getRulesFromString(ruleString));
    }
}
