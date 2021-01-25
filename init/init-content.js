const fs = require('fs').promises;
// const mvdir = require('mvdir');
const YAML = require('yaml');
const simpleGit = require('simple-git');

const configFilePath = process.env.CONFIG_PATH;
const targetDirPath = process.env.TARGET_DIR;

if (configFilePath === undefined) logAndAbort("Error: CONFIG_PATH env variable not defined");
if (targetDirPath === undefined) logAndAbort("Error: TARGET_DIR env variable not defined");

// Start the process
processConfigFile(configFilePath);

function processConfigFile(path) {
    console.log("Going to read config file %s", path);
    fs.readFile(path, 'utf8')
        .then(file => YAML.parseDocument(file, {schema: 'core'}))
        .then(yaml => processConfig(yaml))
        .catch(reason => logAndAbort("Cannot read config file", reason));
}

function processConfig(config) {
    let components = config.get('components');

    let gitPromises = components.items.map((c) => {
        let componentDir = c.get('dir');
        let kind = c.get('kind');
        let spec = c.get('spec');
        if (kind === 'git') {
            const desiredDir = targetDirPath + "/" + componentDir
            return fs.mkdir(desiredDir)
                .then(() => gitCloneSubdirPromise(spec.get('url'), desiredDir, spec.get('ref'), spec.get('dir')));
        }
    });

    // Process all actions
    Promise.all(gitPromises)
        .then(() => console.log("All git repos cloned"))
        .catch(reason => logAndAbort("Error cloning git", reason));
}

function gitCloneSubdirPromise(gitPath, localPath, ref, subDir) {
    let git = simpleGit({
        baseDir: localPath,
        binary: 'git',
        maxConcurrentProcesses: 6,
    });
    // TODO: If directory exists. perform just git pull

    // NOTE: The spare checkout is needed because of abaility to checkout just a subdir
    // equivalent to:

    // git init <repo>
    // cd <repo>
    // git remote add origin <url>
    // git config core.sparsecheckout true
    // echo "DIR/*" >> .git/info/sparse-checkout
    // git pull --depth=1 origin master

    return git.init()
        .then(() => console.log("git-clone START git=%s ref=%s subDir=%s toDir=%s", gitPath, ref, subDir, localPath))
        .then(() => git.addRemote('origin', gitPath))
        .then(() => git.addConfig('core.sparsecheckout', 'true'))
        .then(() => git.addConfig('pull.rebase', 'true'))
        .then(() => git.addConfig('http.sslVerify', 'false'))
        // subdir is no more available. cloning always whole repo
        // .then(() => fs.writeFile(localPath + '/.git/info/sparse-checkout', subDir + '*'))
        .then(() => git.pull('origin', ref, {'--depth': '1'}))
        .then(() => git.branch({'--set-upstream-to': 'origin/' + ref}))
        .then(() => console.log("git-clone DONE dir=%s", localPath))
        // Don't move anything. Let's httpd to be correctly configured
        // .then(() => {
        //     if (subDir !== "/") {
        //         return mvdir(localPath + subDir, localPath)
        //             .then(() => console.log(`Sub-dir ${subDir} moved to ${localPath}`));
        //     }
        // })
        ;
}

function logAndAbort(message, reason) {
    console.error(message + ((reason) ? " reason: %s" + reason : ""));
    process.abort();
}
