package com.example.issuesdownloader

class Main {

    private static def FOLDER = "build/generated/issues"

    static void main(String[] args) {
        def git = new GitHubDownloader()
        git.run(FOLDER)

//        def jira = new JiraDownloader()
//        jira.run(FOLDER)
    }
}
