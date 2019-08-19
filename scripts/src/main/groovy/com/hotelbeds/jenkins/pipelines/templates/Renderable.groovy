package com.hotelbeds.jenkins.pipelines.templates


interface Renderable {

    def render()

    File toFile()

    File toFile(File container)

}
