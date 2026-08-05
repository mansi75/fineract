/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.charge;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Charge is nested under {@code org.apache.fineract.portfolio}, so Spring Modulith's default detection folds it into
 * the surrounding portfolio module and never reports charge-to-tax style edges. The classpath is scanned directly
 * instead, which also keeps this test free of any dependency the module does not already have.
 */
@Slf4j
@EnabledIfSystemProperty(named = "charge.boundary.report", matches = "true")
class ChargeCrossFeatureBoundaryTest {

    private static final String BASE_PACKAGE = "org.apache.fineract";

    private static final String CHARGE_ARTIFACT = "fineract-charge";

    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command");

    private static final String ARTIFACT_PREFIX = "fineract-";

    private static final String UNKNOWN_ARTIFACT = "(unknown-artifact)";

    @Test
    void chargeMustNotImportOtherFeatureModules() {
        Map<String, Set<String>> violations = crossFeatureDependencies();

        assertThat(violations) //
                .as("Charge may depend only on fineract-core and fineract-command. Types referenced from "
                        + "other feature modules must be reached via core read-contracts / DTOs, command/event "
                        + "boundaries, or by-id references (or the shared type moved into core). Offending "
                        + "charge types (with the types they reference and the owning artifact): %s", violations) //
                .isEmpty();
    }

    @Test
    void printChargeCrossFeatureDependencyReport() {
        Map<String, Set<String>> dependenciesByArtifact = new TreeMap<>();

        try (ScanResult scanResult = scan()) {
            for (ClassInfo source : scanResult.getAllClasses()) {
                if (!CHARGE_ARTIFACT.equals(owningArtifact(source))) {
                    continue;
                }
                for (ClassInfo target : source.getClassDependencies()) {
                    if (CHARGE_ARTIFACT.equals(owningArtifact(target))) {
                        continue;
                    }
                    dependenciesByArtifact.computeIfAbsent(owningArtifact(target), key -> new TreeSet<>()).add(target.getName());
                }
            }
        }

        log.info("==== fineract-charge cross-artifact dependency report ====");
        log.info("foundation artifacts : {}", FOUNDATION_ARTIFACTS);
        dependenciesByArtifact.forEach((artifact, types) -> log.info("{} [{}] : {}",
                FOUNDATION_ARTIFACTS.contains(artifact) ? "allowed  " : "VIOLATION", artifact, types));
    }

    private static Map<String, Set<String>> crossFeatureDependencies() {
        Map<String, Set<String>> violations = new TreeMap<>();

        try (ScanResult scanResult = scan()) {
            for (ClassInfo source : scanResult.getAllClasses()) {
                if (!CHARGE_ARTIFACT.equals(owningArtifact(source))) {
                    continue;
                }
                for (ClassInfo target : source.getClassDependencies()) {
                    String artifact = owningArtifact(target);
                    if (CHARGE_ARTIFACT.equals(artifact) || FOUNDATION_ARTIFACTS.contains(artifact)) {
                        continue;
                    }
                    violations.computeIfAbsent(source.getName(), key -> new TreeSet<>()).add(target.getName() + " (" + artifact + ")");
                }
            }
        }

        return violations;
    }

    private static ScanResult scan() {
        return new ClassGraph().enableInterClassDependencies().acceptPackages(BASE_PACKAGE).scan();
    }

    /**
     * Resolves the Fineract module a scanned type was loaded from, so that types sharing a package across modules (for
     * example {@code org.apache.fineract.portfolio.charge} which lives in fineract-core and in fineract-charge) are
     * attributed to the artifact that actually owns them.
     */
    private static String owningArtifact(ClassInfo classInfo) {
        for (File element = classInfo.getClasspathElementFile(); element != null; element = element.getParentFile()) {
            String name = element.getName();
            if (name.startsWith(ARTIFACT_PREFIX)) {
                return name.replaceFirst("-\\d.*$", "");
            }
        }
        return UNKNOWN_ARTIFACT;
    }
}
