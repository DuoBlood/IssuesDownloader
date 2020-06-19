package com.example.issuesdownloader

import groovy.json.JsonSlurper

class Main {

    //GitHub credentials
    private static def USERNAME
    private static def PASSWORD

    private static def FOLDER = "build/generated/issues"
    private static def TITLE = "Reviews note,Title,Number,Created at,Reporter"

    private static def REPOS = [
            "agrosner/DBFlow",
            "openid/AppAuth-Android",
            "swagger-api/swagger-codegen",
            "JodaOrg/joda-time",
            "wasabeef/Blurry",
            "google/dagger",
            "square/okhttp",
            "sqlcipher/android-database-sqlcipher",
            "apache/commons-lang"
    ]

    static void main(String[] args) {
        REPOS.each {
            loadIssues it
        }
    }

    private static def loadIssues(String repo) {
        def fileName = repo.split("/")[1]
        def file = new File(FOLDER, "${fileName}.csv")
        file.parentFile.mkdirs()
        file.delete();
        file.createNewFile()
        file.append "$TITLE\n"

        loadIssues file, repo
    }

    private static def loadIssues(File file, String repo, int page = 1) {
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
            parseAndAppendToFile file, request.inputStream.text
            if (hasNextPage) loadIssues file, repo, ++page
            success = true
            println "Success"
        } else {
            System.err.println "Failed"
            System.err.println "Message: ${request.responseMessage}"
        }
        return success
    }

    private static def parseAndAppendToFile(File file, String jsonText) {
        def parser = new JsonSlurper()
        def json = parser.parseText jsonText
        json.each {
            if (it.pull_request != null) return // Ignore Pull Requests
            def line = "," + wrap("=HYPERLINK(\"${it.html_url}\";\"${it.title}\")") +
                    "," + wrap(it.number) +
                    "," + wrap(it.created_at) +
                    "," + wrap(it.user.login)
            file.append "$line\n"

        }
    }

    private static def wrap(Object str) {
        return "\"${String.valueOf(str).replaceAll("\"", "\"\"")}\""
    }
}