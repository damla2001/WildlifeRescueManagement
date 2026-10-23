package org.example

import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

class RescueManagementGUI : JFrame() {

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

    // Store all case information, including fields not shown in the table
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

    private val cases = mutableListOf<CaseRecord>()
    private val rescueCases = mutableListOf<RescueCase>()

    private val tableModel = object : DefaultTableModel(
        arrayOf(
            "Case Reference", "Date", "Location",
            "Species", "Animals", "Condition", "Status"
        ), 0
    ) {
        override fun isCellEditable(row: Int, column: Int): Boolean = false
    }

    private val table = JTable(tableModel)

    init {
        title = "Wildlife Rescue Management System"
        setSize(1100, 750)
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)

        val formPanel = JPanel(GridLayout(10, 2, 10, 10))
        formPanel.border = BorderFactory.createTitledBorder(
            "Rescue Case Details"
        )

        fun addField(label: String, field: JTextField) {
            formPanel.add(JLabel(label))
            formPanel.add(field)
        }

        addField("Case Reference:", caseReferenceField)
        addField("Rescue Date:", rescueDateField)
        addField("Location:", locationField)
        addField("Animal Species:", speciesField)
        addField("Number of Animals:", animalCountField)
        addField("Reported Condition:", conditionField)
        addField("Initial Assessment:", assessmentField)
        addField("Required Care:", careField)
        addField("Case Status:", statusField)
        addField("Additional Notes:", notesField)

        val saveButton = JButton("Save Case")
        val editButton = JButton("Edit Case")
        val deleteButton = JButton("Delete Case")
        val clearButton = JButton("Clear Fields")

        val buttonPanel = JPanel(GridLayout(1, 4, 10, 10))
        buttonPanel.add(saveButton)
        buttonPanel.add(editButton)
        buttonPanel.add(deleteButton)
        buttonPanel.add(clearButton)

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        // Display selected case in the form
        table.selectionModel.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) {
                val selectedRow = table.selectedRow

                if (selectedRow >= 0) {
                    val modelRow = table.convertRowIndexToModel(selectedRow)
                    displayCase(cases[modelRow])
                }
            }
        }

        // Save a new case
        saveButton.addActionListener {
            val record = readForm()

            if (record != null) {
                if (cases.any {
                        it.reference.equals(
                            record.reference,
                            ignoreCase = true
                        )
                    }) {
                    showMessage("Case reference already exists.")
                } else {
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

                    repeat(record.animalCount) { index ->
                        val animal = Animal(
                            id = index + 1,
                            species = record.species,
                            condition = record.condition,
                            careRequirements = record.care,
                            rehabilitationStatus = record.status
                        )

                        rescueCase.addAnimal(animal)
                    }
                    rescueCases.add(rescueCase)
                    cases.add(record)

                    refreshTable()
                    clearFields()

                    showMessage("Rescue case added successfully!")
                }
            }
        }

        // Edit selected case
        editButton.addActionListener {
            val selectedRow = table.selectedRow

            if (selectedRow == -1) {
                showMessage("Please select a case to edit.")
            } else {
                val modelRow = table.convertRowIndexToModel(selectedRow)
                val record = readForm()

                if (record != null) {
                    val duplicate = cases.withIndex().any {
                        it.index != modelRow &&
                                it.value.reference.equals(
                                    record.reference,
                                    ignoreCase = true
                                )
                    }

                    if (duplicate) {
                        showMessage("Case reference already exists.")
                    } else {
                        cases[modelRow] = record
                        rescueCases[modelRow] = RescueCase(
                            caseReference = record.reference,
                            rescueDate = record.date,
                            location = record.location,
                            reportedCondition = record.condition,
                            status = record.status,
                            reportedAnimalCount = record.animalCount,
                            initialAssessment = record.assessment,
                            requiredCare = record.care,
                            additionalNotes = record.notes,
                            animals = rescueCases[modelRow].animals
                        )

                        val updatedCase = rescueCases[modelRow]
                        updatedCase.animals.forEach { animal ->
                            animal.species = record.species
                            animal.condition = record.condition
                            animal.careRequirements = record.care
                            animal.rehabilitationStatus = record.status
                        }

                        while (updatedCase.getAnimalCount() > record.animalCount) {
                            updatedCase.animals.removeAt(updatedCase.animals.lastIndex)
                        }

                        while (updatedCase.getAnimalCount() < record.animalCount) {
                            updatedCase.addAnimal(
                                Animal(
                                    id = (updatedCase.animals.maxOfOrNull { it.id } ?: 0) + 1,
                                    species = record.species,
                                    condition = record.condition,
                                    careRequirements = record.care,
                                    rehabilitationStatus = record.status
                                )
                            )
                        }


                        refreshTable()
                        clearFields()
                        showMessage("Rescue case updated successfully!")
                    }
                }
            }
        }

        // Delete selected case
        deleteButton.addActionListener {
            val selectedRow = table.selectedRow

            if (selectedRow == -1) {
                showMessage("Please select a case to delete.")
            } else {
                val modelRow = table.convertRowIndexToModel(selectedRow)

                val confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this rescue case?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
                )

                if (confirmation == JOptionPane.YES_OPTION) {
                    cases.removeAt(modelRow)
                    rescueCases.removeAt(modelRow)
                    refreshTable()
                    clearFields()
                    showMessage("Rescue case deleted successfully!")
                }
            }
        }

        // Clear form
        clearButton.addActionListener {
            clearFields()
        }

        val topPanel = JPanel(BorderLayout(10, 10))
        topPanel.add(formPanel, BorderLayout.CENTER)
        topPanel.add(buttonPanel, BorderLayout.SOUTH)

        // Create main tabs
        val tabbedPane = JTabbedPane()

