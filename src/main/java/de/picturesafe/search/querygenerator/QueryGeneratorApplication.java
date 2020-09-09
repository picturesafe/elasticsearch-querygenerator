/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.picturesafe.search.querygenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class QueryGeneratorApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryGeneratorApplication.class);

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            try {
                logBeanDefinitions(ctx);
            } catch (Exception e) {
                LOGGER.error("Command line runner failed!", e);
            }
        };
    }

    private void logBeanDefinitions(ApplicationContext ctx) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Running Spring Boot application with following bean definitions:");
            final String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (final String beanName : beanNames) {
                LOGGER.debug("- {} : {}", beanName, ctx.getType(beanName));
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(QueryGeneratorApplication.class, args);
    }
}
