package com.verisure.xad.poc.inv.kstream.launcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import com.verisure.xad.poc.inv.kstream.Application;

@ContextConfiguration(classes = Application.class)
@SpringBootTest( properties = {"server.port=8080", "spring.cloud.kubernetes.enabled=false"}, classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LauncherTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(this.context);
    }

}