// Rescue Cases tab
        val rescuePanel = JPanel(BorderLayout(10, 10))
        rescuePanel.add(topPanel, BorderLayout.NORTH)
        rescuePanel.add(JScrollPane(table), BorderLayout.CENTER)

        tabbedPane.addTab("Rescue Cases", rescuePanel)

// Other management tabs
        val facilityPanel = JPanel(BorderLayout(10, 10))

        val facilityForm = JPanel(GridLayout(7, 2, 10, 10))
        facilityForm.border = BorderFactory.createTitledBorder(
            "Facility Details"
        )

        val facilityIdField = JTextField()
        val facilityNameField = JTextField()
        val facilityLocationField = JTextField()
        val facilityCapacityField = JTextField()
        val suitableSpeciesField = JTextField()
        val availableFacilitiesField = JTextField()
        val facilityAvailableCheckBox = JCheckBox("Available", true)

        facilityForm.add(JLabel("Facility ID:"))
        facilityForm.add(facilityIdField)

        facilityForm.add(JLabel("Facility Name:"))
        facilityForm.add(facilityNameField)

        facilityForm.add(JLabel("Location:"))
        facilityForm.add(facilityLocationField)

        facilityForm.add(JLabel("Capacity:"))
        facilityForm.add(facilityCapacityField)

        facilityForm.add(JLabel("Suitable Species:"))
        facilityForm.add(suitableSpeciesField)

        facilityForm.add(JLabel("Available Facilities:"))
        facilityForm.add(availableFacilitiesField)
        facilityForm.add(JLabel("Facility Availability:"))
        facilityForm.add(facilityAvailableCheckBox)

        // Facility management buttons
        val saveFacilityButton = JButton("Save Facility")
        val editFacilityButton = JButton("Edit Facility")
        val deleteFacilityButton = JButton("Delete Facility")
        val clearFacilityButton = JButton("Clear Fields")

        val facilityButtonPanel = JPanel(GridLayout(1, 4, 10, 10))
        facilityButtonPanel.add(saveFacilityButton)
        facilityButtonPanel.add(editFacilityButton)
        facilityButtonPanel.add(deleteFacilityButton)
        facilityButtonPanel.add(clearFacilityButton)

