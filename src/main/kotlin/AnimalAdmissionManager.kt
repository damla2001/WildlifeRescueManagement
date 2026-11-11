
package org.example

class AnimalAdmissionManager {

    private val registeredAnimals = mutableMapOf<Int, Animal>()
    private val rescueCases = mutableMapOf<String, RescueCase>()
    private val allocations = mutableMapOf<Int, Allocation>()

    // Register an animal, ensuring its ID is unique
    fun registerAnimal(animal: Animal) {
        require(animal.id !in registeredAnimals) {
            "An animal with this ID is already registered"
        }

        registeredAnimals[animal.id] = animal
        println("Animal registered successfully")
    }

    // Register a rescue case
    fun registerRescueCase(rescueCase: RescueCase) {
        require(rescueCase.caseReference !in rescueCases) {
            "Rescue case already exists"
        }

        rescueCases[rescueCase.caseReference] = rescueCase
    }

    // Link a registered animal to a rescue case
    fun linkAnimalToRescueCase(
        animalId: Int,
        caseReference: String
    ) {
        val animal = registeredAnimals[animalId]
            ?: throw IllegalArgumentException("Animal not found")

        val rescueCase = rescueCases[caseReference]
            ?: throw IllegalArgumentException("Rescue case not found")

        require(rescueCase.animals.none { it.id == animalId }) {
            "Animal is already linked to this rescue case"
        }

        rescueCase.addAnimal(animal)
        println("Animal linked to rescue case successfully")
    }

    // Allocate an animal to a suitable facility
    fun allocateAnimal(
        allocationId: Int,
        animalId: Int,
        facility: Facility,
        allocationDate: String
    ): Allocation {
        val animal = registeredAnimals[animalId]
            ?: throw IllegalArgumentException("Animal not found")

        require(allocationId !in allocations) {
            "Allocation ID already exists"
        }

        require(allocations.values.none {
            it.active && it.animal.id == animalId
        }) {
            "Animal already has an active allocation"
        }

        val allocation = Allocation(
            allocationId,
            animal,
            facility,
            allocationDate,
            true
        )

        allocations[allocationId] = allocation
        println("Animal allocated successfully")

        return allocation
    }

    // Cancel an existing allocation
    fun cancelAllocation(allocationId: Int) {
        val allocation = allocations[allocationId]
            ?: throw IllegalArgumentException("Allocation not found")

        allocation.cancelAllocation()
    }

    // Get all registered animals
    fun getRegisteredAnimals(): List<Animal> {
        return registeredAnimals.values.toList()
    }

    // Get all allocations
    fun getAllocations(): List<Allocation> {
        return allocations.values.toList()
    }

    // Update the rehabilitation status of a registered animal
    fun updateRehabilitationStatus(
        animalId: Int,
        newStatus: String
    ) {
        val animal = registeredAnimals[animalId]
            ?: throw IllegalArgumentException("Animal not found")

        require(newStatus.isNotBlank()) {
            "Rehabilitation status cannot be empty"
        }

        animal.rehabilitationStatus = newStatus.trim()
    }
}
