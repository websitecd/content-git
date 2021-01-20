package io.websitecd.content.git.api;

import io.quarkus.runtime.StartupEvent;
import io.websitecd.content.git.api.model.CommitInfo;
import io.websitecd.content.git.api.model.GitInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class GitService {

    Logger log = Logger.getLogger(GitService.class);

    @ConfigProperty(name = "app.data.dir")
    String dataDir;

    File dataDirFile;

    void onStart(@Observes StartupEvent ev) {
        dataDirFile = new File(dataDir);
        log.infof("content-git-api started. APP_DATA_DIR=%s exists=%s", dataDir, dataDirFile.exists());
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
        File gitDir = getGitDir(dir);

        Git git = Git.init().setDirectory(gitDir).call();
        git.pull();
        git.close();
    }

    public GitInfo info(String dir) throws IOException, GitAPIException {
        File gitDir = getGitDir(dir);
        Git git = Git.init().setDirectory(gitDir).call();

        GitInfo info = new GitInfo();
        info.setBranch(git.getRepository().getBranch());
        info.setUrl(git.remoteList().call().get(0).getURIs().get(0).toString());

        Iterable<RevCommit> log = git.log().call();
        RevCommit lastCommit = log.iterator().next();
        long timestamp = ((long) lastCommit.getCommitTime()) * 1000;
        info.setLastCommit(new CommitInfo(lastCommit.getFullMessage(), lastCommit.getAuthorIdent().getName(), new Date(timestamp)));

        git.close();
        return info;
    }

    protected File getGitDir(String dir) throws FileNotFoundException {
        File gitDir = new File(dataDirFile.getAbsolutePath() + "/" + dir);
        if (!gitDir.exists()) {
            throw new FileNotFoundException("dir " + gitDir.getAbsolutePath() + " does not exist");
        }
        return gitDir;
    }
}
