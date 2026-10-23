
package org.example

fun main() {

    // Create an animal
    val animal = Animal(
        id = 1,
        species = "Fox",
        condition = "Injured",
        careRequirements = "Medication and rest",
        rehabilitationStatus = "Under Treatment"
    )

    // Display animal information
    println("Animal ID: ${animal.id}")
    println("Species: ${animal.species}")
    println("Condition: ${animal.condition}")
    println("Care Requirements: ${animal.careRequirements}")
    println("Rehabilitation Status: ${animal.rehabilitationStatus}")

    // Create a rescue case
    val rescueCase = RescueCase(
        caseReference = "RC001",
        rescueDate = "22/10/2026",
        location = "London",
        reportedCondition = "Injured",
        status = "Open"
    )

    rescueCase.animals.add(animal)

    // Display rescue case information
    println("Case Reference: ${rescueCase.caseReference}")
    println("Rescue Date: ${rescueCase.rescueDate}")
    println("Location: ${rescueCase.location}")
    println("Case Status: ${rescueCase.status}")
    println("Animals in Case: ${rescueCase.animals.size}")

    // Create a rehabilitation facility
    val facility = Facility(
        facilityId = 101,
        name = "Wildlife Rehabilitation Centre",
        location = "London",
        capacity = 10,
        suitableSpecies = listOf("Fox", "Hedgehog"),
        available = true,
        availableFacilities = listOf(
            "Treatment Room",
            "Quarantine Area"
        )
    )

    // Display facility information
    println("Facility ID: ${facility.facilityId}")
    println("Facility Name: ${facility.name}")
    println("Location: ${facility.location}")
    println("Capacity: ${facility.capacity}")
    println("Suitable Species: ${facility.suitableSpecies}")
    println("Available: ${facility.available}")
    println("Facilities: ${facility.availableFacilities}")

    // Create equipment
    val equipment = Equipment(
        equipmentId = 201,
        equipmentType = "Transport Cage",
        quantity = 5,
        location = "London",
        available = true
    )

    // Display equipment information
    println("Equipment ID: ${equipment.equipmentId}")
    println("Equipment Type: ${equipment.equipmentType}")
    println("Quantity: ${equipment.quantity}")
    println("Location: ${equipment.location}")
    println("Available: ${equipment.available}")

    // Create a staff member
    val staff = Staff(
        staffId = 301,
        name = "Sarah Johnson",
        role = "Veterinarian",
        qualifications = listOf(
            "Wildlife Care",
            "Animal First Aid"
        ),
        available = true
    )

    // Display staff information
    println("Staff ID: ${staff.staffId}")
    println("Staff Name: ${staff.name}")
    println("Role: ${staff.role}")
    println("Qualifications: ${staff.qualifications}")
    println("Available: ${staff.available}")

    // Allocate an animal to a facility
    val allocation = Allocation(
        allocationId = 401,
        animal = animal,
        facility = facility,
        allocationDate = "23/10/2026",
        active = true
    )

    // Display allocation information
    println("Allocation ID: ${allocation.allocationId}")
    println("Animal Species: ${allocation.animal.species}")
    println("Facility Name: ${allocation.facility.name}")
    println("Allocation Date: ${allocation.allocationDate}")
    println("Active: ${allocation.active}")

    // Test facility capacity
    println("\n--- Facility Capacity Test ---")

    println("Current Animals: ${facility.currentAnimals}")
    println("Has Available Space: ${facility.hasAvailableSpace()}")

    facility.currentAnimals = 10

    println("Current Animals After Update: ${facility.currentAnimals}")
    println("Has Available Space After Update: ${facility.hasAvailableSpace()}")

    // Test allocation when facility is full
    println("\n--- Full Capacity Allocation Test ---")

    try {
        val fullCapacityAllocation = Allocation(
            allocationId = 402,
            animal = animal,
            facility = facility,
            allocationDate = "23/10/2026",
            active = true
        )

        println("Allocation successful: ${fullCapacityAllocation.allocationId}")

    } catch (e: IllegalArgumentException) {
        println("Allocation failed: ${e.message}")
    }

    // Test allocation cancellation
    println("\n--- Allocation Cancellation Test ---")

    // Reset capacity for testing
    facility.currentAnimals = 1

    println("Before Cancellation: ${facility.currentAnimals}")

    allocation.cancelAllocation()

    println("After Cancellation: ${facility.currentAnimals}")
    println("Allocation Active: ${allocation.active}")

    // Test cancelling the same allocation again
    allocation.cancelAllocation()

    println("Final Animal Count: ${facility.currentAnimals}")
}

