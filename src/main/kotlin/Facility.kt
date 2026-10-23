package org.example

class Facility(
    val facilityId: Int,
    val name: String,
    val location: String,
    val capacity: Int,
    val suitableSpecies: List<String>,
    var available: Boolean,
    val availableFacilities: List<String>

    ) {
    init {
        require(capacity > 0) {
            "Facility capacity must be greater than zero"
        }
    }

    var currentAnimals: Int = 0

    fun hasAvailableSpace(): Boolean {
        return available && currentAnimals < capacity
    }
}


