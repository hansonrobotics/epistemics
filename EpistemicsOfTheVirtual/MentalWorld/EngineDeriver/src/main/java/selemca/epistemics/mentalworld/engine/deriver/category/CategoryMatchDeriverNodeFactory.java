/*
 * This source file is part of the Epistemics of the Virtual software.
 * It was created by:
 * Johan F. Hoorn - theoretical model and algorithms
 * Henri Zwols - software design and engineering
 */
package selemca.epistemics.mentalworld.engine.deriver.category;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import selemca.epistemics.mentalworld.beliefsystem.service.BeliefModelService;
import selemca.epistemics.mentalworld.engine.MentalWorldEngine;
import selemca.epistemics.mentalworld.engine.factory.DeriverNodeFactory;
import selemca.epistemics.mentalworld.engine.node.CategoryMatchDeriverNode;
import selemca.epistemics.mentalworld.engine.workingmemory.WorkingMemory;
import selemca.epistemics.mentalworld.registry.CategoryMatcherRegistry;

@Component
public class CategoryMatchDeriverNodeFactory implements DeriverNodeFactory<CategoryMatchDeriverNode> {
    private static final String CONFIGURATION_NAME = "categoryMatchDeriver.default";
    @Autowired
    private BeliefModelService beliefModelService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private CategoryMatcherRegistry categoryMatcherRegistry;

    @Autowired
    private Configuration applicationSettings;

    @Override
    public Class<CategoryMatchDeriverNode> getDeriverNodeClass() {
        return CategoryMatchDeriverNode.class;
    }

    @Override
    public String getName() {
        return CONFIGURATION_NAME;
    }

    @Override
    public CategoryMatchDeriverNode createDeriverNode(WorkingMemory workingMemory, MentalWorldEngine.Logger logger) {
        return categoryMatcherRegistry.getImplementation()
            .map(categoryMatcher -> new DefaultCategoryMatchDeriverNodeImpl(beliefModelService, workingMemory, categoryMatcher, logger, applicationSettings))
            .orElseThrow(() -> new IllegalStateException("No CategoryMatcher found. Failing"));
    }
}
