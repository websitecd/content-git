const fs = require('fs').promises;
// const mvdir = require('mvdir');
const YAML = require('yaml');
const simpleGit = require('simple-git');

const configFilePath = process.env.CONFIG_PATH;
const targetDirPath = process.env.TARGET_DIR;
const updateDirIfExist = process.env.UPDATE_IF_DIR_EXIST || "true";

if (configFilePath === undefined) logAndAbort("Error: CONFIG_PATH env variable not defined");
if (targetDirPath === undefined) logAndAbort("Error: TARGET_DIR env variable not defined");

const sslNoVerify = process.env.GIT_SSL_NO_VERIFY || "false";
const sslVerify = !(sslNoVerify === "true");

console.log("[main] Configs CONFIG_PATH=%s TARGET_DIR=%s GIT_SSL_NO_VERIFY=%s UPDATE_IF_DIR_EXIST=%s",
    configFilePath, targetDirPath, sslNoVerify, updateDirIfExist);

// Start the process
processConfigFile(configFilePath);

function processConfigFile(path) {
    console.log("[main] START"); // just empty line
    console.log("[main] Going to read config file path=%s sslVerify=%s", path, sslVerify);
    fs.readFile(path, 'utf8')
        .then(file => YAML.parseDocument(file, {schema: 'core'}))
        .then(yaml => yaml.get('components'))
        .then(components => {
            console.log("[main] components_count=%s", components.items.length);
            processComponents(components)
        })
        .catch(reason => logAndAbort("Cannot read config file", reason));
}

function processComponents(components) {
    let gitPromises = components.items
        .filter(c => isKindGit(c))
        .map(c => {
            const spec = c.get('spec');
            const url = spec.get('url');
            const ref = spec.get('ref');
            const dir = spec.get('dir');
            const desiredDir = componentDir(c);
            const desiredDirGit = desiredDir + '/.git';
            let start = new Date();
            return fs.access(desiredDirGit)
                .then(() => {
                    if (updateDirIfExist === 'true') {
                        console.log("[check] EXISTS path=%s", desiredDirGit);
                        console.log("[git-pull] START path=%s", desiredDir);
                        return gitPullSubdirPromise(desiredDir, ref)
                            .then((result) => console.log("[git-pull] DONE path=%s time=%sms result=%s", desiredDir, (new Date() - start), result.summary))
                            .catch(reason => logAndAbort("[git-pull] ERROR path=" + desiredDir, reason));
                    } else {
                        console.log("[check] path=%s exists. No action performed.", desiredDirGit);
                    }
                })
                .catch(() => {
                    console.log("[check] NOT_EXISTS path=%s", desiredDirGit);
                    console.log("[git-clone] START path=%s", desiredDir);
                    return fs.mkdir(desiredDir, {recursive: true})
                        .then(() => gitCloneSubdirPromise(url, desiredDir, ref, dir))
                        .then(() => console.log("[git-clone] DONE path=%s git=%s ref=%s subDir=%s time=%sms", desiredDir, url, ref, dir, (new Date() - start)))
                        .catch(reason => {
                            console.error("[git-clone] ERROR path=%s Going to delete dir.", reason);
                            return fs.rmdir(desiredDir, {recursive: true})
                                .then(() => {
                                    console.log("[git-clone] DELETED path=%s because of error", desiredDir)
                                    throw new Error("Error cloning git repo. Dir deleted.");
                                });
                        });
                });
        });

    // Process all actions
    Promise.all(gitPromises)
        .then(() => console.log("[main] DONE All git repos cloned/updated"))
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
        .then(() => git.checkout(ref));
}

function gitPullSubdirPromise(localPath, ref) {
    let git = simpleGit({
        baseDir: localPath,
        binary: 'git',
        maxConcurrentProcesses: 6,
    });
    return git.pull('origin', ref);
}

function isKindGit(c) {
    return c.get('kind') === 'git';
}

function componentDir(c) {
    return targetDirPath + "/" + c.get('dir');
}

function logAndAbort(message, reason) {
    console.error(message + ((reason) ? " Reason: " + reason : ""));
    process.abort();
}
