const fs = require('fs').promises;
// const mvdir = require('mvdir');
const YAML = require('yaml');
const simpleGit = require('simple-git');

const configFilePath = process.env.CONFIG_PATH;
const targetDirPath = process.env.TARGET_DIR;
const deleteDirIfExist = process.env.DELETE_DIR_IF_EXIST || "true";

if (configFilePath === undefined) logAndAbort("Error: CONFIG_PATH env variable not defined");
if (targetDirPath === undefined) logAndAbort("Error: TARGET_DIR env variable not defined");

const sslNoVerify = process.env.GIT_SSL_NO_VERIFY || "false";
const sslVerify = !(sslNoVerify === "true");

console.log("Configuration CONFIG_PATH=%s TARGET_DIR=%s GIT_SSL_NO_VERIFY=%s DELETE_DIR_IF_EXIST=%s",
    configFilePath, targetDirPath, sslNoVerify, deleteDirIfExist);

// Start the process
processConfigFile(configFilePath);

function processConfigFile(path) {
    console.log("START"); // just empty line
    console.log("Going to read config file path=%s sslVerify=%s", path, sslVerify);
    fs.readFile(path, 'utf8')
        .then(file => YAML.parseDocument(file, {schema: 'core'}))
        .then(yaml => yaml.get('components'))
        .then(components => {
            console.log("components count=%s", components.items.length);
            if (deleteDirIfExist === "true") {
                console.log("CHECK if directory exists and delete them.");
                deleteDirsIfExists(components)
                    .then(() => processComponents(components))
                    .catch(reason => logAndAbort("Cannot delete existing dirs", reason));
            } else {
                processComponents(components)
            }
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
            return fs.access(desiredDir)
                .then(() => {
                    console.log("Dir %s exists. SKIPPING !", desiredDir);
                })
                .catch(() => {
                    console.log("Creating dir %s", desiredDir);
                    return fs.mkdir(desiredDir, {recursive: true})
                        .then(() => gitCloneSubdirPromise(url, desiredDir, ref, dir))
                        .catch(reason => {
                            console.error("Error cloning git repo. Going to delete dir.", reason);
                            return fs.rmdir(desiredDir, {recursive: true})
                                .then(() => {
                                    console.log("dir %s DELETED because of error", desiredDir)
                                    throw new Error("Error cloning git repo. Dir deleted.");
                                });
                        });
                });
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

function deleteDirsIfExists(components) {
    return Promise.all(components.items
        .filter(c => isKindGit(c))
        .map(c => componentDir(c))
        .map(dirname =>
            fs.access(dirname)
                .then(() => fs.rmdir(dirname, {recursive: true}).then(() => console.log("dir %s DELETED", dirname))
                ).catch(() => console.log("Directory %s not exists", dirname))
        ));
}

function isKindGit(c) {
    return c.get('kind') === 'git';
}

function componentDir(c) {
    return targetDirPath + "/" + c.get('dir');
}

function checkDirNotEmpty(dirname) {
    return isDirEmpty(dirname)
        .then(isEmpty => {
            if (isEmpty) {
                throw new Error("Dir is empty dir=" + dirname)
            } else {
                return isEmpty;
            }
        });
}

function isDirEmpty(dirname) {
    return fs.readdir(dirname)
        .then(files => files.length === 0);
}

function logAndAbort(message, reason) {
    console.error(message + ((reason) ? " reason: %s" + reason : ""));
    process.abort();
}
