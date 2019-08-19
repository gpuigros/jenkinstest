package com.hotelbeds.jenkins.pipelines.jobs


enum JobStatus {

    ALWAYS,
    FAILED,
    FAILED_OR_BETTER,
    SUCCESS,
    UNSTABLE,
    UNSTABLE_OR_BETTER,
    UNSTABLE_OR_WORSE

}
