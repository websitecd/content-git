package io.websitecd.content.git.init;

import io.vertx.core.AbstractVerticle;
import io.websitecd.content.git.config.model.GitComponent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.merge.MergeStrategy;
import org.jboss.logging.Logger;

import java.io.File;

public class GitCloneVerticle extends AbstractVerticle {

    Logger log = Logger.getLogger(GitCloneVerticle.class);

    GitComponent component;

    String targetDir;

    public GitCloneVerticle(GitComponent component, String targetDir) {
        this.component = component;
        this.targetDir = targetDir;
    }

    @Override
    public void start() throws Exception {
        log.infof("Start git clone. component=%s", component);

        File baseDir = new File(targetDir);

        File gitDir = new File(baseDir.getAbsolutePath() + "/" + component.getDir());
        boolean gitDirExists = gitDir.exists();


        String gitUrl = component.getSpec().getUrl();
        String ref = component.getSpec().getRef();
        Git git = null;
        try {
            if (!gitDirExists) {
                log.infof("Directory %s not exists. Cloning git", gitDir);
                git = Git.cloneRepository().setURI(gitUrl).setDirectory(gitDir).setBranch(ref).call();
            } else {
                log.infof("Directory %s exists. Just pull", gitDir);
                git = Git.init().setDirectory(gitDir).call();
                git.pull().setStrategy(MergeStrategy.RESOLVE).call();
            }

            String lastCommitMessage = git.log().call().iterator().next().getShortMessage();
            log.infof("Git updated dir=%s commit_message='%s'", gitDir, lastCommitMessage);
        } catch (Exception e) {
            log.error("Error on git dir=" + gitDir.getAbsolutePath(), e);
            throw e;
        } finally {
            if (git != null) {
                git.close();
            }
        }


    }

}
