package io.websitecd.content.git.api.rest;

import io.websitecd.content.git.api.GitService;
import io.websitecd.content.git.api.model.GitInfo;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/websiteinfo")
@Produces(MediaType.APPLICATION_JSON)
public class WebsiteInfoResource {

    @Inject
    GitService gitService;

    @GET
    @Path("")
    public Map<String, GitInfo> info() throws IOException, GitAPIException {
        Map<String, GitInfo> result = new HashMap<>();
        List<String> dirs = gitService.list();
        for (String dir : dirs) {
            result.put(dir, gitService.info(dir));
        }
        return result;
    }

}
