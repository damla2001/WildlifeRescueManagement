package org.example

import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Base64

/** Stores Task C records separately, leaving Aidan's DataStorage format unchanged. */
object TaskCPersistence {
    private val file = File("data_save/task_c_state.txt")

    // Encoding each text field avoids corrupting records containing tabs or newlines.
    private fun encode(text: String): String = Base64.getEncoder()
        .encodeToString(text.toByteArray(Charsets.UTF_8))

    private fun decode(text: String): String = String(
        Base64.getDecoder().decode(text), Charsets.UTF_8
    )

    private fun record(kind: String, vararg fields: String): String =
        (listOf(kind) + fields.map(::encode)).joinToString("\t")

    fun save(cases: List<RescueCase>, manager: AnimalAdmissionManager) {
        val rows = mutableListOf("TASK_C_STATE\t1")
        cases.forEach { rescueCase ->
            rows += record(
                "CASE", rescueCase.caseReference, rescueCase.rescueDate,
                rescueCase.location, rescueCase.reportedCondition, rescueCase.status,
                rescueCase.reportedAnimalCount.toString(),
                rescueCase.initialAssessment, rescueCase.requiredCare,
                rescueCase.additionalNotes
            )
        }
        manager.getRegisteredAnimals().forEach { animal ->
            rows += record(
                "ANIMAL", animal.id.toString(), animal.species,
                animal.condition, animal.careRequirements, animal.rehabilitationStatus,
                animal.rehabilitationCaseReference.orEmpty(), animal.resourceRequirements
            )
        }
        cases.forEach { rescueCase ->
            rescueCase.animals.forEach { animal ->
                rows += record("LINK", rescueCase.caseReference, animal.id.toString())
            }
        }
        manager.getAllocations().forEach { allocation ->
            rows += record(
                "ALLOCATION", allocation.allocationId.toString(),
                allocation.animal.id.toString(), allocation.facility.facilityId.toString(),
                allocation.allocationDate, allocation.active.toString()
            )
        }
        // Facility occupancy at the moment of this snapshot, for a consistent restart.
        manager.getAllocations().map { it.facility }.distinctBy { it.facilityId }
            .forEach { facility ->
                rows += record("OCCUPANCY", facility.facilityId.toString(),
                    facility.currentAnimals.toString())
            }
        rows += "END"

        val parent = file.parentFile
        parent.mkdirs()
        val temp = File.createTempFile("task_c_state_", ".tmp", parent)
        try {
            temp.writeText(rows.joinToString("\n"), Charsets.UTF_8)
            try {
                Files.move(temp.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            temp.delete()
        }
    }

    /** Rebuilds the *same* Animal instances for cases and allocations. */
    fun restore(manager: AnimalAdmissionManager, facilities: List<Facility>): List<RescueCase> {
        if (!file.exists()) return emptyList()
        val lines = file.readLines(Charsets.UTF_8)
        require(lines.firstOrNull() == "TASK_C_STATE\t1" && lines.lastOrNull() == "END") {
            "Task C saved data has an invalid format: ${file.path}"
        }
        val parsed = lines.drop(1).dropLast(1).map { line ->
            val columns = line.split("\t")
            require(columns.isNotEmpty()) { "Invalid saved record" }
            columns.first() to columns.drop(1).map(::decode)
        }
        val loadedCases = mutableListOf<RescueCase>()
        val caseByReference = linkedMapOf<String, RescueCase>()
        val animalById = linkedMapOf<Int, Animal>()
        for ((kind, f) in parsed) when (kind) {
            "CASE" -> {
                require(f.size == 9) { "Invalid rescue case record" }
                val rescued = RescueCase(
                    f[0], f[1], f[2], f[3], f[4],
                    reportedAnimalCount = f[5].toInt(),
                    initialAssessment = f[6], requiredCare = f[7], additionalNotes = f[8]
                )
                require(caseByReference.putIfAbsent(rescued.caseReference, rescued) == null) {
                    "Duplicate saved case reference"
                }
                loadedCases.add(rescued)
            }
            "ANIMAL" -> {
                require(f.size == 7) { "Invalid animal record" }
                val animal = Animal(
                    id = f[0].toInt(), species = f[1], condition = f[2],
                    careRequirements = f[3], rehabilitationStatus = f[4],
                    rehabilitationCaseReference = f[5].ifEmpty { null },
                    resourceRequirements = f[6]
                )
                require(animalById.putIfAbsent(animal.id, animal) == null) {
                    "Duplicate saved animal ID"
                }
            }
            "LINK", "ALLOCATION", "OCCUPANCY" -> Unit
            else -> error("Unknown saved Task C record type: $kind")
        }
        // Validate all references before mutating the manager or facility list.
        val links = parsed.filter { it.first == "LINK" }.map { (_, f) ->
            require(f.size == 2) { "Invalid animal/case link" }
            val rescueCase = caseByReference[f[0]] ?: error("Saved case not found: ${f[0]}")
            val animal = animalById[f[1].toInt()] ?: error("Saved animal not found: ${f[1]}")
            rescueCase to animal
        }
        val allocationData = parsed.filter { it.first == "ALLOCATION" }.map { (_, f) ->
            require(f.size == 5) { "Invalid allocation record" }
            val animal = animalById[f[1].toInt()] ?: error("Saved allocation animal not found")
            val facility = facilities.find { it.facilityId == f[2].toInt() }
                ?: error("Saved allocation facility ${f[2]} no longer exists")
            Triple(f[0].toInt(), animal to facility, f[3] to f[4].toBooleanStrict())
        }
        val savedOccupancy = parsed.filter { it.first == "OCCUPANCY" }.map { (_, f) ->
            require(f.size == 2) { "Invalid occupancy record" }
            f[0].toInt() to f[1].toInt()
        }.toMap()
        require(links.distinctBy { it.first.caseReference to it.second.id }.size == links.size) {
            "Duplicate saved animal/case link"
        }
        for ((rescueCase, animal) in links) rescueCase.addAnimal(animal)
        loadedCases.forEach(manager::registerRescueCase)
        animalById.values.forEach(manager::registerAnimal)
        // Linking through the manager ensures its own case and animal maps remain consistent.
        // The lists were populated above solely to validate counts; clear before linking.
        loadedCases.forEach { it.animals.clear() }
        for ((rescueCase, animal) in links) {
            manager.linkAnimalToRescueCase(animal.id, rescueCase.caseReference)
        }
        allocationData.forEach { (id, pair, details) ->
            manager.restoreAllocation(id, pair.first.id, pair.second, details.first, details.second)
        }
        for (facility in facilities) {
            val occupancy = savedOccupancy[facility.facilityId] ?: continue
            require(occupancy >= 0) { "Invalid saved facility occupancy" }
            facility.currentAnimals = occupancy
        }
        return loadedCases
    }
}
