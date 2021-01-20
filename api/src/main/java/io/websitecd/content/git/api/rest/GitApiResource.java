package io.websitecd.content.git.api.rest;

import io.websitecd.content.git.api.GitService;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.util.List;

@Path("/api")
public class GitApiResource {

    @Inject
    GitService gitService;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> list() {
        return gitService.list();
    }

    @GET
    @Path("update/{dir}")
    public String list(@PathParam("dir") String dir) throws FileNotFoundException, GitAPIException {
        gitService.updateGit(dir);
        return "DONE";
    }


}
