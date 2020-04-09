String getDiscordMessage() {
    def msg = "**Status:** " + currentBuild.currentResult.toLowerCase() + "\n**Branch:** [${BRANCH_NAME}](https://github.com/ValkyrienSkies/Valkyrien-Skies/tree${BRANCH_NAME})\n**Changes:**\n"
    if (!currentBuild.changeSets.isEmpty()) {
        currentBuild.changeSets.first().getLogs().each {
            msg += "- `" + it.getCommitId().substring(0, 8) + "` *" + it.getComment().substring(0, Math.min(64, it.getComment().length() - 1)) + (it.getComment().length() - 1 > 64 ? "..." : "") + "*\n"
        }
    } else {
        msg += "- no changes\n"
    }

    msg += "\n**Artifacts:**\n"
    currentBuild.rawBuild.getArtifacts().each {
        msg += "- [" + it.getDisplayPath() + "](" + env.BUILD_URL + "artifact/" + it.getHref() + ")\n"
    }

    return msg.length() > 2048 ? msg.substring(0, 2045) + "..." : msg
}

void renameJars()   {
    def commitHash = sh(script: "git log -n 1 --pretty=format:'%H'", returnStdout: true).substring(0, 8)
    def branchName = "${BRANCH_NAME}".replaceAll("[^a-zA-Z0-9.]", "_")

    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^((?:[^-]*?-){2}[^-]*?)((?:-sources)?\\.jar)\$").matcher("")
    File dir = new File("build/libs/")
    for (File file : dir.listFiles())   {
        if (matcher.reset(file.name).find())   {
            file.renameTo(new File(dir, String.format("%s-%s-%s%s", matcher.group(1), commitHash, branchName, matcher.group(2))))
        }
    }
}

pipeline {
    agent any
    tools {
        git "Default"
        jdk "jdk8"
    }

    stages {
        stage("Prepare Workspace")  {
            steps {
                sh "./gradlew clean setupDecompWorkspace --no-daemon"
            }
        }
        stage("Build") {
            steps {
                sh "./gradlew build --no-daemon"
            }
            post {
                success {
                    renameJars()
                    archiveArtifacts artifacts: "build/libs/*.jar", fingerprint: true
                    junit "build/test-results/**/*.xml"
                }
            }
        }
        stage("Deploy") {
            when {
                branch "master"
            }
            steps {
                sh "./gradlew publishToMavenLocal --no-daemon"
            }
        }
    }

    post {
        always {
            deleteDir()

            withCredentials([string(credentialsId: "discord_webhook", variable: "discordWebhook")]) {
                discordSend thumbnail: "https://static.miraheze.org/valkyrienskieswiki/6/63/Logo_128.png",
                        result: currentBuild.currentResult,
                        description: getDiscordMessage(),
                        link: env.BUILD_URL,
                        title: "Valkyrien Skies:${BRANCH_NAME} #${BUILD_NUMBER}",
                        webhookURL: "${discordWebhook}"
            }
        }
    }
}
