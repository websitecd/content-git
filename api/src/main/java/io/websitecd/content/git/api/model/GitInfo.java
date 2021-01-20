package io.websitecd.content.git.api.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class GitInfo {
    String url;
    String branch;
    CommitInfo lastCommit;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public CommitInfo getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(CommitInfo lastCommit) {
        this.lastCommit = lastCommit;
    }
}
