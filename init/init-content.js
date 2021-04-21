const fs = require('fs').promises;
// const mvdir = require('mvdir');
const YAML = require('yaml');
const simpleGit = require('simple-git');

const configFilePath = process.env.CONFIG_PATH;
const targetDirPath = process.env.TARGET_DIR;

if (configFilePath === undefined) logAndAbort("Error: CONFIG_PATH env variable not defined");
if (targetDirPath === undefined) logAndAbort("Error: TARGET_DIR env variable not defined");

const sslNoVerify = process.env.GIT_SSL_NO_VERIFY || "false";
const sslVerify = !(sslNoVerify === "true");

console.log("Configuration CONFIG_PATH=%s TARGET_DIR=%s GIT_SSL_NO_VERIFY=%s", configFilePath, targetDirPath, sslNoVerify);

// Start the process
processConfigFile(configFilePath);

function processConfigFile(path) {
    console.log("Going to read config file path=%s sslVerify=%s", path, sslVerify);
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
            return fs.access(desiredDir)
                .then(() => {
                    console.log("Dir %s exists. SKIPPING !", desiredDir);
                })
                .catch(() => {
                    console.log("Creating dir %s", desiredDir);
                    return fs.mkdir(desiredDir, { recursive: true })
                        .then(() => gitCloneSubdirPromise(spec.get('url'), desiredDir, spec.get('ref'), spec.get('dir')))
                        .catch(reason => {
                            console.error("Error cloning git repo. Going to delete dir.", reason);
                            return fs.rmdir(desiredDir, { recursive: true })
                                .then(() => {
                                    console.log("dir %s DELETED because of error", desiredDir)
                                    throw new Error("Error cloning git repo. Dir deleted.");
                                });
                        });
                });
        }
    });

    // Process all actions
    Promise.all(gitPromises)
        .then(() => console.log("SUCCESS. All git repos cloned"))
        .catch(reason => logAndAbort("Error!", reason));
}

function gitCloneSubdirPromise(gitPath, localPath, ref, subDir) {
    let git = simpleGit({
        baseDir: localPath,
        binary: 'git',
        maxConcurrentProcesses: 6,
    });
    return git.clone(gitPath, localPath)
        .then(() => git.addConfig('pull.rebase', 'true'))
        .then(() => git.addConfig('pull.rebase', 'true'))
        .then(() => git.addConfig('http.sslVerify', '' + sslVerify))
        .then(() => git.checkout(ref))
        .then(() => console.log("git-clone DONE git=%s ref=%s subDir=%s toDir=%s", gitPath, ref, subDir, localPath));
}

function logAndAbort(message, reason) {
    console.error(message + ((reason) ? " reason: %s" + reason : ""));
    process.abort();
}
