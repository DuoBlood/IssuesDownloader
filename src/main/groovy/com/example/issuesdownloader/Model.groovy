package com.example.issuesdownloader

class Model {

    public static def DELIMITER = "\t"

    String reviewComment
    final String title
    final String url
    final String issueId
    final String createdAt
    final String reporter

    Model(String reviewComment, String title, String url, String issueId, String createdAt, String reporter) {
        this.reviewComment = reviewComment
        this.title = title
        this.url = url
        this.issueId = issueId
        this.createdAt = createdAt
        this.reporter = reporter
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Model model = (Model) o

        if (issueId != model.issueId) return false
        if (url != model.url) return false

        return true
    }

    int hashCode() {
        int result
        result = url.hashCode()
        result = 31 * result + issueId.hashCode()
        return result
    }


    @Override
    public String toString() {
        return "$reviewComment$DELIMITER$title$DELIMITER$url$DELIMITER$issueId$DELIMITER$createdAt$DELIMITER$reporter"
    }
}