// Facility table
        val facilityTableModel = object : DefaultTableModel(
            arrayOf(
                "ID", "Name", "Location", "Capacity",
                "Suitable Species", "Available Facilities", "Available"
            ), 0
        ) {
            override fun isCellEditable(row: Int, column: Int): Boolean = false
        }

        val facilities = mutableListOf<Facility>()
        val facilityTable = JTable(facilityTableModel)
        facilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        facilityTable.selectionModel.addListSelectionListener { event ->
            if (!event.valueIsAdjusting && facilityTable.selectedRow >= 0) {
                val row = facilityTable.convertRowIndexToModel(
                    facilityTable.selectedRow
                )

                val facility = facilities[row]

                facilityIdField.text = facility.facilityId.toString()
                facilityNameField.text = facility.name
                facilityLocationField.text = facility.location
                facilityCapacityField.text = facility.capacity.toString()
                suitableSpeciesField.text =
                    facility.suitableSpecies.joinToString(", ")
                availableFacilitiesField.text =
                    facility.availableFacilities.joinToString(", ")
                facilityAvailableCheckBox.isSelected = facility.available
            }
        }

// Arrange the form and buttons
        val facilityTopPanel = JPanel(BorderLayout(10, 10))
        facilityTopPanel.add(facilityForm, BorderLayout.CENTER)
        facilityTopPanel.add(facilityButtonPanel, BorderLayout.SOUTH)

        facilityPanel.add(facilityTopPanel, BorderLayout.NORTH)
        facilityPanel.add(JScrollPane(facilityTable), BorderLayout.CENTER)
        saveFacilityButton.addActionListener {
            val id = facilityIdField.text.trim().toIntOrNull()
            val name = facilityNameField.text.trim()
            val location = facilityLocationField.text.trim()
            val capacity = facilityCapacityField.text.trim().toIntOrNull()

            val species = suitableSpeciesField.text
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val features = availableFacilitiesField.text
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (id == null || id <= 0 ||
                name.isBlank() || location.isBlank() ||
                capacity == null || capacity <= 0 ||
                species.isEmpty()
            ) {
                showMessage("Please enter valid facility details.")
            } else if (facilities.any { it.facilityId == id }) {
                showMessage("Facility ID already exists.")
            } else {
                val facility = Facility(
                    facilityId = id,
                    name = name,
                    location = location,
                    capacity = capacity,
                    suitableSpecies = species,
                    available = facilityAvailableCheckBox.isSelected,
                    availableFacilities = features
                )

                facilities.add(facility)

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

                showMessage("Facility added successfully!")
            }

            clearFacilityButton.addActionListener {
                facilityIdField.text = ""
                facilityNameField.text = ""
                facilityLocationField.text = ""
                facilityCapacityField.text = ""
                suitableSpeciesField.text = ""
                availableFacilitiesField.text = ""

                facilityAvailableCheckBox.isSelected = true
                facilityTable.clearSelection()
            }

            // Edit selected facility
            editFacilityButton.addActionListener {

                val selectedRow = facilityTable.selectedRow

                if (selectedRow == -1) {
                    showMessage("Please select a facility to edit.")
                } else {
                    val modelRow = facilityTable.convertRowIndexToModel(selectedRow)

                    val id = facilityIdField.text.trim().toIntOrNull()
                    val name = facilityNameField.text.trim()
                    val location = facilityLocationField.text.trim()
                    val capacity = facilityCapacityField.text.trim().toIntOrNull()

                    val species = suitableSpeciesField.text
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val features = availableFacilitiesField.text
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    if (id == null || id <= 0 ||
                        name.isBlank() || location.isBlank() ||
                        capacity == null || capacity <= 0 ||
                        species.isEmpty()
                    ) {
                        showMessage("Please enter valid facility details.")

                    } else if (facilities.withIndex().any {
                            it.index != modelRow && it.value.facilityId == id
                        }) {
                        showMessage("Facility ID already exists.")

                    } else if (capacity < facilities[modelRow].currentAnimals) {
                        showMessage(
                            "Capacity cannot be lower than the number of allocated animals."
                        )

                    } else {
                        val previousFacility = facilities[modelRow]

                        val updatedFacility = Facility(
                            facilityId = id,
                            name = name,
                            location = location,
                            capacity = capacity,
                            suitableSpecies = species,
                            available = facilityAvailableCheckBox.isSelected,
                            availableFacilities = features
                        )

                        updatedFacility.currentAnimals = previousFacility.currentAnimals
                        facilities[modelRow] = updatedFacility

                        facilityTableModel.setValueAt(id, modelRow, 0)
                        facilityTableModel.setValueAt(name, modelRow, 1)
                        facilityTableModel.setValueAt(location, modelRow, 2)
                        facilityTableModel.setValueAt(capacity, modelRow, 3)
                        facilityTableModel.setValueAt(
                            species.joinToString(", "), modelRow, 4
                        )
                        facilityTableModel.setValueAt(
                            features.joinToString(", "), modelRow, 5
                        )
                        facilityTableModel.setValueAt(
                            updatedFacility.available, modelRow, 6
                        )

                        showMessage("Facility updated successfully!")
                    }
                }
            }
            // Delete selected facility
            deleteFacilityButton.addActionListener {
                val selectedRow = facilityTable.selectedRow

                if (selectedRow == -1) {
                    showMessage("Please select a facility to delete.")
                } else {
                    val modelRow = facilityTable.convertRowIndexToModel(selectedRow)
                    val facility = facilities[modelRow]

                    if (facility.currentAnimals > 0) {
                        showMessage("Cannot delete a facility with allocated animals.")
                    } else {
                        val confirmation = JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to delete this facility?",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION
                        )

                        if (confirmation == JOptionPane.YES_OPTION) {
                            facilities.removeAt(modelRow)
                            facilityTableModel.removeRow(modelRow)

// Clear all facility fields after deletion
                            facilityIdField.text = ""
                            facilityNameField.text = ""
                            facilityLocationField.text = ""
                            facilityCapacityField.text = ""
                            suitableSpeciesField.text = ""
                            availableFacilitiesField.text = ""

                            facilityAvailableCheckBox.isSelected = true
                            facilityTable.clearSelection()

                            showMessage("Facility deleted successfully!")
                        }
                    }
                }
            }


        }

        tabbedPane.addTab("Facilities", facilityPanel)


