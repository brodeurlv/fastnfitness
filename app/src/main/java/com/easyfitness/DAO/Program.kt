package com.easyfitness.DAO

class Program(programName: String?, private val profileId: Long) : ARecord() {
    var programName: String? = null

    init {
        this.programName = programName
    }
}
