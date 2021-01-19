package io.websitecd.content.git.init;

import io.quarkus.runtime.ApplicationLifecycleManager;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.websitecd.content.git.config.GitContentUtils;
import io.websitecd.content.git.config.model.ContentConfig;
import io.websitecd.content.git.config.model.GitComponent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@QuarkusMain
public class GitInitApp implements QuarkusApplication {

    Logger log = Logger.getLogger(GitInitApp.class);

    @ConfigProperty(name = "app.config.path")
    String configPath;

    @ConfigProperty(name = "app.target.dir")
    String targetDir;

    @Inject
    Vertx vertx;

    @Override
    public int run(String... args) throws Exception {
        log.infof("loading configuration path=%s", configPath);
        ContentConfig contentConfig;
        File file = new File(configPath);
        if (!file.exists()) {
            throw new Exception("file '" + configPath + "' does not exists");
        }
        File outputDir = new File(targetDir);
        if (!outputDir.exists()) {
            throw new Exception("path '" + outputDir.getAbsolutePath() + "' does not exists");
        }

        try (InputStream is = new FileInputStream(file)) {
            contentConfig = GitContentUtils.loadYaml(is);
        }

        DeploymentOptions options = new DeploymentOptions().setWorker(true);
        List<Future> promises = new ArrayList<>();
        for (GitComponent component : contentConfig.getComponents()) {
            Future<Void> promise = deployVerticle(component, targetDir, options);
            promises.add(promise);
        }
        CompositeFuture.all(promises).onSuccess(result -> {
            log.infof("Done");
            ApplicationLifecycleManager.exit(0);
        }).onFailure(err -> {
            log.error("ERROR", err);
            ApplicationLifecycleManager.exit(1);
        });

        Quarkus.waitForExit();
        return ApplicationLifecycleManager.getExitCode();
    }

    public Future<Void> deployVerticle(GitComponent component, String targetDir, DeploymentOptions options) {
        Future<Void> promise = Future.future();

        GitCloneVerticle cloneVerticle = new GitCloneVerticle(component, targetDir);
        log.infof("Deploying git clone verticle dir=%s", component.getDir());
        vertx.deployVerticle(cloneVerticle, options, result -> {
            if (result.failed()) {
                promise.fail(result.cause());
            } else {
                promise.complete();
            }
        });
        return promise;
    }
}
