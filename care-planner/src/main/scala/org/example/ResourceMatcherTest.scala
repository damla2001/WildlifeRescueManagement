package org.example

object ResourceMatcherTest {

  def main(args: Array[String]): Unit = {

    val matcher = new ResourceMatcher()

    val requirements = AnimalResourceRequirements(
      1,
      "Fox",
      "Rehabilitation",
      10.0,
      Set("Medical kit"),
      Set("Animal care")
    )

    val facilities = List(
      ResourceFacility(
        1, "Enclosure A", "Rehabilitation",
        15.0, Set("Fox"), true, 5, 1
      ),
      ResourceFacility(
        2, "Enclosure B", "Rehabilitation",
        20.0, Set("Fox"), true, 5, 1
      )
    )

    val staff = List(
      CareStaff(
        1, "Emma", Set("Animal care"),
        true, 540, 1020
      ),
      CareStaff(
        2, "James", Set("Animal care"),
        true, 540, 1020
      )
    )

    val equipment = List(
      CareEquipment(1, "Medical kit", 2, true),
      CareEquipment(2, "Medical kit", 1, true)
    )

    println("RESOURCE MATCHING TESTS")

    // Test 1: Single valid combination
    val singleResults = matcher.findMatches(
      requirements,
      List(facilities.head, facilities(1).copy(space = 5.0)),
      List(staff.head, staff(1).copy(
        qualifications = Set("Administration")
      )),
      List(equipment.head)
    )

    println("Test 1: Single valid combination")
    println("Expected: 1  Actual: " + singleResults.size)
    assert(singleResults.size == 1)
    assert(singleResults.head.facility.id == 1)
    assert(singleResults.head.staff.id == 1)
    println("PASSED")

    // Test 2: Multiple valid combinations
    val multipleResults = matcher.findMatches(
      requirements,
      facilities,
      staff,
      List(equipment.head)
    )

    println("Test 2: Multiple resource combinations")
    println("Expected: 4  Actual: " + multipleResults.size)
    assert(multipleResults.size == 4)
    println("PASSED")

    // Test 3: Alternative equipment combinations
    val alternativeResults = matcher.findMatches(
      requirements,
      facilities,
      staff,
      equipment
    )

    println("Test 3: Alternative equipment combinations")
    println("Expected: 8  Actual: " + alternativeResults.size)
    assert(alternativeResults.size == 8)
    println("PASSED")

    // Display valid resource combinations
    alternativeResults.foreach { result =>
      println(
        "Facility: " + result.facility.name +
          ", Staff: " + result.staff.name +
          ", Equipment: " +
          result.equipment.map(item =>
            s"${item.id} - ${item.equipmentType}"
          ).mkString(", ")
      )
    }

    // Test 4: No valid resource combinations
    val unavailableEquipment = equipment.map(
      item => item.copy(available = false)
    )

    val noResults = matcher.findMatches(
      requirements,
      facilities,
      staff,
      unavailableEquipment
    )

    println("Test 4: No valid resource combinations")
    println("Expected: 0  Actual: " + noResults.size)
    assert(noResults.isEmpty)
    println("PASSED")

    println("All 4 tests passed successfully!")
  }
}