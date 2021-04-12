package io.spaship.content.git.api.rest;

import io.spaship.content.git.api.GitService;
import io.spaship.content.git.api.model.GitInfo;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return "UPDATE SUCCESSFUL result=" + gitService.updateGit(dir);
    }

    @GET
    @Path("info/{dir}")
    public GitInfo info(@PathParam("dir") String dir) throws IOException, GitAPIException {
        if (info.containsKey(dir)) {
            return info.get(dir);
        }
        try {
            GitInfo actualInfo = gitService.info(dir);
            info.put(dir, actualInfo);
            return actualInfo;
        } catch (FileNotFoundException e) {
            throw new NotFoundException("not exits. dir=" + dir);
        }
    }

    Map<String, GitInfo> info = new HashMap<>();

    public void clearInfo(String dir) {
        if (info.containsKey(dir)) {
            info.remove(dir);
        }
    }

}
