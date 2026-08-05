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
package org.apache.fineract.portfolio.savings;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Splitter;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guards the cross-feature boundary of the fixed deposit feature: it may reach for {@code fineract-core} and
 * {@code fineract-command} only.
 * <p>
 * Unlike accounting or client, fixed deposit is not a package of its own - it is a set of classes living inside
 * {@code org.apache.fineract.portfolio.savings}, split across {@code fineract-savings} and {@code fineract-provider}.
 * Spring Modulith derives its modules from packages alone and therefore cannot express this feature, so the boundary is
 * checked with ArchUnit using the same feature definition the modularization outline uses for F6: types under
 * {@code portfolio.savings} whose simple name starts with {@code FixedDeposit}.
 * <p>
 * References into the savings host packages ({@code portfolio.savings} and the deposit-only
 * {@code portfolio.interestratechart}) are the residue of {@code FixedDepositAccount extends SavingsAccount} and
 * {@code FixedDepositProduct extends SavingsProduct}. Breaking that inheritance is the separate "Deposits" phase of the
 * outline, so those references are listed explicitly below, reported on every run, and deliberately not treated as
 * violations here. Everything else must cross the boundary as a {@code fineract-core} read contract / DTO, a
 * {@code fineract-command} command or event, or a by-id reference.
 */
class FixedDepositCrossFeatureBoundaryTest {

    private static final Logger LOG = LoggerFactory.getLogger(FixedDepositCrossFeatureBoundaryTest.class);

    private static final String BASE = "org.apache.fineract";

    /** The feature owns the {@code FixedDeposit*} types under this package - see F6 in the modularization outline. */
    private static final String FEATURE_PACKAGE = BASE + ".portfolio.savings";

    private static final String FEATURE_CLASS_PREFIX = "FixedDeposit";

    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command");

    /**
     * Savings packages the fixed deposit classes are still embedded in through inheritance; removed by the "Deposits"
     * phase, reported but not failed here.
     */
    private static final Set<String> HOST_PACKAGES = Set.of(BASE + ".portfolio.savings", BASE + ".portfolio.interestratechart");

    private static final Pattern FINERACT_ARTIFACT = Pattern.compile("(?<=/)fineract-[a-z0-9-]+");

    private static final Pattern ARTIFACT_VERSION_SUFFIX = Pattern.compile("-\\d.*$");

    private static JavaClasses classes() {
        return ClassesHolder.CLASSES;
    }

    private static boolean isFixedDepositFeature(JavaClass type) {
        String packageName = type.getPackageName();
        boolean inFeaturePackage = packageName.equals(FEATURE_PACKAGE) || packageName.startsWith(FEATURE_PACKAGE + ".");
        return inFeaturePackage && topLevelClass(type).getSimpleName().startsWith(FEATURE_CLASS_PREFIX);
    }

    /**
     * Nested types (the Swagger request/response DTOs, for instance) belong to whichever feature owns the class they
     * are declared in, so resolve them to their top-level class before matching the name prefix.
     */
    private static JavaClass topLevelClass(JavaClass type) {
        JavaClass current = type;
        while (current.getEnclosingClass().isPresent()) {
            current = current.getEnclosingClass().get();
        }
        return current;
    }

