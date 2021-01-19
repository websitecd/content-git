package io.websitecd.content.git.init;

import io.vertx.core.AbstractVerticle;
import io.websitecd.content.git.config.model.GitComponent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.URIish;
import org.jboss.logging.Logger;

import java.io.File;

public class GitCloneVerticle extends AbstractVerticle {

    Logger log = Logger.getLogger(GitCloneVerticle.class);

    GitComponent component;

    String targetDir;

    boolean sslVerify;

    public GitCloneVerticle(GitComponent component, String targetDir, boolean sslVerify) {
        this.component = component;
        this.targetDir = targetDir;
        this.sslVerify = sslVerify;
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
            git = Git.init().setDirectory(gitDir).call();
            if (!gitDirExists) {
                log.infof("Directory %s not exists. Configuring git", gitDir);
                git.remoteAdd().setName("origin").setUri(new URIish(gitUrl)).call();
                StoredConfig config = git.getRepository().getConfig();
                config.setBoolean("http", null, "sslVerify", sslVerify);
                config.save();
            } else {
                log.infof("Directory %s exists. Just pull", gitDir);
            }
            git.pull().setRemote("origin").setRemoteBranchName(ref).setStrategy(MergeStrategy.RESOLVE).call();


            String lastCommitMessage = git.log().call().iterator().next().getShortMessage();
            log.infof("Git cloned to dir=%s commit_message='%s'", gitDir, lastCommitMessage);
        } catch (Exception e) {
            log.errorf("Error on git dir=%s", gitDir.getAbsolutePath(), e);
            throw e;
        } finally {
            if (git != null) {
                git.close();
            }
        }


    }

}
