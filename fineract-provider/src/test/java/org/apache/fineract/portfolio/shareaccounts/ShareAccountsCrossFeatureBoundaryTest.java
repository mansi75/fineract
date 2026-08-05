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
package org.apache.fineract.portfolio.shareaccounts;

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
import org.springframework.modulith.core.ApplicationModules;

/**
 * Guards the share account feature boundary: it may only reach into fineract-core and fineract-command.
 *
 * <p>
 * The repository-wide {@code AllModulesCrossFeatureBoundaryTest} reports on every module at once and therefore has to
 * stay tolerant of the modules that are not decoupled yet. This test is the strict counterpart for the one module that
 * <em>is</em> decoupled, so a regression in share accounts fails on its own instead of being lost in a global report.
 */
@Slf4j
class ShareAccountsCrossFeatureBoundaryTest {

    private static final String BASE = "org.apache.fineract";
    private static final String SHARE_ACCOUNTS_PACKAGE = BASE + ".portfolio.shareaccounts";

    private static final Set<String> FOUNDATION_ARTIFACTS = Set.of("fineract-core", "fineract-command");

    private static final Pattern FINERACT_ARTIFACT = Pattern.compile("(?<=/)fineract-[a-z0-9-]+");
    private static final Pattern ARTIFACT_VERSION_SUFFIX = Pattern.compile("-\\d.*$");

    @Test
    void shareAccountsMustNotDependOnOtherFeatureModules() {
        Set<String> violations = new TreeSet<>();
        shareAccountsModule().getDirectDependencies(modules()).stream().forEach(dependency -> {
            JavaClass target = dependency.getTargetType();
            if (!isFoundation(target)) {
                violations.add(target.getName() + " (" + owningArtifact(target) + ")");
            }
        });

        assertThat(violations) //
                .as("Share accounts may depend only on fineract-core and fineract-command. Reach other features "
                        + "through a contract declared in this module and implemented by the owning feature, through "
                        + "command/event boundaries or by-id references, or move the shared type into fineract-core. "
                        + "Offending types (with owning artifact): %s", violations) //
                .isEmpty();
    }

    @Test
    @EnabledIfSystemProperty(named = "shareaccounts.boundary.report", matches = "true")
    void printShareAccountsCrossFeatureDependencyReport() {
        Map<String, Set<String>> targetsBySourceType = new TreeMap<>();
        shareAccountsModule().getDirectDependencies(modules()).stream().forEach(dependency -> {
            JavaClass target = dependency.getTargetType();
            String status = isFoundation(target) ? "foundation" : "VIOLATION";
            targetsBySourceType.computeIfAbsent(dependency.getSourceType().getName(), key -> new TreeSet<>())
                    .add(target.getName() + " [" + owningArtifact(target) + " : " + status + "]");
        });

        log.info("==== Share accounts cross-feature dependency report ====");
        targetsBySourceType.forEach((source, targets) -> log.info("{} -> {}", source, targets));
    }

    private static ApplicationModules modules() {
        return ModulesHolder.MODULES;
    }

    private static ApplicationModule shareAccountsModule() {
        return modules().stream() //
                .filter(module -> module.getBasePackage().getName().equals(SHARE_ACCOUNTS_PACKAGE)) //
                .findFirst() //
                .orElseThrow(() -> new IllegalStateException("Share accounts module not found in the model; is " + SHARE_ACCOUNTS_PACKAGE
                        + "/package-info.java annotated with @ApplicationModule?"));
    }

    private static boolean isFoundation(JavaClass type) {
        return FOUNDATION_ARTIFACTS.contains(owningArtifact(type));
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

    private static final class ModulesHolder {

        private static final ApplicationModules MODULES = ApplicationModules.of(BASE);
    }
}