// Equipment management tab
        val equipmentPanel = JPanel(BorderLayout(10, 10))

// Equipment form
        val equipmentForm = JPanel(GridLayout(5, 2, 10, 10))
        equipmentForm.border = BorderFactory.createTitledBorder("Equipment Details")

// Input fields
        val equipmentIdField = JTextField()
        val equipmentTypeField = JTextField()
        val equipmentQuantityField = JTextField()
        val equipmentLocationField = JTextField()
        val equipmentAvailableCheckBox = JCheckBox("Available", true)

// Equipment ID
        equipmentForm.add(JLabel("Equipment ID:"))
        equipmentForm.add(equipmentIdField)

// Equipment Type
        equipmentForm.add(JLabel("Equipment Type:"))
        equipmentForm.add(equipmentTypeField)

// Quantity
        equipmentForm.add(JLabel("Quantity:"))
        equipmentForm.add(equipmentQuantityField)

// Location
        equipmentForm.add(JLabel("Location:"))
        equipmentForm.add(equipmentLocationField)

// Availability
        equipmentForm.add(JLabel("Equipment Availability:"))
        equipmentForm.add(equipmentAvailableCheckBox)


// Equipment management buttons
        val saveEquipmentButton = JButton("Save Equipment")
        val editEquipmentButton = JButton("Edit Equipment")
        val deleteEquipmentButton = JButton("Delete Equipment")
        val clearEquipmentButton = JButton("Clear Fields")

        val equipmentButtonPanel = JPanel(GridLayout(1, 4, 10, 10))
        equipmentButtonPanel.add(saveEquipmentButton)
        equipmentButtonPanel.add(editEquipmentButton)
        equipmentButtonPanel.add(deleteEquipmentButton)
        equipmentButtonPanel.add(clearEquipmentButton)

// Equipment table
        val equipmentTableModel = object : DefaultTableModel(
            arrayOf(
                "Equipment ID", "Equipment Type", "Quantity",
                "Location", "Available"
            ), 0
        ) {
            override fun isCellEditable(row: Int, column: Int): Boolean = false
        }

        val equipmentTable = JTable(equipmentTableModel)
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

// Arrange Equipment form and buttons
        val equipmentTopPanel = JPanel(BorderLayout(10, 10))
        equipmentTopPanel.add(equipmentForm, BorderLayout.CENTER)
        equipmentTopPanel.add(equipmentButtonPanel, BorderLayout.SOUTH)

// Add everything to Equipment panel
        equipmentPanel.add(equipmentTopPanel, BorderLayout.NORTH)
        equipmentPanel.add(JScrollPane(equipmentTable), BorderLayout.CENTER)

