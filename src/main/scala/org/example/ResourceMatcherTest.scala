
package org.example

object ResourceMatcherTest {

  def main(args: Array[String]): Unit = {

    // Create an animal for testing
    val animal = new Animal(
      1,
      "dog",
      "injured",
      "Medical care",
      "Ongoing",
      null,
      ""
    )

    // Load existing resources
    val facilities = DataStorage.INSTANCE.loadFacilities()
    val equipments = DataStorage.INSTANCE.loadEquipment()
    val staffList = DataStorage.INSTANCE.loadStaff()

    val matcher = new ResourceMatcher()

    // Find suitable resource combinations
    val matches = matcher.matchResources(
      animal,
      facilities,
      equipments,
      staffList,
      "indoor", //available facilities
      1,
      "bandage", //equipment
      1,
      "medical science in animals level 6" //staff
    )

    // Display the results
    println("Suitable resource combinations:")
    println("Total matches: " + matches.size())

    if (matches.isEmpty) {
      println("No suitable resource combinations found.")
    } else {

      matches.forEach { result =>

        println("-----------------------------")
        println("Facility: " + result.getFacility.getName)
        println("Equipment: " + result.getEquipment.getEquipmentType)
        println("Staff: " + result.getStaff.getName)

      }
    }
  }
}
