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
package org.apache.fineract.portfolio.tax;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Splitter;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Verifies that the tax feature depends only on the foundation artifacts. The tax feature is a nested package, so it is
 * resolved with ArchUnit rather than with the Spring Modulith module model, which only detects the direct sub-packages
 * of {@code org.apache.fineract} as modules.
 */
@Slf4j
class TaxCrossFeatureBoundaryTest {

    private static final String BASE = "org.apache.fineract";
    private static final String TAX_PACKAGE = BASE + ".portfolio.tax";

    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command");

    private static final Pattern FINERACT_ARTIFACT = Pattern.compile("(?<=/)fineract-[a-z0-9-]+");

    private static final Pattern ARTIFACT_VERSION_SUFFIX = Pattern.compile("-\\d.*$");

    private static List<Dependency> taxDependenciesOnOtherFeatures() {
        return ClassesHolder.CLASSES.stream() //
                .filter(TaxCrossFeatureBoundaryTest::isTaxType) //
                .flatMap(taxType -> taxType.getDirectDependenciesFromSelf().stream()) //
                .filter(dependency -> dependency.getTargetClass().getPackageName().startsWith(BASE)) //
                .filter(dependency -> !isTaxType(dependency.getTargetClass())) //
                .toList();
    }

    private static boolean isTaxType(JavaClass type) {
        String packageName = type.getPackageName();
        return packageName.equals(TAX_PACKAGE) || packageName.startsWith(TAX_PACKAGE + ".");
    }

    private static String featureKey(String typeName) {
        String prefix = BASE + ".";
        if (!typeName.startsWith(prefix)) {
            return typeName;
        }
        List<String> parts = Splitter.on('.').splitToList(typeName.substring(prefix.length()));
        return parts.size() >= 2 ? parts.get(0) + "." + parts.get(1) : parts.get(0);
    }

    private static String owningArtifact(JavaClass type) {
        return type.getSource() //
                .map(source -> source.getUri().toString()) //
                .map(uri -> {
                    Matcher matcher = FINERACT_ARTIFACT.matcher(uri);
                    String artifact = null;
                    while (matcher.find()) {
                        artifact = matcher.group();
                    }
                    return artifact == null ? uri : ARTIFACT_VERSION_SUFFIX.matcher(artifact).replaceAll("");
                }) //
                .orElse("(unknown-source)");
    }

    private static boolean isFoundation(JavaClass type) {
        return FOUNDATION_ARTIFACTS.contains(owningArtifact(type));
    }

    @Test
    @EnabledIfSystemProperty(named = "tax.boundary.report", matches = "true")
    void printTaxCrossFeatureDependencyReport() {
        Map<String, Set<String>> sourceTypeToTargets = new TreeMap<>();
        Set<String> violationFeatures = new TreeSet<>();
        Set<String> allowedFromCore = new TreeSet<>();

        for (Dependency dependency : taxDependenciesOnOtherFeatures()) {
            JavaClass targetType = dependency.getTargetClass();
            String sourceType = dependency.getOriginClass().getName();
            String featureKey = featureKey(targetType.getName());
            String artifact = owningArtifact(targetType);
            boolean foundation = isFoundation(targetType);

            String label = featureKey + "  [" + artifact + (foundation ? " : foundation]" : " : VIOLATION]");
            sourceTypeToTargets.computeIfAbsent(sourceType, key -> new TreeSet<>()).add(label);

            if (foundation) {
                allowedFromCore.add(featureKey + " (" + artifact + ")");
            } else {
                violationFeatures.add(featureKey + " (" + artifact + ")");
            }
        }

        log.info("==== Tax cross-feature dependency report ====");
        log.info("tax package          : {}", TAX_PACKAGE);
        log.info("-- source type -> referenced feature packages [owning artifact : status] --");

        sourceTypeToTargets.forEach((source, targets) -> {
            log.info("source type          : {}", source);
            log.info("referenced targets   : {}", targets);
        });

        log.info("-- allowed dependencies --");
        log.info("allowed from core    : {}", allowedFromCore);
        log.info("-- dependency violations --");
        log.info("violation features   : {}", violationFeatures);
    }

    @Test
    void taxMustNotImportOtherFeatureModules() {
        Set<String> featureDependencies = new TreeSet<>();

        for (Dependency dependency : taxDependenciesOnOtherFeatures()) {
            JavaClass targetType = dependency.getTargetClass();
            if (!isFoundation(targetType)) {
                featureDependencies.add(featureKey(targetType.getName()) + " (" + owningArtifact(targetType) + ")");
            }
        }

        assertThat(featureDependencies) //
                .as("Tax may depend only on fineract-core and fineract-command. Types referenced from other feature "
                        + "modules must be reached via core read-contracts / DTOs, command/event boundaries, or "
                        + "by-id references (or the shared type moved into core). Offending feature packages "
                        + "(with owning artifact): %s", featureDependencies) //
                .isEmpty();
    }

    private static final class ClassesHolder {

        private static final JavaClasses CLASSES = new ClassFileImporter().importPackages(BASE);
    }
}
