package com.example.issuesdownloader

import groovy.json.JsonSlurper

class GitHubDownloader {

    //GitHub credentials
    private static def USERNAME
    private static def PASSWORD

    private static def TITLE = "Reviews note,Title,Number,Created at,Reporter"

    private static def REPOS = [
            "sqlcipher/android-database-sqlcipher"           : null,
            "tony19/logback-android"                         : null,
            "google/gson"                                    : null,
            "square/okhttp"                                  : null,
            "google/dagger"                                  : null,
            "JakeWharton/RxRelay"                            : null,
            "ReactiveX/RxAndroid"                            : null,
            "ReactiveX/RxJava"                               : { obj -> obj.title.contains("2.x") || obj.labels.stream().anyMatch { it.name == "2.x" } },
//            "mmin18/RealtimeBlurView"                        : null,
//            "wasabeef/Blurry"                                : null,
//            "agrosner/DBFlow"                                : null,
//            "JodaOrg/joda-time"                              : null,
//            "swagger-api/swagger-codegen"                    : { obj -> obj.title.contains("java") },
//            "Adobe-Marketing-Cloud/mobile-services"          : null,
            "CAAPIM/Android-MAS-SDK"                         : null,
            "openid/AppAuth-Android"                         : null,
            "scottyab/rootbeer"                              : null,
            "material-components/material-components-android": null,
            "firebase/firebase-android-sdk": { obj -> obj.labels.stream().anyMatch { it.name == "api: analytics" } },
    ]

    def run(String folder) {
        REPOS.each { repo, filter ->
            loadIssues folder, repo, filter
        }
    }

    private def loadIssues(String folder, String repo, Closure filter) {
        def fileName = repo.split("/")[1]
        def file = new File(folder, "${fileName}.txt")
        file.parentFile.mkdirs()
        file.delete();
        file.createNewFile()
//        file.append "$TITLE\n"

        loadIssues file, repo, filter
    }

    private def loadIssues(File file, String repo, Closure filter, int page = 1) {
        println "$repo loading page:$page"
        def request = new URL("https://api.github.com/repos/$repo/issues?page=$page&per_page=100").openConnection() as HttpURLConnection
        if (USERNAME && PASSWORD) {
            // Will be removed on November 13, 2020 https://developer.github.com/v3/auth/#via-username-and-password
            if (System.currentTimeSeconds() >= 1605225600) {
                throw new UnsupportedOperationException("Password authentication to the API was removed on November 13, 2020")
            }
            def basic = new String(Base64.encoder.encode("$USERNAME:$PASSWORD".bytes))
            request.setRequestProperty "Authorization", "Basic $basic"
        }
        def responseCode = request.responseCode
        def success = false
        if (responseCode == 200) {
            def link = request.headerFields.Link ?: request.headerFields.link
            def hasNextPage = link?.get(0)?.contains("rel=\"next\"") ?: false
            parseAndAppendToFile file, request.inputStream.text, filter
            if (hasNextPage) loadIssues file, repo, filter, ++page
            success = true
            println "Success"
        } else {
            System.err.println "Failed"
            System.err.println "Message: ${request.responseMessage}"
        }
        return success
    }

    private def parseAndAppendToFile(File file, String jsonText, Closure filter) {
        def parser = new JsonSlurper()
        def json = parser.parseText jsonText
        json.each {
            if (it.pull_request != null) return // Ignore Pull Requests
            if (it.labels.size == 0
                    || it.labels.stream().anyMatch { it.name == "bug" }
                    || (filter && filter.call(it)) // Filter issues
            ) {
                def line = "$it.title" +
                        "\t$it.html_url" +
                        "\t$it.number" +
                        "\t$it.created_at" +
                        "\t$it.user.login"

//                def line = "," + wrap("=HYPERLINK(\"${it.html_url}\";\"${it.title}\")") +
//                        "," + wrap(it.number) +
//                        "," + wrap(it.created_at) +
//                        "," + wrap(it.user.login)
                file.append "$line\n"
            }

        }
    }

    private def wrap(Object str) {
        return "\"${String.valueOf(str).replaceAll("\"", "\"\"")}\""
    }

}
