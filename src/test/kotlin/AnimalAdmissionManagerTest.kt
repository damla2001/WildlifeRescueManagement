
package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnimalAdmissionManagerTest {

    @Test
    fun testAnimalRegistration() {
        val manager = AnimalAdmissionManager()

        val animal = Animal(
            id = 1,
            species = "Fox",
            condition = "Injured",
            careRequirements = "Medical treatment",
            rehabilitationStatus = "In progress"
        )

        manager.registerAnimal(animal)

        assertEquals(1, manager.getRegisteredAnimals().size)
    }

    @Test
    fun testDuplicateAnimalRegistration() {
        val manager = AnimalAdmissionManager()

        val animal = Animal(
            id = 1,
            species = "Fox",
            condition = "Injured",
            careRequirements = "Medical treatment",
            rehabilitationStatus = "In progress"
        )

        manager.registerAnimal(animal)

        assertFailsWith<IllegalArgumentException> {
            manager.registerAnimal(animal)
        }
    }

    @Test
    fun testAnimalRescueCaseLinking() {
        val manager = AnimalAdmissionManager()

        val animal = Animal(
            id = 2,
            species = "Deer",
            condition = "Injured",
            careRequirements = "Medical care",
            rehabilitationStatus = "In progress"
        )

        val rescueCase = RescueCase(
            caseReference = "RC001",
            rescueDate = "2026-10-24",
            location = "Forest",
            reportedCondition = "Injured",
            status = "Open"
        )

        manager.registerAnimal(animal)
        manager.registerRescueCase(rescueCase)

        manager.linkAnimalToRescueCase(2, "RC001")

        assertEquals(1, rescueCase.getAnimalCount())
        assertEquals(2, rescueCase.animals[0].id)
    }

    @Test
    fun testFacilityCapacity() {
        val facility = Facility(
            facilityId = 1,
            name = "Wildlife Rescue Centre",
            location = "London",
            capacity = 2,
            suitableSpecies = listOf("Fox", "Deer"),
            available = true,
            availableFacilities = listOf("Medical Room")
        )

        // Initially, the facility has space
        assertEquals(true, facility.hasAvailableSpace())

        // One animal is admitted
        facility.currentAnimals = 1
        assertEquals(true, facility.hasAvailableSpace())

        // Facility reaches maximum capacity
        facility.currentAnimals = 2
        assertEquals(false, facility.hasAvailableSpace())
    }

    @Test
    fun testInvalidAllocationPrevention() {
        val manager = AnimalAdmissionManager()

        val animal = Animal(
            id = 3,
            species = "Fox",
            condition = "Injured",
            careRequirements = "Medical care",
            rehabilitationStatus = "In progress"
        )

        val facility1 = Facility(
            facilityId = 1,
            name = "Rescue Centre A",
            location = "London",
            capacity = 5,
            suitableSpecies = listOf("Fox"),
            available = true,
            availableFacilities = listOf("Medical Room")
        )

        val facility2 = Facility(
            facilityId = 2,
            name = "Rescue Centre B",
            location = "Croydon",
            capacity = 5,
            suitableSpecies = listOf("Fox"),
            available = true,
            availableFacilities = listOf("Medical Room")
        )

        manager.registerAnimal(animal)

        manager.allocateAnimal(
            allocationId = 1,
            animalId = 3,
            facility = facility1,
            allocationDate = "2026-10-24"
        )

        assertFailsWith<IllegalArgumentException> {
            manager.allocateAnimal(
                allocationId = 2,
                animalId = 3,
                facility = facility2,
                allocationDate = "2026-10-24"
            )
        }

        assertEquals(1, manager.getAllocations().size)
        assertEquals(1, facility1.currentAnimals)
        assertEquals(0, facility2.currentAnimals)
    }

    @Test
    fun testAllocationCancellation() {
        val manager = AnimalAdmissionManager()

        val animal = Animal(
            id = 4,
            species = "Fox",
            condition = "Injured",
            careRequirements = "Medical treatment",
            rehabilitationStatus = "In progress"
        )

        val facility = Facility(
            facilityId = 3,
            name = "Wildlife Rescue Centre",
            location = "London",
            capacity = 1,
            suitableSpecies = listOf("Fox"),
            available = true,
            availableFacilities = listOf("Medical Room")
        )

        manager.registerAnimal(animal)

        val allocation = manager.allocateAnimal(
            allocationId = 3,
            animalId = 4,
            facility = facility,
            allocationDate = "2026-10-24"
        )

        // Check the allocation is active
        assertEquals(true, allocation.active)
        assertEquals(1, facility.currentAnimals)
        assertEquals(false, facility.hasAvailableSpace())

        // Cancel the allocation
        manager.cancelAllocation(3)

        // Check cancellation and restored capacity
        assertEquals(false, allocation.active)
        assertEquals(0, facility.currentAnimals)
        assertEquals(true, facility.hasAvailableSpace())
    }

    @Test
    fun testAllocationWhenFacilityIsFull() {
        val manager = AnimalAdmissionManager()

        val animal = Animal(
            id = 5,
            species = "Fox",
            condition = "Injured",
            careRequirements = "Medical care",
            rehabilitationStatus = "In progress"
        )

        val facility = Facility(
            facilityId = 4,
            name = "Small Rescue Centre",
            location = "London",
            capacity = 1,
            suitableSpecies = listOf("Fox"),
            available = true,
            availableFacilities = listOf("Medical Room")
        )

        manager.registerAnimal(animal)

        // Simulate a facility that has reached capacity
        facility.currentAnimals = 1

        assertFailsWith<IllegalArgumentException> {
            manager.allocateAnimal(
                allocationId = 4,
                animalId = 5,
                facility = facility,
                allocationDate = "2026-10-24"
            )
        }

        // A failed allocation must not change the facility count
        assertEquals(1, facility.currentAnimals)
        assertEquals(0, manager.getAllocations().size)
    }

    @Test
    fun testUnsuitableSpeciesAllocation() {
        val manager = AnimalAdmissionManager()

        val animal = Animal(
            id = 6,
            species = "Fox",
            condition = "Injured",
            careRequirements = "Medical treatment",
            rehabilitationStatus = "In progress"
        )

        val facility = Facility(
            facilityId = 5,
            name = "Bird Rehabilitation Centre",
            location = "London",
            capacity = 5,
            suitableSpecies = listOf("Owl", "Eagle"),
            available = true,
            availableFacilities = listOf("Bird Enclosure")
        )

        manager.registerAnimal(animal)

        assertFailsWith<IllegalArgumentException> {
            manager.allocateAnimal(
                allocationId = 5,
                animalId = 6,
                facility = facility,
                allocationDate = "2026-10-24"
            )
        }

        assertEquals(0, facility.currentAnimals)
        assertEquals(0, manager.getAllocations().size)
    }
    @Test
    fun testRehabilitationStatusUpdate() {
        val manager = AnimalAdmissionManager()

        val animal = Animal(
            id = 7,
            species = "Fox",
            condition = "Recovering",
            careRequirements = "Medical care",
            rehabilitationStatus = "In progress"
        )

        manager.registerAnimal(animal)

        manager.updateRehabilitationStatus(7, "Completed")

        assertEquals("Completed", animal.rehabilitationStatus)
    }
}
