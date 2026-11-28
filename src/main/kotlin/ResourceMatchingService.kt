package org.example

//Aidan



data class ResourceMatch(
    val facility: Facility,
    val equipment: Equipment,
    val staff: Staff
)

@JvmSuppressWildcards
interface ResourceMatchingService {

    fun matchResources(
        animal: Animal,
        facilities: List<Facility>,
        equipments: List<Equipment>,
        staffList: List<Staff>,
        requiredEnclosure: String,
        minimumSpace: Int,
        requiredEquipment: String,
        minimumQuantity: Int,
        requiredQualification: String
    ): List<ResourceMatch>
}
