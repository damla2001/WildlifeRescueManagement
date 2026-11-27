package org.example

import scala.jdk.javaapi.CollectionConverters

class ResourceMatchingService {

    private val matcher = ResourceMatcher()

    // Convert Kotlin facility data into the Scala resource model
    private fun convertFacility(facility: Facility): ResourceFacility {
        return ResourceFacility(
            facility.facilityId,
            facility.name,
            facility.enclosureType,
            facility.space,
            CollectionConverters.asScala(
                facility.suitableSpecies.toSet()
            ).toSet(),
            facility.available,
            facility.capacity,
            facility.currentAnimals
        )
    }

    // Convert Kotlin staff data into the Scala care model
    private fun convertStaff(staff: Staff): CareStaff {
        return CareStaff(
            staff.staffId,
            staff.name,
            CollectionConverters.asScala(
                staff.qualifications.toSet()
            ).toSet(),
            staff.available,
            0,
            1440
        )
    }

    // Convert Kotlin equipment data into the Scala care model
    private fun convertEquipment(equipment: Equipment): CareEquipment {
        return CareEquipment(
            equipment.equipmentId,
            equipment.equipmentType,
            equipment.quantity,
            equipment.available
        )
    }

    // Match animal requirements with available resources
    fun findMatches(
        animal: Animal,
        enclosureType: String,
        minimumSpace: Double,
        requiredEquipment: List<String>,
        requiredQualifications: List<String>,
        facilities: List<Facility>,
        staff: List<Staff>,
        equipment: List<Equipment>
    ): List<ResourceMatch> {

        val requirements = AnimalResourceRequirements(
            animal.id,
            animal.species,
            enclosureType,
            minimumSpace,
            CollectionConverters.asScala(
                requiredEquipment.toSet()
            ).toSet(),
            CollectionConverters.asScala(
                requiredQualifications.toSet()
            ).toSet()
        )

        // Convert facility records into a Scala list
        val scalaFacilities = CollectionConverters.asScala(
            facilities.map { convertFacility(it) }
        ).toList()

        // Convert staff records into a Scala list
        val scalaStaff = CollectionConverters.asScala(
            staff.map { convertStaff(it) }
        ).toList()

        // Convert equipment records into a Scala list
        val scalaEquipment = CollectionConverters.asScala(
            equipment.map { convertEquipment(it) }
        ).toList()

// Execute the Scala resource matching algorithm
        val results = matcher.findMatches(
            requirements,
            scalaFacilities,
            scalaStaff,
            scalaEquipment
        )

// Convert Scala matching results into a Kotlin list
        return CollectionConverters.asJava(results).toList()
    }

}