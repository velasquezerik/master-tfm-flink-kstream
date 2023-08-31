package com.verisure.xad.poc.inv.kstream.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.springframework.web.server.ResponseStatusException;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeJars;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.verisure.xad.poc.inv.kstream.processor.exception.ProcessException;

@AnalyzeClasses(packages = "com.verisure.xad.poc.inv.kstream", importOptions = {DoNotIncludeTests.class, DoNotIncludeJars.class})
public class CodingRulesTest {

    @ArchTest
    static ArchRule no_classes_should_throw_generic_exceptions =
        noClasses()
        .that(not(belongToAnyOf(ProcessException.class)))
        .should().callCodeUnitWhere(
            JavaCall.Predicates.target(
                AccessTarget.Predicates.constructor()
                    .and(AccessTarget.Predicates.declaredIn(JavaClass.Predicates.assignableTo(Throwable.class)))
                    .and(DescribedPredicate.not(AccessTarget.Predicates.declaredIn(JavaClass.Predicates.assignableTo(ProcessException.class))))
                    .and(DescribedPredicate.not(AccessTarget.Predicates.declaredIn(JavaClass.Predicates.assignableTo(ResponseStatusException.class))))
                    .and(DescribedPredicate.not(AccessTarget.Predicates.declaredIn(JavaClass.Predicates.assignableTo(AssertionError.class))))
        ));

    @ArchTest
    static ArchRule all_exceptions_should_extend_base_exception =
        classes()
        .that().areAssignableTo(Throwable.class)
        .should().beAssignableTo(ProcessException.class);
}