package com.product.config;

import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.CommandScope;
import liquibase.exception.LiquibaseException;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.integration.spring.SpringResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

import static liquibase.command.core.UpdateCommandStep.CHANGELOG_FILE_ARG;
import static liquibase.command.core.UpdateCommandStep.COMMAND_NAME;
import static liquibase.command.core.helpers.DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS;
import static liquibase.command.core.helpers.DbUrlConnectionCommandStep.DATABASE_ARG;

@Slf4j
public class MongoLiquibaseRunner implements CommandLineRunner, ResourceLoaderAware {

    public final MongoLiquibaseDatabase database;

    protected ResourceLoader resourceLoader;

    public void setResourceLoader(final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public MongoLiquibaseRunner(MongoLiquibaseDatabase database) {
        this.database = database;
    }

    public void run(final String... args) throws Exception {
        runInScope(() -> {
            log.info("Starting process of Liquibase update.");
            CommandScope updateCommand = new CommandScope(COMMAND_NAME);
            updateCommand.addArgumentValue(DATABASE_ARG, database);
            updateCommand.addArgumentValue(CHANGELOG_FILE_ARG, "classpath:db/changelog/liquibase-changelog.xml");
            updateCommand.addArgumentValue(CHANGELOG_PARAMETERS, new ChangeLogParameters(database));
            updateCommand.execute();
        });
    }

    private void runInScope(Scope.ScopedRunner<CommandScope> scopedRunner) throws LiquibaseException {
        Map<String, Object> scopeObjects = new HashMap<>();
        scopeObjects.put(Scope.Attr.database.name(), database);
        scopeObjects.put(Scope.Attr.resourceAccessor.name(), new SpringResourceAccessor(resourceLoader));
        try {
            Scope.child(scopeObjects, scopedRunner);
        } catch (Exception e) {
            log.error("Error when running scripts", e);
            throw new LiquibaseException(e);
        }
    }

}