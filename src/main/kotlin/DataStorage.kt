
package org.example

import java.io.File

object DataStorage{

    private val folder = File("data_save")
    //saving facilities to text file
    fun saveFacilities(facilities: List<Facility>){
        folder.mkdirs()

        val file = File(folder, "facilities.txt")

        file.writeText(
            facilities.joinToString("\n") {facility ->
                listOf(
                    facility.facilityId.toString(),
                    facility.name,
                    facility.location,
                    facility.capacity.toString(),
                    facility.suitableSpecies.joinToString(";"),
                    facility.available.toString(),
                    facility.availableFacilities.joinToString(";"),
                    facility.currentAnimals.toString()

                ).joinToString("|")
            }
        )


    }

    //loading facilities from text file
    fun loadFacilities(): MutableList<Facility>{

        val file = File(folder, "facilities.txt")

        if (!file.exists()){
            return mutableListOf()
        }

        val facilities = mutableListOf<Facility>()

        file.forEachLine { line ->
            val parts = line.split("|")

            val facility = Facility(
                facilityId = parts[0].toInt(),
                name = parts[1],
                location = parts[2],
                capacity = parts[3].toInt(),
                suitableSpecies = parts[4].split(";"),
                available = parts[5].toBoolean(),
                availableFacilities = parts[6].split(";").filter { it.isNotEmpty() }

            )
            facility.currentAnimals = parts[7].toInt()
            facilities.add(facility)
        }

        return facilities

    }

    //saving equipment to text file
    fun saveEquipment(equipments: List<Equipment>){
        folder.mkdirs()

        val file = File(folder, "equipment.txt")

        file.writeText(
            equipments.joinToString("\n") {equipment ->
                listOf(
                    equipment.equipmentId.toString(),
                    equipment.equipmentType,
                    equipment.quantity.toString(),
                    equipment.location,
                    equipment.available.toString()
                ).joinToString("|")
            }
        )
    }

    //loading equipment from text file
    fun loadEquipment(): MutableList<Equipment>{
        val file = File(folder, "equipment.txt")
        if (!file.exists()){
            return mutableListOf()
        }
        val equipments = mutableListOf<Equipment>()

        file.forEachLine { line ->
            val parts = line.split("|")

            val equipment = Equipment(
                equipmentId = parts[0].toInt(),
                equipmentType = parts[1],
                quantity = parts[2].toInt(),
                location = parts[3],
                available = parts [4].toBoolean()

            )
            equipments.add(equipment)
        }
        return equipments
    }
}