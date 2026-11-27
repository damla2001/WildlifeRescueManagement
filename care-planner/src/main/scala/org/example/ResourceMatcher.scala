package org.example

class ResourceMatcher {

  // Find suitable facilities
  def matchFacilities(
                       requirements: AnimalResourceRequirements,
                       facilities: List[ResourceFacility]
                     ): List[ResourceFacility] = {

    facilities.filter { facility =>
      facility.available &&
        facility.space >= requirements.minimumSpace &&
        facility.enclosureType.equalsIgnoreCase(
          requirements.enclosureType
        ) &&
        facility.suitableSpecies.exists(
          _.equalsIgnoreCase(requirements.species)
        ) &&
        facility.currentAnimals < facility.capacity
    }
  }

  // Find suitable equipment
  def matchEquipment(
                      requirements: AnimalResourceRequirements,
                      equipment: List[CareEquipment]
                    ): List[CareEquipment] = {

    equipment.filter { item =>
      item.available &&
        item.quantity > 0 &&
        requirements.requiredEquipment.exists(
          _.equalsIgnoreCase(item.equipmentType)
        )
    }
  }

  // Find suitable staff
  def matchStaff(
                  requirements: AnimalResourceRequirements,
                  staff: List[CareStaff]
                ): List[CareStaff] = {

    staff.filter { member =>
      member.available &&
        requirements.requiredQualifications.forall { required =>
          member.qualifications.exists(
            _.equalsIgnoreCase(required)
          )
        }
    }
  }

  // Generate valid resource combinations
  def findMatches(
                   requirements: AnimalResourceRequirements,
                   facilities: List[ResourceFacility],
                   staff: List[CareStaff],
                   equipment: List[CareEquipment]
                 ): List[ResourceMatch] = {

    val suitableFacilities =
      matchFacilities(requirements, facilities)

    val suitableStaff =
      matchStaff(requirements, staff)

    val suitableEquipment =
      matchEquipment(requirements, equipment)

    val equipmentOptions =
      requirements.requiredEquipment.toList.map { required =>
        suitableEquipment.filter { item =>
          item.equipmentType.equalsIgnoreCase(required)
        }
      }

    val equipmentCombinations =
      equipmentOptions.foldLeft(List(List.empty[CareEquipment])) {
        (current, next) =>
          current.flatMap { combination =>
            next.map { item =>
              combination :+ item
            }
          }
      }

    // Check whether all required equipment is available
    val allEquipmentAvailable =
      requirements.requiredEquipment.forall { required =>
        suitableEquipment.exists { item =>
          item.equipmentType.equalsIgnoreCase(required)
        }
      }

    // Return no matches if required equipment is missing
    if (!allEquipmentAvailable) {
      List.empty[ResourceMatch]
    } else {

      // Combine suitable facilities, staff and equipment
      for {
        facility <- suitableFacilities
        member <- suitableStaff
        selectedEquipment <- equipmentCombinations
      } yield ResourceMatch(
        facility,
        member,
        selectedEquipment
      )
    }
  }
}