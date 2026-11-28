package org.example

import scala.jdk.CollectionConverters._



class ResourceMatcher extends ResourceMatchingService { //scala implements from ResourceMatchingService

  // Find suitable facilities
  def matchFacilities(
                       animal: Animal,
                       facilities: java.util.List[Facility],
                       requiredEnclosure: String,
                       minimumSpace: Int
                     ): java.util.List[Facility] = {

    facilities.asScala.toList.filter { facility =>

      facility.getSuitableSpecies.contains(animal.getSpecies) &&
        facility.hasAvailableSpace() &&
        facility.getAvailableFacilities.contains(requiredEnclosure) &&
        (facility.getCapacity - facility.getCurrentAnimals) >= minimumSpace

    }.asJava
  }
  // Find suitable equipment
  def matchEquipment(
                      equipments: java.util.List[Equipment],
                      requiredEquipment: String,
                      minimumQuantity: Int
                    ): java.util.List[Equipment] = {

    equipments.asScala.toList.filter { equipment =>

      equipment.getEquipmentType.equalsIgnoreCase(requiredEquipment) &&
        equipment.getAvailable &&
        equipment.getQuantity >= minimumQuantity

    }.asJava
  }
  // Find suitable staff members
  def matchStaff(
                  staffList: java.util.List[Staff],
                  requiredQualification: String
                ): java.util.List[Staff] = {

    staffList.asScala.toList.filter { staff =>

      staff.getAvailable &&
        staff.getQualifications.asScala.exists { qualification =>
          qualification.equalsIgnoreCase(requiredQualification)
        }

    }.asJava
  }

  // Generate suitable resource combinations

  override def matchResources( //provide the function from ResourceMatchingService.kt
                      animal: Animal,
                      facilities: java.util.List[Facility],
                      equipments: java.util.List[Equipment],
                      staffList: java.util.List[Staff],
                      requiredEnclosure: String,
                      minimumSpace: Int,
                      requiredEquipment: String,
                      minimumQuantity: Int,
                      requiredQualification: String
                    ): java.util.List[ResourceMatch] = {

    val suitableFacilities = matchFacilities(
      animal,
      facilities,
      requiredEnclosure,
      minimumSpace
    ).asScala.toList

    val suitableEquipment = matchEquipment(
      equipments,
      requiredEquipment,
      minimumQuantity
    ).asScala.toList

    val suitableStaff = matchStaff(
      staffList,
      requiredQualification
    ).asScala.toList

    val matches = for {
      facility <- suitableFacilities
      equipment <- suitableEquipment
      staff <- suitableStaff
    } yield new ResourceMatch(facility, equipment, staff)

    matches.asJava
  }


}
