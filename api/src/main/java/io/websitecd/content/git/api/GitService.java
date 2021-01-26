package io.websitecd.content.git.api;

import io.quarkus.runtime.StartupEvent;
import io.websitecd.content.git.api.model.CommitInfo;
import io.websitecd.content.git.api.model.GitInfo;
import io.websitecd.content.git.api.rest.WebsiteInfoResource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
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

    String dataDirPath;

    @Inject
    WebsiteInfoResource websiteInfoResource;

    void onStart(@Observes StartupEvent ev) {
        File dataDirFile = new File(dataDir);
        dataDirPath = dataDirFile.getAbsolutePath();
        log.infof("content-git-api started. APP_DATA_DIR=%s exists=%s", dataDirPath, dataDirFile.exists());
    }

    public List<String> list() {
        log.info("get list");

        File file = new File(dataDir);
        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());

        if (directories == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(directories);
    }

    public String updateGit(String dir) throws FileNotFoundException, GitAPIException {
        log.infof("Update git dir=%s", dir);
        File gitDir = getGitDir(dir);

        Git git = Git.init().setDirectory(gitDir).call();

        PullResult result;
        try {
            result = git.pull().call();
        } catch (NoHeadException e) {
            log.infof("Update skipped. Not on HEAD");
            return "SKIP-NO-HEAD";
        }
        String resultStr = toString(result);
        if (!result.isSuccessful()) {
            throw new RuntimeException("Cannot perform git pull. reason=%s" + resultStr);
        }
        git.close();
        log.infof("Update Success. result=%s", resultStr);

        websiteInfoResource.clearInfo();
        return resultStr;
    }

    public String toString(PullResult result) {
        StringBuilder sb = new StringBuilder();
        if (result.getFetchResult() != null) {
            sb.append(result.getFetchResult().submoduleResults());
        } else {
            sb.append("No fetch result");
        }
        sb.append(" ");
        if (result.getMergeResult() != null) {
            sb.append(result.getMergeResult().getMergeStatus());
        } else if (result.getRebaseResult() != null) {
            sb.append("Rebase:");
            sb.append(result.getRebaseResult().getStatus());
        } else
            sb.append("No update result");
        return sb.toString();
    }

    public GitInfo info(String dir) throws IOException, GitAPIException {
        log.infof("get info dir=%s", dir);

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
        File gitDir = new File(dataDirPath + "/" + dir);
        if (!gitDir.exists()) {
            throw new FileNotFoundException("dir " + gitDir.getAbsolutePath() + " does not exist");
        }
        return gitDir;
    }
}
