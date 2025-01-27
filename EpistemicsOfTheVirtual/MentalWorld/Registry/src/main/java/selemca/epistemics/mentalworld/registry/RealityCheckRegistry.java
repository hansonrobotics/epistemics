/*
 * This source file is part of the Epistemics of the Virtual software.
 * It was created by:
 * Johan F. Hoorn - theoretical model and algorithms
 * Henri Zwols - software design and engineering
 */
package selemca.epistemics.mentalworld.registry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import selemca.epistemics.mentalworld.engine.realitycheck.RealityCheck;
import selemca.epistemics.mentalworld.registry.config.RegistryKey;

import java.util.Map;

@Component
public class RealityCheckRegistry extends AbstractPluginRegistry<RealityCheck> {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public RealityCheckRegistry(Map<String, RealityCheck> implementations) {
        super(RegistryKey.REALITY_CHECK_IMPLEMENTATION, RealityCheck.class, implementations);
    }
}
