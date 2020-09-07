package com.example.issuesdownloader

class Diff {

    private static def DIFFS_FOLDER = "build/generated/diffs"
    private static def MERGE_FOLDER = "build/generated/merge"
    private static def EXPORTS_FOLDER = "build/generated/exports"


    private static def COLUMNS = ['Review comment', 'Title', 'URL', 'Number', 'Created at', 'Reporter']

    private static def REPOS = [
            //reviewed issues, all issues, file name
            ["gson.csv", "gson.txt", "gson"],
//            ["logback-android.csv", "logback-android.txt", "logback-android"],
//            ["Android-MAS-SDK.csv", "Android-MAS-SDK.txt", "Android-MAS-SDK"],
//            ["rootbeer.csv", "rootbeer.txt", "rootbeer"],
//            ["DBFlow.csv", "DBFlow.txt", "DBFlow"],
//            ["AppAuth-Android.csv", "AppAuth-Android.txt", "AppAuth-Android"],
//            ["swagger-codegen.csv", "swagger-codegen.txt", "swagger-codegen"],
//            ["joda-time.csv", "joda-time.txt", "joda-time"],
//            ["RealtimeBlurView.csv", "RealtimeBlurView.txt", "RealtimeBlurView"],
//            ["Blurry.csv", "Blurry.txt", "Blurry"],
//            ["dagger.csv", "dagger.txt", "dagger"],
//            ["mobile-services.csv", "mobile-services.txt", "mobile-services"],
//            ["okhttp.csv", "okhttp.txt", "okhttp"],
//            ["android-database-sqlchipher.csv", "android-database-sqlcipher.txt", "android-database-sqlchipher"],
//            ["RxJava.csv", "RxJava.txt", "RxJava"],
//            ["slf4j.csv", "SLF4J.txt", "slf4j"]
    ]

    def mergeIssues(String issuesFolder) {
        REPOS.each {
            def reviewedIssues = getReviewedIssues(EXPORTS_FOLDER, it[0])
            def allIssues = getAllIssues(issuesFolder, it[1])

            allIssues.each { issue ->
                def reviewedIssue = reviewedIssues.find { reviewedIssue -> reviewedIssue.url == issue.url }
                if (reviewedIssue) {
                    issue.reviewComment = reviewedIssue.reviewComment
                }
            }

            saveDiffToCsv MERGE_FOLDER, "${it[2]}_merge", allIssues
        }
    }

    def generateDiffs(String issuesFolder) {
        REPOS.each {
            def reviewedIssues = getReviewedIssues(EXPORTS_FOLDER, it[0])
            def allIssues = getAllIssues(issuesFolder, it[1])

            def diffs = allIssues - reviewedIssues
            println it[2] + ":" + diffs.size()
            saveDiffToCsv DIFFS_FOLDER, "${it[2]}_diff", diffs
        }
    }

    private static Set<Model> getAllIssues(String folder, String fileName) {
        def issues = new HashSet<Model>()
        def file = new File(folder, fileName)

        if (!file.exists()) return issues
        file.splitEachLine(Model.DELIMITER) { fields ->
            issues.add(
                    new Model(
                            "",
                            fields[0],
                            fields[1],
                            fields[2],
                            fields[3],
                            fields[4],
                    )
            )
        }
        return issues.sort { it.createdAt }.reverse()
    }

    private static Set<Model> getReviewedIssues(String folder, String fileName) {
        def reviewedIssues = new HashSet<Model>()

        def file = new File(folder, fileName)
        if (!file.exists()) return reviewedIssues
        file.splitEachLine(Model.DELIMITER) { fields ->
            if (fields[2] != null) {
                reviewedIssues.add(
                        new Model(
                                fields[0],
                                fields[1],
                                fields[2],
                                fields[3],
                                fields[4],
                                fields[5]
                        )
                )
            }
        }
        return reviewedIssues
    }

    private static def saveDiffToCsv(String folder, String fileName, Set<Model> data) {
        def file = new File(folder, fileName + ".csv")
        file.parentFile.mkdirs()
        file.delete();
        file.createNewFile()
        data.eachWithIndex { Model it, int index ->
            if (index == data.size() - 1) {
                file.append "$it"
            } else {
                file.append "$it\n"
            }
        }
    }
}
