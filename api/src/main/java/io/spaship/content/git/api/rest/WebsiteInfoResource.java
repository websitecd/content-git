package io.spaship.content.git.api.rest;

import io.spaship.content.git.api.GitService;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/websiteinfo")
@Produces(MediaType.APPLICATION_JSON)
public class WebsiteInfoResource {

    @Inject
    GitService gitService;


}