// Add Equipment tab

// Store equipment records while the application is running
        val equipments = mutableListOf<Equipment>()

// Save a new equipment record
        saveEquipmentButton.addActionListener {
            val id = equipmentIdField.text.trim().toIntOrNull()
            val type = equipmentTypeField.text.trim()
            val quantity = equipmentQuantityField.text.trim().toIntOrNull()
            val location = equipmentLocationField.text.trim()
            val available = equipmentAvailableCheckBox.isSelected

            if (id == null || id <= 0 ||
                type.isBlank() ||
                quantity == null || quantity < 0 ||
                location.isBlank()
            ) {
                showMessage("Please enter valid equipment details.")
            } else if (equipments.any { it.equipmentId == id }) {
                showMessage("Equipment ID already exists.")
            } else {
                val equipment = Equipment(
                    equipmentId = id,
                    equipmentType = type,
                    quantity = quantity,
                    location = location,
                    available = available
                )

                equipments.add(equipment)

                equipmentTableModel.addRow(
                    arrayOf<Any>(
                        equipment.equipmentId,
                        equipment.equipmentType,
                        equipment.quantity,
                        equipment.location,
                        equipment.available
                    )
                )

                showMessage("Equipment added successfully!")
            }
        }

        editEquipmentButton.addActionListener {
            val selectedRow = equipmentTable.selectedRow

            if (selectedRow == -1) {
                showMessage("Please select equipment to edit.")
            } else {
                val id = equipmentIdField.text.trim().toIntOrNull()
                val type = equipmentTypeField.text.trim()
                val quantity = equipmentQuantityField.text.trim().toIntOrNull()
                val location = equipmentLocationField.text.trim()
                val available = equipmentAvailableCheckBox.isSelected

                if (id == null || id <= 0 ||
                    type.isBlank() ||
                    quantity == null || quantity < 0 ||
                    location.isBlank()
                ) {
                    showMessage("Please enter valid equipment details.")
                } else {
                    val modelRow = equipmentTable.convertRowIndexToModel(selectedRow)

                    if (equipments.indices.any {
                            it != modelRow && equipments[it].equipmentId == id
                        }
                    ) {
                        showMessage("Equipment ID already exists.")
                    } else {
                        val updatedEquipment = Equipment(
                            equipmentId = id,
                            equipmentType = type,
                            quantity = quantity,
                            location = location,
                            available = available
                        )

                        equipments[modelRow] = updatedEquipment

                        equipmentTableModel.setValueAt(id, modelRow, 0)
                        equipmentTableModel.setValueAt(type, modelRow, 1)
                        equipmentTableModel.setValueAt(quantity, modelRow, 2)
                        equipmentTableModel.setValueAt(location, modelRow, 3)
                        equipmentTableModel.setValueAt(available, modelRow, 4)

                        showMessage("Equipment updated successfully!")
                    }
                }
            }
        }



        deleteEquipmentButton.addActionListener {
            val selectedRow = equipmentTable.selectedRow

            if (selectedRow == -1) {
                showMessage("Please select equipment to delete.")
            } else {
                val modelRow = equipmentTable.convertRowIndexToModel(selectedRow)

                val confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this equipment?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
                )

                if (confirm == JOptionPane.YES_OPTION) {
                    equipments.removeAt(modelRow)
                    equipmentTableModel.removeRow(modelRow)

                    equipmentIdField.text = ""
                    equipmentTypeField.text = ""
                    equipmentQuantityField.text = ""
                    equipmentLocationField.text = ""
                    equipmentAvailableCheckBox.isSelected = true

                    equipmentTable.clearSelection()

                    showMessage("Equipment deleted successfully!")
                }
            }
        }


        clearEquipmentButton.addActionListener {
            equipmentIdField.text = ""
            equipmentTypeField.text = ""
            equipmentQuantityField.text = ""
            equipmentLocationField.text = ""
            equipmentAvailableCheckBox.isSelected = true

            equipmentTable.clearSelection()
            equipmentIdField.requestFocusInWindow()
        }


        tabbedPane.addTab("Equipment", equipmentPanel)


        // ================================
        // STAFF MANAGEMENT
        // ================================

        val staffPanel = JPanel(BorderLayout(10, 10))

        // Staff form
        val staffFormPanel = JPanel(GridLayout(5, 2, 10, 10))
        staffFormPanel.border =
            BorderFactory.createTitledBorder("Staff Details")

        val staffIdField = JTextField()
        val staffNameField = JTextField()
        val staffRoleField = JTextField()
        val staffQualificationsField = JTextField()
        val staffAvailableCheckBox = JCheckBox("Available", true)

        staffFormPanel.add(JLabel("Staff ID:"))
        staffFormPanel.add(staffIdField)

        staffFormPanel.add(JLabel("Staff Name:"))
        staffFormPanel.add(staffNameField)

        staffFormPanel.add(JLabel("Role:"))
        staffFormPanel.add(staffRoleField)

        staffFormPanel.add(JLabel("Qualifications:"))
        staffFormPanel.add(staffQualificationsField)

        staffFormPanel.add(JLabel("Availability:"))
        staffFormPanel.add(staffAvailableCheckBox)

        // Staff buttons
        val staffButtonPanel = JPanel(GridLayout(1, 4, 10, 10))

        val saveStaffButton = JButton("Save Staff")
        val editStaffButton = JButton("Edit Staff")
        val deleteStaffButton = JButton("Delete Staff")
        val clearStaffButton = JButton("Clear Fields")

        staffButtonPanel.add(saveStaffButton)
        staffButtonPanel.add(editStaffButton)
        staffButtonPanel.add(deleteStaffButton)
        staffButtonPanel.add(clearStaffButton)

        // Staff table
        val staffTableModel = DefaultTableModel(
            arrayOf(
                "Staff ID",
                "Name",
                "Role",
                "Qualifications",
                "Available"
            ),
            0
        )

        val staffTable = JTable(staffTableModel)

        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        // Store staff records
        val staffList = mutableListOf<Staff>()

        // Combine form and buttons
        val staffTopPanel = JPanel(BorderLayout(10, 10))

        staffTopPanel.add(
            staffFormPanel,
            BorderLayout.CENTER
        )

        staffTopPanel.add(
            staffButtonPanel,
            BorderLayout.SOUTH
        )

        // Add components to Staff panel
        staffPanel.add(
            staffTopPanel,
            BorderLayout.NORTH
        )

        staffPanel.add(
            JScrollPane(staffTable),
            BorderLayout.CENTER
        )


        saveStaffButton.addActionListener {
            val id = staffIdField.text.trim().toIntOrNull()
            val name = staffNameField.text.trim()
            val role = staffRoleField.text.trim()
            val qualifications = staffQualificationsField.text
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val available = staffAvailableCheckBox.isSelected

            if (id == null || id <= 0 || name.isBlank() ||
                role.isBlank() || qualifications.isEmpty()
            ) {
                showMessage("Please enter valid staff details.")
            } else if (staffList.any { it.staffId == id }) {
                showMessage("Staff ID already exists.")
            } else {
                val newStaff = Staff(
                    staffId = id,
                    name = name,
                    role = role,
                    qualifications = qualifications,
                    available = available
                )

                staffList.add(newStaff)

                staffTableModel.addRow(
                    arrayOf<Any>(
                        id,
                        name,
                        role,
                        qualifications.joinToString(", "),
                        available
                    )
                )


                showMessage("Staff saved successfully!")
            }
        }


        editStaffButton.addActionListener {
            val selectedRow = staffTable.selectedRow

            if (selectedRow == -1) {
                showMessage("Please select a staff member to edit.")
            } else {
                val id = staffIdField.text.trim().toIntOrNull()
                val name = staffNameField.text.trim()
                val role = staffRoleField.text.trim()
                val qualifications = staffQualificationsField.text
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                val available = staffAvailableCheckBox.isSelected

                if (id == null || id <= 0 || name.isBlank() ||
                    role.isBlank() || qualifications.isEmpty()
                ) {
                    showMessage("Please enter valid staff details.")
                } else {
                    val modelRow = staffTable.convertRowIndexToModel(selectedRow)

                    if (staffList.indices.any {
                            it != modelRow && staffList[it].staffId == id
                        }
                    ) {
                        showMessage("Staff ID already exists.")
                    } else {
                        val updatedStaff = Staff(
                            staffId = id,
                            name = name,
                            role = role,
                            qualifications = qualifications,
                            available = available
                        )

                        staffList[modelRow] = updatedStaff

                        staffTableModel.setValueAt(id, modelRow, 0)
                        staffTableModel.setValueAt(name, modelRow, 1)
                        staffTableModel.setValueAt(role, modelRow, 2)
                        staffTableModel.setValueAt(
                            qualifications.joinToString(", "),
                            modelRow,
                            3
                        )
                        staffTableModel.setValueAt(available, modelRow, 4)

                        showMessage("Staff updated successfully!")
                    }
                }
            }
        }


        deleteStaffButton.addActionListener {
            val selectedRow = staffTable.selectedRow

            if (selectedRow == -1) {
                showMessage("Please select a staff member to delete.")
            } else {
                val confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this staff member?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
                )

                if (confirm == JOptionPane.YES_OPTION) {
                    val modelRow = staffTable.convertRowIndexToModel(selectedRow)

                    staffList.removeAt(modelRow)
                    staffTableModel.removeRow(modelRow)

                    staffIdField.text = ""
                    staffNameField.text = ""
                    staffRoleField.text = ""
                    staffQualificationsField.text = ""
                    staffAvailableCheckBox.isSelected = true

                    staffTable.clearSelection()

                    showMessage("Staff deleted successfully!")
                }
            }
        }



        clearStaffButton.addActionListener {
            staffIdField.text = ""
            staffNameField.text = ""
            staffRoleField.text = ""
            staffQualificationsField.text = ""
            staffAvailableCheckBox.isSelected = true

            staffTable.clearSelection()
            staffIdField.requestFocusInWindow()
        }

        // Add Staff tab
        tabbedPane.addTab("Staff", staffPanel)



        // CARE PLANS GUI
        val carePlansPanel = JPanel(BorderLayout(10, 10))

        val carePlansTitle = JLabel(
            "Animal Care Plans",
            SwingConstants.CENTER
        )

        val carePlansTableModel = DefaultTableModel(
            arrayOf(
                "Animal ID",
                "Care Type",
                "Description",
                "Scheduled Time",
                "Assigned Staff",
                "Status"
            ),
            0
        )

        val carePlansTable = JTable(carePlansTableModel)
        carePlansTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        )

        val carePlansInfoLabel = JLabel(
            "Care plans will be displayed here after generation."
        )

        val carePlansTopPanel = JPanel(BorderLayout(5, 5))
        carePlansTopPanel.add(carePlansTitle, BorderLayout.NORTH)
        carePlansTopPanel.add(carePlansInfoLabel, BorderLayout.SOUTH)

        carePlansPanel.add(carePlansTopPanel, BorderLayout.NORTH)
        carePlansPanel.add(
            JScrollPane(carePlansTable),
            BorderLayout.CENTER
        )

        tabbedPane.addTab("Care Plans", carePlansPanel)

