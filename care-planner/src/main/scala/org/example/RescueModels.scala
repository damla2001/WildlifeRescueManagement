package org.example

// Animal resource requirements
case class AnimalResourceRequirements(
                                       animalId: Int,
                                       species: String,
                                       enclosureType: String,
                                       minimumSpace: Double,
                                       requiredEquipment: Set[String],
                                       requiredQualifications: Set[String]
                                     )

// Facility information
case class ResourceFacility(
                             id: Int,
                             name: String,
                             enclosureType: String,
                             space: Double,
                             suitableSpecies: Set[String],
                             available: Boolean,
                             capacity: Int,
                             currentAnimals: Int
                           )

// Valid resource combination
case class ResourceMatch(
                          facility: ResourceFacility,
                          staff: CareStaff,
                          equipment: List[CareEquipment]
                        )