    private static boolean isHostPackage(JavaClass type) {
        String packageName = type.getPackageName();
        return HOST_PACKAGES.stream().anyMatch(host -> packageName.equals(host) || packageName.startsWith(host + "."));
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

    /** Every {@code org.apache.fineract} type a fixed deposit class references, minus the feature's own types. */
    private static Set<JavaClass> outboundTypes(JavaClass source) {
        Set<JavaClass> targets = new TreeSet<>(java.util.Comparator.comparing(JavaClass::getName));
        for (Dependency dependency : source.getDirectDependenciesFromSelf()) {
            JavaClass target = dependency.getTargetClass().getBaseComponentType();
            if (!target.getPackageName().startsWith(BASE) || isFixedDepositFeature(target)) {
                continue;
            }
            targets.add(target);
        }
        return targets;
    }

    private static Set<JavaClass> featureClasses() {
        Set<JavaClass> featureClasses = new TreeSet<>(java.util.Comparator.comparing(JavaClass::getName));
        classes().stream().filter(FixedDepositCrossFeatureBoundaryTest::isFixedDepositFeature).forEach(featureClasses::add);
        return featureClasses;
    }

    @Test
    @EnabledIfSystemProperty(named = "fixeddeposit.boundary.report", matches = "true")
    void printFixedDepositCrossFeatureDependencyReport() {
        Map<String, Set<String>> sourceTypeToTargets = new TreeMap<>();
        Set<String> violationFeatures = new TreeSet<>();
        Set<String> allowedFromFoundation = new TreeSet<>();
        Set<String> hostResidue = new TreeSet<>();

        for (JavaClass source : featureClasses()) {
            for (JavaClass target : outboundTypes(source)) {
                String featureKey = featureKey(target.getName());
                String artifact = owningArtifact(target);
                String status;
                if (isFoundation(target)) {
                    status = " : foundation]";
                    allowedFromFoundation.add(featureKey + " (" + artifact + ")");
                } else if (isHostPackage(target)) {
                    status = " : savings host - Deposits phase]";
                    hostResidue.add(target.getName().replace(BASE + ".", "") + " (" + artifact + ")");
                } else {
                    status = " : VIOLATION]";
                    violationFeatures.add(featureKey + " (" + artifact + ")");
                }
                sourceTypeToTargets.computeIfAbsent(source.getName(), key -> new TreeSet<>()).add(featureKey + "  [" + artifact + status);
            }
        }

        LOG.info("==== Fixed deposit cross-feature dependency report ====");
        LOG.info("base package         : {}", BASE);
        LOG.info("feature classes      : {}", featureClasses().size());
        LOG.info("-- source type -> referenced feature packages [owning artifact : status] --");
        sourceTypeToTargets.forEach((source, targets) -> {
            LOG.info("source type          : {}", source);
            LOG.info("referenced targets   : {}", targets);
        });
        LOG.info("-- allowed dependencies --");
        LOG.info("allowed from core    : {}", allowedFromFoundation);
        LOG.info("-- savings host residue (removed by the Deposits phase) --");
        LOG.info("host residue types   : {} -> {}", hostResidue.size(), hostResidue);
        LOG.info("-- dependency violations --");
        LOG.info("violation features   : {}", violationFeatures);
    }

    @Test
    void fixedDepositFeatureIsDiscoverable() {
        assertThat(featureClasses()) //
                .as("No %s* type found under %s - the feature definition is stale and the boundary check below would " + "pass vacuously",
                        FEATURE_CLASS_PREFIX, FEATURE_PACKAGE) //
                .isNotEmpty();
    }

    @Test
    void fixedDepositMustNotImportOtherFeatureModules() {
        Map<String, Set<String>> offending = new TreeMap<>();

        for (JavaClass source : featureClasses()) {
            for (JavaClass target : outboundTypes(source)) {
                if (isFoundation(target) || isHostPackage(target)) {
                    continue;
                }
                offending.computeIfAbsent(source.getName().replace(BASE + ".", ""), key -> new TreeSet<>())
                        .add(target.getName().replace(BASE + ".", "") + " (" + owningArtifact(target) + ")");
            }
        }

        assertThat(offending) //
                .as("Fixed deposit may depend only on fineract-core and fineract-command (plus the savings host "
                        + "packages it still inherits from, which the Deposits phase removes). Types referenced from "
                        + "other feature modules must be reached via core read-contracts / DTOs, command/event "
                        + "boundaries, or by-id references (or the shared type moved into core). Offending fixed "
                        + "deposit types (each with the foreign types it reaches for): %s", offending) //
                .isEmpty();
    }

    private static final class ClassesHolder {

        private static final JavaClasses CLASSES = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }
}
