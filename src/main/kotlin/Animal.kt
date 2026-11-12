package org.example

class Animal(
    val id: Int,
    var species: String,
    var condition: String,
    var careRequirements: String,
    var rehabilitationStatus: String,
    var rehabilitationCaseReference: String? = null,
    var resourceRequirements: String = ""
)