// Add tabs to the main window
        layout = BorderLayout()
        add(tabbedPane, BorderLayout.CENTER)

    }

    private fun readForm(): CaseRecord? {
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
            date.isBlank() ||
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
                        "The number of animals must be greater than zero."
            )
            return null
        }

        val dateFormatter = DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT)

        try {
            LocalDate.parse(date, dateFormatter)
        } catch (e: DateTimeParseException) {
            showMessage("Please enter a valid date in YYYY-MM-DD format.")
            return null
        }

        return CaseRecord(
            reference, date, location, species, count,
            condition, assessment, care, status, notes
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

    private fun refreshTable() {
        tableModel.rowCount = 0

        cases.forEachIndexed { index, record ->
            tableModel.addRow(
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

    private fun clearFields() {
        caseReferenceField.text = ""
        rescueDateField.text = ""
        locationField.text = ""
        speciesField.text = ""
        animalCountField.text = ""
        conditionField.text = ""
        assessmentField.text = ""
        careField.text = ""
        statusField.text = ""
        notesField.text = ""
        table.clearSelection()
    }

    private fun showMessage(message: String) {
        JOptionPane.showMessageDialog(this, message)
    }
}

fun main() {
    SwingUtilities.invokeLater {
        RescueManagementGUI().isVisible = true
    }
}
