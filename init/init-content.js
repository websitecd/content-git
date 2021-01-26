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
            const desiredDir = targetDirPath + "/" + componentDir;
            // TODO: Redesign the git clone process. It should git clone to another directory and rsync to target dir and delete the previous "version" within rsync
            fs.access(desiredDir)
                .then(() => {
                    console.log("Dir %s exists. SKIPPING !", desiredDir);
                })
                .catch(() => {
                    console.log("Creating dir %s", desiredDir);
                    return fs.mkdir(desiredDir)
                        .then(() => gitCloneSubdirPromise(spec.get('url'), desiredDir, spec.get('ref'), spec.get('dir')));
                });
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
    return git.clone(gitPath, localPath)
        .then(() => git.addConfig('pull.rebase', 'true'))
        .then(() => git.checkout(ref))
        .then(() => console.log("git-clone DONE git=%s ref=%s subDir=%s toDir=%s", gitPath, ref, subDir, localPath));
}

function logAndAbort(message, reason) {
    console.error(message + ((reason) ? " reason: %s" + reason : ""));
    process.abort();
}
