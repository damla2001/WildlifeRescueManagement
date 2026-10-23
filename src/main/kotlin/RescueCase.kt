
package org.example

class RescueCase(
    val caseReference: String,
    val rescueDate: String,
    val location: String,
    var reportedCondition: String,
    var status: String,
    val animals: MutableList<Animal> = mutableListOf(),
    var reportedAnimalCount: Int = 0,
    var initialAssessment: String = "",
    var requiredCare: String = "",
    var additionalNotes: String = ""
) {

    fun addAnimal(animal: Animal) {
        require(reportedAnimalCount == 0 ||
                animals.size < reportedAnimalCount) {
            "The reported animal count has been reached"
        }

        animals.add(animal)
    }

    fun getAnimalCount(): Int {
        return animals.size
    }

    fun updateCondition(newCondition: String) {
        reportedCondition = newCondition
    }

    fun updateStatus(newStatus: String) {
        status = newStatus
    }
}

