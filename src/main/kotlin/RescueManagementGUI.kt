
package org.example

import java.awt.BorderLayout
import java.awt.GridLayout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import javax.swing.*
import javax.swing.table.DefaultTableModel

class RescueManagementGUI : JFrame() {

    private val animalAdmissionManager = AnimalAdmissionManager()
    private var nextAnimalId = 1

    private val facilities = mutableListOf<Facility>()
    private val rescueCases = mutableListOf<RescueCase>()
    private val cases = mutableListOf<CaseRecord>()

    private val caseReferenceField = JTextField()
    private val rescueDateField = JTextField()
    private val locationField = JTextField()
    private val speciesField = JTextField()
    private val animalCountField = JTextField()
    private val conditionField = JTextField()
    private val assessmentField = JTextField()
    private val careField = JTextField()
    private val statusField = JTextField()
    private val notesField = JTextField()

    private data class CaseRecord(
        val reference: String,
        val date: String,
        val location: String,
        val species: String,
        val animalCount: Int,
        val condition: String,
        val assessment: String,
        val care: String,
        val status: String,
        val notes: String
    )

    private fun newTableModel(
        vararg columns: String
    ): DefaultTableModel {
        return object : DefaultTableModel(
            arrayOf(*columns), 0
        ) {
            override fun isCellEditable(
                row: Int,
                column: Int
            ): Boolean = false
        }
    }

    private val caseTableModel = newTableModel(
        "Case Reference", "Date", "Location",
        "Species", "Animals", "Condition", "Status"
    )

    private val caseTable = JTable(caseTableModel)

    private val facilityTableModel = newTableModel(
        "ID", "Name", "Location", "Capacity",
        "Suitable Species", "Available Facilities",
        "Available"
    )

    private val facilityTable = JTable(facilityTableModel)

    private val equipmentTableModel = newTableModel(
        "Equipment ID", "Equipment Type",
        "Quantity", "Location", "Available"
    )

    private val equipmentTable = JTable(equipmentTableModel)

    private val staffTableModel = newTableModel(
        "Staff ID", "Name", "Role",
        "Qualifications", "Available"
    )

    private val staffTable = JTable(staffTableModel)

    private val equipments = mutableListOf<Equipment>()
    private val staffList = mutableListOf<Staff>()

    init {
        title = "Wildlife Rescue Management System"
        setSize(1100, 750)
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)

        val tabs = JTabbedPane()

        tabs.addTab(
            "Animal Allocation",
            AnimalAllocationPanel(
                animalAdmissionManager,
                facilities
            )
        )

        tabs.addTab("Rescue Cases", createRescuePanel())
        tabs.addTab("Facilities", createFacilityPanel())
        tabs.addTab("Equipment", createEquipmentPanel())
        tabs.addTab("Staff", createStaffPanel())
        tabs.addTab("Care Plans", createCarePlansPanel())

