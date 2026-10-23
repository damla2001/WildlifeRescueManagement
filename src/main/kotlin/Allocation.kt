package org.example

class Allocation(
    val allocationId: Int,
    val animal: Animal,
    val facility: Facility,
    val allocationDate: String,
    var active: Boolean
) {
    init {
        // Check if the facility is suitable for the animal
        require(animal.species in facility.suitableSpecies) {
            "Facility is not suitable for this animal"
        }

        // Check if the facility has available capacity
        require(facility.hasAvailableSpace()) {
            "Facility has no available capacity"
        }

        // Automatically increase the number of animals
        if (active) {
            facility.currentAnimals++
        }
    }

    // Cancel an existing allocation
    fun cancelAllocation() {
        if (active) {
            active = false
            facility.currentAnimals--
            println("Allocation cancelled successfully")
        } else {
            println("Allocation is already inactive")
        }
    }
}