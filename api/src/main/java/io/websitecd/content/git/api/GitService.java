package io.websitecd.content.git.api;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class GitService {

    Logger log = Logger.getLogger(GitService.class);

    @ConfigProperty(name = "app.data.dir")
    String dataDir;

    void onStart(@Observes StartupEvent ev) {
        log.infof("content-git-api started. APP_DATA_DIR=%s", dataDir);
    }

    public List<String> list() {
        File file = new File(dataDir);
        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());

        if (directories == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(directories);
    }

    public void updateGit(String dir) throws FileNotFoundException, GitAPIException {
        log.infof("Update git %s", dir);
        File baseDir = new File(dataDir);
        File gitDir = new File(baseDir.getAbsolutePath() + "/" + dir);
        if (!gitDir.exists()) {
            throw new FileNotFoundException("dir " + gitDir.getAbsolutePath() + " does not exist");
        }

        Git git = Git.init().setDirectory(gitDir).call();
        git.pull();
    }
}
