package org.example
//Aidan GUI tab for resource matching inside main application
import java.awt.BorderLayout
import java.awt.GridLayout
import java.util.ServiceLoader
import javax.swing.*
import javax.swing.table.DefaultTableModel

class ResourceMatchingPanel(
    private val manager: AnimalAdmissionManager,
    private val facilities: List<Facility>,
    private val equipments: List<Equipment>,
    private val staffList: List<Staff>
) : JPanel(BorderLayout(10, 10)) {

    // Input fields
    private val animalIdField = JTextField()
    private val enclosureField = JTextField()
    private val minimumSpaceField = JTextField("1")
    private val equipmentField = JTextField()
    private val quantityField = JTextField("1")
    private val qualificationField = JTextField()

    // Results table
    private val tableModel = object : DefaultTableModel(
        arrayOf("Facility", "Equipment", "Staff"), 0
    ) {
        override fun isCellEditable(
            row: Int,
            column: Int
        ): Boolean = false
    }

    private val resultsTable = JTable(tableModel)

    init {
        // Create the input form
        val form = JPanel(GridLayout(6, 2, 10, 10))

        form.border = BorderFactory.createTitledBorder(
            "Resource Matching Requirements"
        )

        form.add(JLabel("Animal ID:"))
        form.add(animalIdField)

        form.add(JLabel("Required Enclosure:"))
        form.add(enclosureField)

        form.add(JLabel("Minimum Available Spaces:"))
        form.add(minimumSpaceField)

        form.add(JLabel("Required Equipment:"))
        form.add(equipmentField)

        form.add(JLabel("Minimum Equipment Quantity:"))
        form.add(quantityField)

        form.add(JLabel("Required Staff Qualification:"))
        form.add(qualificationField)

        val matchButton = JButton("Find Matching Resources")

        val topPanel = JPanel(BorderLayout(10, 10))
        topPanel.add(form, BorderLayout.CENTER)
        topPanel.add(matchButton, BorderLayout.SOUTH)

        add(topPanel, BorderLayout.NORTH)
        add(JScrollPane(resultsTable), BorderLayout.CENTER)

        matchButton.addActionListener {
            findMatches()
        }
    }

    // Find matching resource combinations
    private fun findMatches() {

        tableModel.rowCount = 0

        val animalId = animalIdField.text.trim().toIntOrNull()
        val enclosure = enclosureField.text.trim()
        val minimumSpace = minimumSpaceField.text.trim().toIntOrNull()
        val equipment = equipmentField.text.trim()
        val quantity = quantityField.text.trim().toIntOrNull()
        val qualification = qualificationField.text.trim()

        // Validate user input
        if (
            animalId == null || animalId <= 0 ||
            minimumSpace == null || minimumSpace <= 0 ||
            quantity == null || quantity <= 0 ||
            enclosure.isBlank() ||
            equipment.isBlank() ||
            qualification.isBlank()
        ) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter valid resource requirements."
            )
            return
        }

        // Find the registered animal
        val animal = manager.getRegisteredAnimals().find {
            it.id == animalId
        }

        if (animal == null) {
            JOptionPane.showMessageDialog(
                this,
                "Animal not found."
            )
            return
        }

        // Load the Scala resource matcher
        val matcher = ServiceLoader
            .load(ResourceMatchingService::class.java)
            .findFirst()
            .orElse(null)

        if (matcher == null) {
            JOptionPane.showMessageDialog(
                this,
                "Resource matcher could not be loaded."
            )
            return
        }

        // Pass Kotlin data to the Scala algorithm
        val matches = matcher.matchResources(
            animal,
            facilities,
            equipments,
            staffList,
            enclosure,
            minimumSpace,
            equipment,
            quantity,
            qualification
        )

        // Display matching combinations
        matches.forEach { match ->
            tableModel.addRow(
                arrayOf<Any>(
                    match.facility.name,
                    match.equipment.equipmentType,
                    match.staff.name
                )
            )
        }

        JOptionPane.showMessageDialog(
            this,
            "Suitable combinations found: ${matches.size}"
        )
    }
}