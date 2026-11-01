
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
}