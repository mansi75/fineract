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
package org.apache.fineract.portfolio.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModuleDependency;
import org.springframework.modulith.core.ApplicationModules;

@Slf4j
class CalendarCrossFeatureBoundaryTest {

    private static final String BASE = "org.apache.fineract";
    private static final String CALENDAR_PACKAGE = "org.apache.fineract.portfolio.calendar";

    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command", "fineract-validation");

    /**
     * Letters only, so a versioned jar name such as {@code fineract-core-1.16.0.jar} still resolves to fineract-core.
     */
    private static final Pattern FINERACT_ARTIFACT = Pattern.compile("fineract-[a-z]+(?:-[a-z]+)*");

    private static ApplicationModules modules;

    private static ApplicationModules modules() {
        if (modules == null) {
            modules = ApplicationModules.of(BASE);
        }
        return modules;
    }

    private static ApplicationModule calendarModule() {
        return modules().stream() //
                .filter(module -> module.getBasePackage().getName().equals(CALENDAR_PACKAGE)) //
                .findFirst() //
                .orElseThrow(() -> new IllegalStateException("Calendar module not found in the model"));
    }

    private static String featureKey(String typeName) {
        String prefix = BASE + ".";
        if (!typeName.startsWith(prefix)) {
            return typeName;
        }
        String remainder = typeName.substring(prefix.length());
        int firstDot = remainder.indexOf('.');
        if (firstDot < 0) {
            return remainder;
        }
        int secondDot = remainder.indexOf('.', firstDot + 1);
        return secondDot < 0 ? remainder : remainder.substring(0, secondDot);
    }

    /**
     * The last match wins: an enclosing checkout directory can itself be named {@code fineract-...}, so the first match
     * in the URI is not necessarily the module that owns the type.
     */
    private static String owningArtifact(JavaClass type) {
        return type.getSource() //
                .map(source -> source.getUri().toString()) //
                .map(uri -> {
                    Matcher matcher = FINERACT_ARTIFACT.matcher(uri);
                    String artifact = uri;
                    while (matcher.find()) {
                        artifact = matcher.group();
                    }
                    return artifact;
                }) //
                .orElse("(unknown-source)");
    }

    private static boolean isFoundation(JavaClass type) {
        return FOUNDATION_ARTIFACTS.contains(owningArtifact(type));
    }

    @EnabledIfSystemProperty(named = "calendar.boundary.report", matches = "true")
    @Test
    void printCalendarCrossFeatureDependencyReport() {
        Map<String, Set<String>> sourceTypeToTargets = new TreeMap<>();
        Set<String> violationFeatures = new TreeSet<>();
        Set<String> allowedFromCore = new TreeSet<>();

        calendarModule().getDirectDependencies(modules()).stream().forEach((ApplicationModuleDependency dependency) -> {
            JavaClass targetType = dependency.getTargetType();
            String sourceType = dependency.getSourceType().getName();
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
        });

        log.info("==== Calendar cross-feature dependency report ====");
        log.info("base package         : {}", BASE);
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
    void calendarMustNotImportOtherFeatureModules() {
        Set<String> featureDependencies = new TreeSet<>();

        calendarModule().getDirectDependencies(modules()).stream().forEach((ApplicationModuleDependency dependency) -> {
            JavaClass targetType = dependency.getTargetType();
            if (!isFoundation(targetType)) {
                featureDependencies.add(featureKey(targetType.getName()) + " (" + owningArtifact(targetType) + ")");
            }
        });

        assertThat(featureDependencies) //
                .as("Calendar may depend only on fineract-core and fineract-command. Types referenced from other "
                        + "feature modules must be reached via core read-contracts / DTOs, command/event "
                        + "boundaries, or by-id references (or the shared type moved into core). Offending "
                        + "feature packages (with owning artifact): %s", featureDependencies) //
                .isEmpty();
    }
}