        layout = BorderLayout()
        add(tabs, BorderLayout.CENTER)
    }

    // =====================================
    // COMMON HELPERS
    // =====================================

    private fun showMessage(message: String) {
        JOptionPane.showMessageDialog(this, message)
    }

    private fun confirmDeletion(message: String): Boolean {
        return JOptionPane.showConfirmDialog(
            this,
            message,
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION
    }

    private fun addField(
        panel: JPanel,
        label: String,
        field: JTextField
    ) {
        panel.add(JLabel(label))
        panel.add(field)
    }

    private fun buttons(vararg items: JButton): JPanel {
        val panel = JPanel(GridLayout(1, items.size, 10, 10))
        items.forEach { panel.add(it) }
        return panel
    }

    private fun makePanel(
        form: JPanel,
        buttonPanel: JPanel,
        table: JTable
    ): JPanel {
        val top = JPanel(BorderLayout(10, 10))
        top.add(form, BorderLayout.CENTER)
        top.add(buttonPanel, BorderLayout.SOUTH)

        val panel = JPanel(BorderLayout(10, 10))
        panel.add(top, BorderLayout.NORTH)
        panel.add(JScrollPane(table), BorderLayout.CENTER)

        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        )

        return panel
    }

    private fun commaList(text: String): List<String> {
        return text.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun validDate(date: String): Boolean {
        return try {
            val formatter = DateTimeFormatter
                .ofPattern("uuuu-MM-dd")
                .withResolverStyle(ResolverStyle.STRICT)

            LocalDate.parse(date, formatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    // =====================================
    // RESCUE CASE MANAGEMENT
    // =====================================

    private fun createRescuePanel(): JPanel {
        val form = JPanel(GridLayout(10, 2, 10, 10))
        form.border = BorderFactory.createTitledBorder(
            "Rescue Case Details"
        )

        addField(form, "Case Reference:", caseReferenceField)
        addField(form, "Rescue Date:", rescueDateField)
        addField(form, "Location:", locationField)
        addField(form, "Animal Species:", speciesField)
        addField(form, "Number of Animals:", animalCountField)
        addField(form, "Reported Condition:", conditionField)
        addField(form, "Initial Assessment:", assessmentField)
        addField(form, "Required Care:", careField)
        addField(form, "Case Status:", statusField)
        addField(form, "Additional Notes:", notesField)

        val save = JButton("Save Case")
        val edit = JButton("Edit Case")
        val delete = JButton("Delete Case")
        val clear = JButton("Clear Fields")

        val panel = makePanel(
            form,
            buttons(save, edit, delete, clear),
            caseTable
        )

        caseTable.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting && caseTable.selectedRow >= 0) {
                val row = caseTable.convertRowIndexToModel(
                    caseTable.selectedRow
                )
                displayCase(cases[row])
            }
        }

        save.addActionListener {
            val record = readCaseForm() ?: return@addActionListener

            if (cases.any {
                    it.reference.equals(
                        record.reference,
                        ignoreCase = true
                    )
                }) {
                showMessage("Case reference already exists.")
                return@addActionListener
            }

            val rescueCase = RescueCase(
                caseReference = record.reference,
                rescueDate = record.date,
                location = record.location,
                reportedCondition = record.condition,
                status = record.status,
                reportedAnimalCount = record.animalCount,
                initialAssessment = record.assessment,
                requiredCare = record.care,
                additionalNotes = record.notes
            )

            animalAdmissionManager.registerRescueCase(rescueCase)

            val createdAnimalIds = mutableListOf<Int>()

            repeat(record.animalCount) {
                val animal = Animal(
                    id = nextAnimalId++,
                    species = record.species,
                    condition = record.condition,
                    careRequirements = record.care,
                    rehabilitationStatus = record.status
                )

                animalAdmissionManager.registerAnimal(animal)

                animalAdmissionManager.linkAnimalToRescueCase(
                    animal.id,
                    rescueCase.caseReference
                )

                createdAnimalIds.add(animal.id)
            }

            rescueCases.add(rescueCase)
            cases.add(record)

            refreshCaseTable()
            clearCaseFields()

            showMessage(
                "Rescue case added successfully!\n" +
                        "Registered Animal IDs: " +
                        createdAnimalIds.joinToString(", ")
            )
        }

        edit.addActionListener {
            val selected = caseTable.selectedRow

            if (selected < 0) {
                showMessage("Please select a case to edit.")
                return@addActionListener
            }

            val row = caseTable.convertRowIndexToModel(selected)
            val record = readCaseForm() ?: return@addActionListener
            val previous = cases[row]
            val existingCase = rescueCases[row]

            if (record.reference != previous.reference) {
                showMessage(
                    "Case Reference cannot be changed after registration."
                )
                return@addActionListener
            }

            if (cases.withIndex().any {
                    it.index != row &&
                            it.value.reference.equals(
                                record.reference,
                                ignoreCase = true
                            )
                }) {
                showMessage("Case reference already exists.")
                return@addActionListener
            }

            if (record.animalCount != existingCase.animals.size) {
                showMessage(
                    "Animal count cannot be changed during editing. " +
                            "Existing animal registrations and allocations " +
                            "must remain consistent."
                )
                return@addActionListener
            }

            existingCase.reportedCondition = record.condition
            existingCase.status = record.status
            existingCase.initialAssessment = record.assessment
            existingCase.requiredCare = record.care
            existingCase.additionalNotes = record.notes


            existingCase.animals.forEach { animal ->
                animal.species = record.species
                animal.condition = record.condition
                animal.careRequirements = record.care
            }


            cases[row] = record

            refreshCaseTable()
            clearCaseFields()
            showMessage("Rescue case updated successfully!")
        }

        delete.addActionListener {
            val selected = caseTable.selectedRow

            if (selected < 0) {
                showMessage("Please select a case to delete.")
                return@addActionListener
            }

            val row = caseTable.convertRowIndexToModel(selected)
            val rescueCase = rescueCases[row]

            val allocatedIds = animalAdmissionManager
                .getAllocations()
                .filter { it.active }
                .map { it.animal.id }
                .toSet()

            if (rescueCase.animals.any { it.id in allocatedIds }) {
                showMessage(
                    "Cannot delete a case containing animals " +
                            "with active allocations. Cancel them first."
                )
                return@addActionListener
            }

            showMessage(
                "Deleting a rescue case is disabled until " +
                        "animal registration cleanup is implemented. " +
                        "This prevents orphaned animal records."
            )
        }

        clear.addActionListener {
            clearCaseFields()
        }

        return panel
    }

    private fun readCaseForm(): CaseRecord? {
        val reference = caseReferenceField.text.trim()
        val date = rescueDateField.text.trim()
        val location = locationField.text.trim()
        val species = speciesField.text.trim()
        val count = animalCountField.text.trim().toIntOrNull()
        val condition = conditionField.text.trim()
        val assessment = assessmentField.text.trim()
        val care = careField.text.trim()
        val status = statusField.text.trim()
        val notes = notesField.text.trim()

        if (
            reference.isBlank() ||
            location.isBlank() ||
            species.isBlank() ||
            count == null ||
            count <= 0 ||
            condition.isBlank() ||
            assessment.isBlank() ||
            care.isBlank() ||
            status.isBlank()
        ) {
            showMessage(
                "Please enter valid required information. " +
                        "Number of animals must be greater than zero."
            )
            return null
        }

        if (!validDate(date)) {
            showMessage(
                "Please enter a valid date in YYYY-MM-DD format."
            )
            return null
        }

        return CaseRecord(
            reference,
            date,
            location,
            species,
            count,
            condition,
            assessment,
            care,
            status,
            notes
        )
    }

    private fun displayCase(record: CaseRecord) {
        caseReferenceField.text = record.reference
        rescueDateField.text = record.date
        locationField.text = record.location
        speciesField.text = record.species
        animalCountField.text = record.animalCount.toString()
        conditionField.text = record.condition
        assessmentField.text = record.assessment
        careField.text = record.care
        statusField.text = record.status
        notesField.text = record.notes
    }

    private fun refreshCaseTable() {
        caseTableModel.rowCount = 0

        cases.forEachIndexed { index, record ->
            caseTableModel.addRow(
                arrayOf<Any>(
                    record.reference,
                    record.date,
                    record.location,
                    record.species,
                    rescueCases[index].getAnimalCount(),
                    record.condition,
                    record.status
                )
            )
        }
    }

    private fun clearCaseFields() {
        listOf(
            caseReferenceField,
            rescueDateField,
            locationField,
            speciesField,
            animalCountField,
            conditionField,
            assessmentField,
            careField,
            statusField,
            notesField
        ).forEach { it.text = "" }

        caseTable.clearSelection()
    }

    // =====================================
    // FACILITY MANAGEMENT
    // =====================================

    private fun createFacilityPanel(): JPanel {
        val idField = JTextField()
        val nameField = JTextField()
        val locationField = JTextField()
        val capacityField = JTextField()
        val speciesField = JTextField()
        val featuresField = JTextField()
        val availableBox = JCheckBox("Available", true)

        val form = JPanel(GridLayout(7, 2, 10, 10))
        form.border = BorderFactory.createTitledBorder(
            "Facility Details"
        )

        addField(form, "Facility ID:", idField)
        addField(form, "Facility Name:", nameField)
        addField(form, "Location:", locationField)
        addField(form, "Capacity:", capacityField)
        addField(form, "Suitable Species:", speciesField)
        addField(form, "Available Facilities:", featuresField)

        form.add(JLabel("Facility Availability:"))
        form.add(availableBox)

        val save = JButton("Save Facility")
        val edit = JButton("Edit Facility")
        val delete = JButton("Delete Facility")
        val clear = JButton("Clear Fields")

        val panel = makePanel(
            form,
            buttons(save, edit, delete, clear),
            facilityTable
        )

        fun clearFields() {
            listOf(
                idField, nameField, locationField,
                capacityField, speciesField, featuresField
            ).forEach { it.text = "" }

            availableBox.isSelected = true
            facilityTable.clearSelection()
        }

        fun refresh() {
            facilityTableModel.rowCount = 0

            facilities.forEach { facility ->
                facilityTableModel.addRow(
                    arrayOf<Any>(
                        facility.facilityId,
                        facility.name,
                        facility.location,
                        facility.capacity,
                        facility.suitableSpecies.joinToString(", "),
                        facility.availableFacilities.joinToString(", "),
                        facility.available
                    )
                )
            }
        }

        fun readFacility(): Facility? {
            val id = idField.text.trim().toIntOrNull()
            val name = nameField.text.trim()
            val location = locationField.text.trim()
            val capacity = capacityField.text.trim().toIntOrNull()
            val species = commaList(speciesField.text)
            val features = commaList(featuresField.text)

            if (
                id == null || id <= 0 ||
                name.isBlank() ||
                location.isBlank() ||
                capacity == null || capacity <= 0 ||
                species.isEmpty()
            ) {
                showMessage("Please enter valid facility details.")
                return null
            }

            return Facility(
                facilityId = id,
                name = name,
                location = location,
                capacity = capacity,
                suitableSpecies = species,
                available = availableBox.isSelected,
                availableFacilities = features
            )
        }

        facilityTable.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting && facilityTable.selectedRow >= 0) {
                val row = facilityTable.convertRowIndexToModel(
                    facilityTable.selectedRow
                )
                val facility = facilities[row]

                idField.text = facility.facilityId.toString()
                nameField.text = facility.name
                locationField.text = facility.location
                capacityField.text = facility.capacity.toString()
                speciesField.text =
                    facility.suitableSpecies.joinToString(", ")
                featuresField.text =
                    facility.availableFacilities.joinToString(", ")
                availableBox.isSelected = facility.available
            }
        }

        save.addActionListener {
            val facility = readFacility()
                ?: return@addActionListener

            if (facilities.any {
                    it.facilityId == facility.facilityId
                }) {
                showMessage("Facility ID already exists.")
                return@addActionListener
            }

            facilities.add(facility)
            refresh()
            clearFields()

            showMessage("Facility added successfully!")
        }

        edit.addActionListener {
            val selected = facilityTable.selectedRow

            if (selected < 0) {
                showMessage("Please select a facility to edit.")
                return@addActionListener
            }

            val row = facilityTable.convertRowIndexToModel(selected)
            val previous = facilities[row]

            if (previous.currentAnimals > 0) {
                showMessage(
                    "Cannot edit a facility with active allocations. " +
                            "Cancel the allocations first."
                )
                return@addActionListener
            }

            val updated = readFacility()
                ?: return@addActionListener

            if (facilities.withIndex().any {
                    it.index != row &&
                            it.value.facilityId == updated.facilityId
                }) {
                showMessage("Facility ID already exists.")
                return@addActionListener
            }

            val referenced = animalAdmissionManager
                .getAllocations()
                .any { it.facility === previous }

            if (referenced) {
                showMessage(
                    "This facility has allocation history. " +
                            "Editing is restricted to preserve references."
                )
                return@addActionListener
            }

            facilities[row] = updated
            refresh()
            clearFields()

            showMessage("Facility updated successfully!")
        }

        delete.addActionListener {
            val selected = facilityTable.selectedRow

            if (selected < 0) {
                showMessage("Please select a facility to delete.")
                return@addActionListener
            }

            val row = facilityTable.convertRowIndexToModel(selected)
            val facility = facilities[row]

            val referenced = animalAdmissionManager
                .getAllocations()
                .any { it.facility === facility }

            if (facility.currentAnimals > 0 || referenced) {
                showMessage(
                    "Cannot delete a facility referenced by allocations."
                )
                return@addActionListener
            }

            if (confirmDeletion(
                    "Are you sure you want to delete this facility?"
                )
            ) {
                facilities.removeAt(row)
                refresh()
                clearFields()
                showMessage("Facility deleted successfully!")
            }
        }

        clear.addActionListener {
            clearFields()
        }

        return panel
    }

    // =====================================
    // EQUIPMENT MANAGEMENT
    // =====================================

    private fun createEquipmentPanel(): JPanel {
        val idField = JTextField()
        val typeField = JTextField()
        val quantityField = JTextField()
        val locationField = JTextField()
        val availableBox = JCheckBox("Available", true)

        val form = JPanel(GridLayout(5, 2, 10, 10))
        form.border = BorderFactory.createTitledBorder(
            "Equipment Details"
        )

        addField(form, "Equipment ID:", idField)
        addField(form, "Equipment Type:", typeField)
        addField(form, "Quantity:", quantityField)
        addField(form, "Location:", locationField)

        form.add(JLabel("Equipment Availability:"))
        form.add(availableBox)

        val save = JButton("Save Equipment")
        val edit = JButton("Edit Equipment")
        val delete = JButton("Delete Equipment")
        val clear = JButton("Clear Fields")

        val panel = makePanel(
            form,
            buttons(save, edit, delete, clear),
            equipmentTable
        )

        fun clearFields() {
            listOf(
                idField, typeField, quantityField, locationField
            ).forEach { it.text = "" }

            availableBox.isSelected = true
            equipmentTable.clearSelection()
        }

        fun refresh() {
            equipmentTableModel.rowCount = 0

            equipments.forEach { equipment ->
                equipmentTableModel.addRow(
                    arrayOf<Any>(
                        equipment.equipmentId,
                        equipment.equipmentType,
                        equipment.quantity,
                        equipment.location,
                        equipment.available
                    )
                )
            }
        }

        fun readEquipment(): Equipment? {
            val id = idField.text.trim().toIntOrNull()
            val type = typeField.text.trim()
            val quantity = quantityField.text.trim().toIntOrNull()
            val location = locationField.text.trim()

            if (
                id == null || id <= 0 ||
                type.isBlank() ||
                quantity == null || quantity < 0 ||
                location.isBlank()
            ) {
                showMessage("Please enter valid equipment details.")
                return null
            }

            return Equipment(
                equipmentId = id,
                equipmentType = type,
                quantity = quantity,
                location = location,
                available = availableBox.isSelected
            )
        }

        equipmentTable.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting && equipmentTable.selectedRow >= 0) {
                val row = equipmentTable.convertRowIndexToModel(
                    equipmentTable.selectedRow
                )

                val equipment = equipments[row]

                idField.text = equipment.equipmentId.toString()
                typeField.text = equipment.equipmentType
                quantityField.text = equipment.quantity.toString()
                locationField.text = equipment.location
                availableBox.isSelected = equipment.available
            }
        }

        save.addActionListener {
            val equipment = readEquipment()
                ?: return@addActionListener

            if (equipments.any {
                    it.equipmentId == equipment.equipmentId
                }) {
                showMessage("Equipment ID already exists.")
                return@addActionListener
            }

            equipments.add(equipment)
            refresh()
            clearFields()

            showMessage("Equipment added successfully!")
        }

        edit.addActionListener {
            val selected = equipmentTable.selectedRow

            if (selected < 0) {
                showMessage("Please select equipment to edit.")
                return@addActionListener
            }

            val row = equipmentTable.convertRowIndexToModel(selected)
            val updated = readEquipment()
                ?: return@addActionListener

            if (equipments.withIndex().any {
                    it.index != row &&
                            it.value.equipmentId == updated.equipmentId
                }) {
                showMessage("Equipment ID already exists.")
                return@addActionListener
            }

            equipments[row] = updated
            refresh()
            clearFields()

            showMessage("Equipment updated successfully!")
        }

        delete.addActionListener {
            val selected = equipmentTable.selectedRow

            if (selected < 0) {
                showMessage("Please select equipment to delete.")
                return@addActionListener
            }

            val row = equipmentTable.convertRowIndexToModel(selected)

            if (confirmDeletion(
                    "Are you sure you want to delete this equipment?"
                )
            ) {
                equipments.removeAt(row)
                refresh()
                clearFields()
                showMessage("Equipment deleted successfully!")
            }
        }

        clear.addActionListener {
            clearFields()
        }

        return panel
    }

    // =====================================
    // STAFF MANAGEMENT
    // =====================================

    private fun createStaffPanel(): JPanel {
        val idField = JTextField()
        val nameField = JTextField()
        val roleField = JTextField()
        val qualificationsField = JTextField()
        val availableBox = JCheckBox("Available", true)

        val form = JPanel(GridLayout(5, 2, 10, 10))
        form.border = BorderFactory.createTitledBorder(
            "Staff Details"
        )

        addField(form, "Staff ID:", idField)
        addField(form, "Staff Name:", nameField)
        addField(form, "Role:", roleField)
        addField(form, "Qualifications:", qualificationsField)

        form.add(JLabel("Availability:"))
        form.add(availableBox)

        val save = JButton("Save Staff")
        val edit = JButton("Edit Staff")
        val delete = JButton("Delete Staff")
        val clear = JButton("Clear Fields")

        val panel = makePanel(
            form,
            buttons(save, edit, delete, clear),
            staffTable
        )

        fun clearFields() {
            listOf(
                idField, nameField, roleField, qualificationsField
            ).forEach { it.text = "" }

            availableBox.isSelected = true
            staffTable.clearSelection()
        }

        fun refresh() {
            staffTableModel.rowCount = 0

            staffList.forEach { staff ->
                staffTableModel.addRow(
                    arrayOf<Any>(
                        staff.staffId,
                        staff.name,
                        staff.role,
                        staff.qualifications.joinToString(", "),
                        staff.available
                    )
                )
            }
        }

        fun readStaff(): Staff? {
            val id = idField.text.trim().toIntOrNull()
            val name = nameField.text.trim()
            val role = roleField.text.trim()
            val qualifications =
                commaList(qualificationsField.text)

            if (
                id == null || id <= 0 ||
                name.isBlank() ||
                role.isBlank() ||
                qualifications.isEmpty()
            ) {
                showMessage("Please enter valid staff details.")
                return null
            }

            return Staff(
                staffId = id,
                name = name,
                role = role,
                qualifications = qualifications,
                available = availableBox.isSelected
            )
        }

        staffTable.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting && staffTable.selectedRow >= 0) {
                val row = staffTable.convertRowIndexToModel(
                    staffTable.selectedRow
                )

                val staff = staffList[row]

                idField.text = staff.staffId.toString()
                nameField.text = staff.name
                roleField.text = staff.role
                qualificationsField.text =
                    staff.qualifications.joinToString(", ")
                availableBox.isSelected = staff.available
            }
        }

        save.addActionListener {
            val staff = readStaff()
                ?: return@addActionListener

            if (staffList.any {
                    it.staffId == staff.staffId
                }) {
                showMessage("Staff ID already exists.")
                return@addActionListener
            }

            staffList.add(staff)
            refresh()
            clearFields()

            showMessage("Staff saved successfully!")
        }

        edit.addActionListener {
            val selected = staffTable.selectedRow

            if (selected < 0) {
                showMessage("Please select a staff member to edit.")
                return@addActionListener
            }

            val row = staffTable.convertRowIndexToModel(selected)
            val updated = readStaff()
                ?: return@addActionListener

            if (staffList.withIndex().any {
                    it.index != row &&
                            it.value.staffId == updated.staffId
                }) {
                showMessage("Staff ID already exists.")
                return@addActionListener
            }

            staffList[row] = updated
            refresh()
            clearFields()

            showMessage("Staff updated successfully!")
        }

        delete.addActionListener {
            val selected = staffTable.selectedRow

            if (selected < 0) {
                showMessage("Please select a staff member to delete.")
                return@addActionListener
            }

            val row = staffTable.convertRowIndexToModel(selected)

            if (confirmDeletion(
                    "Are you sure you want to delete this staff member?"
                )
            ) {
                staffList.removeAt(row)
                refresh()
                clearFields()
                showMessage("Staff deleted successfully!")
            }
        }

        clear.addActionListener {
            clearFields()
        }

        return panel
    }

    // =====================================
    // CARE PLANS GUI
    // =====================================

    private fun createCarePlansPanel(): JPanel {
        val panel = JPanel(BorderLayout(10, 10))

        val title = JLabel(
            "Animal Care Plans",
            SwingConstants.CENTER
        )

        val info = JLabel(
            "Care plans will be displayed here after generation."
        )

        val model = newTableModel(
            "Animal ID",
            "Care Type",
            "Description",
            "Scheduled Time",
            "Assigned Staff",
            "Status"
        )

        val table = JTable(model)
        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        )

        val top = JPanel(BorderLayout(5, 5))
        top.add(title, BorderLayout.NORTH)
        top.add(info, BorderLayout.SOUTH)

        panel.add(top, BorderLayout.NORTH)
        panel.add(JScrollPane(table), BorderLayout.CENTER)

        return panel
    }
}

fun main() {
    SwingUtilities.invokeLater {
        RescueManagementGUI().isVisible = true
    }
}
