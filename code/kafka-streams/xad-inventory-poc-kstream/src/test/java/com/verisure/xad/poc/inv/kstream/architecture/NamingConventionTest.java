package com.verisure.xad.poc.inv.kstream.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.springframework.stereotype.Component;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.verisure.xad.poc.inv.kstream")
public class NamingConventionTest {


    @ArchTest
    static ArchRule config_should_be_suffixed =
            classes()
                    .that().resideInAPackage("..config")
                    .and().areAnnotatedWith(org.springframework.context.annotation.Configuration.class)
                    .should().haveSimpleNameEndingWith("Config");

    @ArchTest
    static ArchRule classes_named_config_should_be_in_a_config_package =
            classes()
                    .that().haveSimpleNameContaining("Config")
                    .should().resideInAPackage("..config");
    
    @ArchTest
    static ArchRule topology_should_be_suffixed =
            classes()
                    .that().resideInAPackage("..topology")
                    .and().areAnnotatedWith(Component.class)
                    .should().haveSimpleNameEndingWith("Topology");

    @ArchTest
    static ArchRule classes_named_topology_should_be_in_a_topology_package =
            classes()
                    .that().haveSimpleNameContaining("Topology")
                    .should().resideInAPackage("..topology");

    @ArchTest
    static ArchRule processor_should_be_suffixed =
            classes()
                    .that().resideInAPackage("..processor")
                    .and().areAnnotatedWith(Component.class)
                    .should().haveSimpleNameEndingWith("Processor");

//    @ArchTest
//    static ArchRule classes_named_processor_should_be_in_a_processor_package =
//            classes()
//                    .that().haveSimpleNameContaining("Processor")
//                    .should().resideInAPackage("..processor");

}