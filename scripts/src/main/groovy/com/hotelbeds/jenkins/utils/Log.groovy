package com.hotelbeds.jenkins.utils

public class Log {

    private static final Log configure = new Log()

    private File file

    private Log() {
    }

    /**
     * Configure the file.
     *
     * @param file the file
     * @return the log
     */
    public static Log configure(File file) {
        file.getParentFile().mkdirs()
        configure.file = file
        configure.file.createNewFile()
        file.write('')
        return configure
    }

    /**
     * Info.
     *
     * @param message the message
     * @return the java.lang. object
     */
    def static info(message) {
        configure.file?.append("\nINFO: ${message}")
    }

    /**
     * Debug.
     *
     * @param message the message
     * @return the java.lang. object
     */
    def static debug(message) {
        configure.file?.append("\nDEBUG: ${message}")
    }

    /**
     * Error.
     *
     * @param message the message
     * @return the java.lang. object
     */
    def static error(message) {
        configure.file?.append("\nERROR: ${message}")
    }
}
