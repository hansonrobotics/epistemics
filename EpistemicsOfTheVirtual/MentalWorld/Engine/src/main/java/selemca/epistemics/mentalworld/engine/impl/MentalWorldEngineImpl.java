/*
 * This source file is part of the Epistemics of the Virtual software.
 * It was created by:
 * Johan F. Hoorn - theoretical model and algorithms
 * Henri Zwols - software design and engineering
 */
package selemca.epistemics.mentalworld.engine.impl;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import selemca.epistemics.mentalworld.beliefsystem.repository.AssociationRepository;
import selemca.epistemics.mentalworld.beliefsystem.repository.ConceptRepository;
import selemca.epistemics.mentalworld.beliefsystem.service.BeliefModelService;
import selemca.epistemics.mentalworld.engine.MentalWorldEngine;
import selemca.epistemics.mentalworld.engine.MentalWorldEngineState;
import selemca.epistemics.mentalworld.engine.accept.Engine;
import selemca.epistemics.mentalworld.engine.workingmemory.WorkingMemory;
import selemca.epistemics.mentalworld.registry.DeriverNodeProviderRegistry;
import selemca.epistemics.mentalworld.registry.MetaphorProcessorRegistry;

import java.util.*;

import static selemca.epistemics.mentalworld.engine.workingmemory.AttributeKind.ENGINE_SETTINGS;
import static selemca.epistemics.mentalworld.engine.workingmemory.AttributeKind.NEW_CONTEXT;
import static selemca.epistemics.mentalworld.engine.workingmemory.AttributeKind.OBSERVATION_FEATURES;

@Component("mentalWorldEngine")
public class MentalWorldEngineImpl implements MentalWorldEngine {
    public static final int MAXIMUM_TRAVERSALS_DEFAULT = 1;

    @Autowired
    private BeliefModelService beliefModelService;

    @Autowired
    private MetaphorProcessorRegistry metaphorProcessorRegistry;

    @Autowired
    private DeriverNodeProviderRegistry deriverNodeProviderRegistry;

    @Autowired
    private ConceptRepository conceptRepository;

    @Autowired
    private AssociationRepository associationRepository;

    @Autowired
    private Configuration applicationSettings;

    public BeliefModelService getBeliefModelService() {
        return beliefModelService;
    }

    public MetaphorProcessorRegistry getMetaphorProcessorRegistry() {
        return metaphorProcessorRegistry;
    }

    public DeriverNodeProviderRegistry getDeriverNodeProviderRegistry() {
        return deriverNodeProviderRegistry;
    }

    public ConceptRepository getConceptRepository() {
        return conceptRepository;
    }

    public AssociationRepository getAssociationRepository() {
        return associationRepository;
    }

    public Configuration getApplicationSettings() {
        return applicationSettings;
    }

    @Override
    public void acceptObservation(Set<String> observationFeatures, Logger logger) {
        beliefModelService.getContext()
            .map(context -> {
                VirtualModelEngineState virtualModelEngineState = new VirtualModelEngineState(this, context, observationFeatures, logger);
                virtualModelEngineState.acceptObservation();
                return context;
            })
            .orElseGet(() -> {
                logger.info("There is no context. We are mentally blind");
                return null;
            });
    }

    @Override
    public boolean acceptObservation(Set<String> observationFeatures, Engine engineSettings, Logger logger) {
        return beliefModelService.getContext()
            .map(context -> {
                MentalWorldEngineState mentalWorldModelEngineState = createState(logger);
                WorkingMemory workingMemory = mentalWorldModelEngineState.getWorkingMemory();
                workingMemory.set(ENGINE_SETTINGS, engineSettings);
                workingMemory.set(OBSERVATION_FEATURES, observationFeatures);
                workingMemory.set(NEW_CONTEXT, context);

                mentalWorldModelEngineState.acceptObservation();
                return mentalWorldModelEngineState.isObservationAccepted();
            })
            .orElseGet(() -> {
                logger.info("There is no context. We are mentally blind");
                return false;
            });
    }

    @Override
    public MentalWorldEngineState createState(Logger logger) {
        return new VirtualModelEngineState(this, new WorkingMemory(), logger);
    }
}
