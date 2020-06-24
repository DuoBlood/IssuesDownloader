package com.example.issuesdownloader

import groovy.json.JsonSlurper

class JiraDownloader {

    private static def REPOS = [
            ["https://issues.apache.org/jira", "LANG", null],
            ["https://issues.apache.org/jira", "IO", null]
    ]


    def run(String folder) {
        REPOS.each {
            loadIssues folder, it[0], it[1], it[2]
        }
    }

    private def loadIssues(String folder, String jira, String project, Closure filter) {
        def file = new File(folder, "${project}.txt")
        file.parentFile.mkdirs()
        file.delete();
        file.createNewFile()
        loadIssues file, jira, project, filter
    }

    private def loadIssues(File file, String jira, String project, Closure filter) {
        def request = new URL("$jira/rest/api/2/search").openConnection() as HttpURLConnection
        def body = "{\"jql\":\"project = $project AND issuetype = Bug AND status in (Open, \\\"In Progress\\\", Reopened) ORDER BY key DESC\",\"startAt\":0,\"maxResults\":1000,\"fields\":[\"id\",\"key\",\"summary\",\"created\",\"reporter\"]}"
        request.setRequestMethod("POST")
        request.setDoOutput(true)
        request.setRequestProperty("Content-Type", "application/json")
        request.getOutputStream().write(body.getBytes("UTF-8"));
        def responseCode = request.responseCode
        def success = false
        if (responseCode == 200) {
            parseAndAppendToFile file, jira, request.inputStream.text, filter
            println "Success"
            success = true
        } else {
            System.err.println "Failed"
            System.err.println "Message: ${request.errorMessages}"
        }
        return success
    }

    private def parseAndAppendToFile(File file, String jira, String jsonText, Closure filter) {
        def parser = new JsonSlurper()
        def json = parser.parseText jsonText
        json.issues.each {
            if (filter && !filter.call(it)) return // Filter issues
            def line = "$it.fields.summary" +
                    "\t$jira/browse/$it.key" +
                    "\t$it.key" +
                    "\t$it.fields.created" +
                    "\t$it.fields.reporter.name"
            file.append "$line\n"
        }
    }
}
