
package org.example

import java.awt.BorderLayout
import java.awt.GridLayout
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

class AnimalAllocationPanel(
    private val manager: AnimalAdmissionManager,
    private val facilities: List<Facility>
) : JPanel(BorderLayout(10, 10)) {

    private val animalIdField = JTextField()
    private val facilityIdField = JTextField()
    private val allocationIdField = JTextField()
    private val allocationDateField = JTextField()
    private val rehabilitationAnimalIdField = JTextField()
    private val rehabilitationStatusField = JTextField()
    private val rehabilitationCaseField = JTextField()
    private val resourceRequirementsField = JTextField()

    private val tableModel = object : DefaultTableModel(
        arrayOf(
            "Allocation ID",
            "Animal ID",
            "Facility",
            "Date",
            "Active"
        ),
        0
    ) {
        override fun isCellEditable(
            row: Int,
            column: Int
        ): Boolean = false
    }

    private val allocationTable = JTable(tableModel)

    init {
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        val formPanel = JPanel(GridLayout(8, 2, 10, 10))

        formPanel.add(JLabel("Animal ID:"))
        formPanel.add(animalIdField)

        formPanel.add(JLabel("Facility ID:"))
        formPanel.add(facilityIdField)

        formPanel.add(JLabel("Allocation ID:"))
        formPanel.add(allocationIdField)

        formPanel.add(JLabel("Allocation Date (YYYY-MM-DD):"))
        formPanel.add(allocationDateField)
        formPanel.add(JLabel("Rehabilitation Animal ID:"))
        formPanel.add(rehabilitationAnimalIdField)

        formPanel.add(JLabel("New Rehabilitation Status:"))
        formPanel.add(rehabilitationStatusField)
        formPanel.add(JLabel("Rehabilitation Case Reference:"))
        formPanel.add(rehabilitationCaseField)

        formPanel.add(JLabel("Resource Requirements:"))
        formPanel.add(resourceRequirementsField)

        val allocateButton = JButton("Allocate Animal")
        val cancelButton = JButton("Cancel Allocation")
        val refreshButton = JButton("Refresh")
        val updateStatusButton = JButton("Update Rehabilitation Status")
        val assignCaseButton = JButton("Assign Rehabilitation Case")
        val saveResourcesButton = JButton("Save Resource Requirements")

        val buttonPanel = JPanel()
        buttonPanel.add(allocateButton)
        buttonPanel.add(cancelButton)
        buttonPanel.add(refreshButton)
        buttonPanel.add(updateStatusButton)
        buttonPanel.add(assignCaseButton)
        buttonPanel.add(saveResourcesButton)

        val topPanel = JPanel(BorderLayout(10, 10))
        topPanel.add(formPanel, BorderLayout.CENTER)
        topPanel.add(buttonPanel, BorderLayout.SOUTH)

        allocationTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        )

        allocationTable.selectionModel.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) {
                val selectedRow = allocationTable.selectedRow

                if (selectedRow >= 0) {
                    val modelRow =
                        allocationTable.convertRowIndexToModel(selectedRow)

                    val allocation =
                        manager.getAllocations().getOrNull(modelRow)

                    if (allocation != null) {
                        allocationIdField.text =
                            allocation.allocationId.toString()

                        animalIdField.text =
                            allocation.animal.id.toString()

                        facilityIdField.text =
                            allocation.facility.facilityId.toString()

                        allocationDateField.text =
                            allocation.allocationDate
                    }
                }
            }
        }

        add(topPanel, BorderLayout.NORTH)
        add(JScrollPane(allocationTable), BorderLayout.CENTER)

        allocateButton.addActionListener {
            try {
                val animalId =
                    animalIdField.text.trim().toIntOrNull()
                        ?: throw IllegalArgumentException(
                            "Please enter a valid Animal ID."
                        )

                val facilityId =
                    facilityIdField.text.trim().toIntOrNull()
                        ?: throw IllegalArgumentException(
                            "Please enter a valid Facility ID."
                        )

                val allocationId =
                    allocationIdField.text.trim().toIntOrNull()
                        ?: throw IllegalArgumentException(
                            "Please enter a valid Allocation ID."
                        )

                require(
                    animalId > 0 &&
                            facilityId > 0 &&
                            allocationId > 0
                ) {
                    "IDs must be greater than zero."
                }

                val date = allocationDateField.text.trim()

                try {
                    LocalDate.parse(date)
                } catch (e: DateTimeParseException) {
                    throw IllegalArgumentException(
                        "Please enter a valid date in YYYY-MM-DD format."
                    )
                }

                val facility = facilities.find {
                    it.facilityId == facilityId
                } ?: throw IllegalArgumentException(
                    "Facility not found."
                )

                manager.allocateAnimal(
                    allocationId = allocationId,
                    animalId = animalId,
                    facility = facility,
                    allocationDate = date
                )
                assignCaseButton.addActionListener {
                    try {
                        val animalId = rehabilitationAnimalIdField.text.trim().toIntOrNull()
                            ?: throw IllegalArgumentException("Please enter a valid Animal ID.")

                        val caseReference = rehabilitationCaseField.text.trim()

                        require(animalId > 0) {
                            "Animal ID must be greater than zero."
                        }

                        require(caseReference.isNotBlank()) {
                            "Please enter a rehabilitation case reference."
                        }

                        manager.assignToRehabilitationCase(animalId, caseReference)

                        JOptionPane.showMessageDialog(
                            this,
                            "Animal assigned to rehabilitation case successfully!"
                        )

                    } catch (e: IllegalArgumentException) {
                        JOptionPane.showMessageDialog(
                            this,
                            e.message ?: "Unable to assign rehabilitation case.",
                            "Assignment Error",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
                refreshTable()

                JOptionPane.showMessageDialog(
                    this,
                    "Animal allocated successfully!"
                )

            } catch (e: IllegalArgumentException) {
                JOptionPane.showMessageDialog(
                    this,
                    e.message ?: "Invalid allocation details.",
                    "Allocation Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }

        cancelButton.addActionListener {
            try {
                val allocationId =
                    allocationIdField.text.trim().toIntOrNull()
                        ?: throw IllegalArgumentException(
                            "Please enter a valid Allocation ID."
                        )

                val allocation = manager.getAllocations().find {
                    it.allocationId == allocationId
                } ?: throw IllegalArgumentException(
                    "Allocation not found."
                )

                require(allocation.active) {
                    "This allocation is already inactive."
                }

                manager.cancelAllocation(allocationId)

                refreshTable()

                JOptionPane.showMessageDialog(
                    this,
                    "Allocation cancelled successfully!"
                )

            } catch (e: IllegalArgumentException) {
                JOptionPane.showMessageDialog(
                    this,
                    e.message ?: "Invalid allocation ID.",
                    "Cancellation Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }

        refreshButton.addActionListener {
            refreshTable()
        }

        updateStatusButton.addActionListener {
            val animalId = rehabilitationAnimalIdField.text.trim().toIntOrNull()
            if (animalId == null || animalId <= 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid Animal ID.",
                    "Invalid Animal ID",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }
            val newStatus = rehabilitationStatusField.text.trim()

            if (newStatus.isBlank()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please enter a rehabilitation status.",
                    "Invalid Status",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }
            try {
                manager.updateRehabilitationStatus(animalId, newStatus)

                JOptionPane.showMessageDialog(
                    this,
                    "Rehabilitation status updated successfully!"
                )
            } catch (e: IllegalArgumentException) {
                JOptionPane.showMessageDialog(
                    this,
                    e.message ?: "Unable to update rehabilitation status.",
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }


        assignCaseButton.addActionListener {
            try {
                val animalId = rehabilitationAnimalIdField.text.trim().toIntOrNull()
                    ?: throw IllegalArgumentException("Please enter a valid Animal ID.")

                val caseReference = rehabilitationCaseField.text.trim()

                require(animalId > 0) {
                    "Animal ID must be greater than zero."
                }

                require(caseReference.isNotBlank()) {
                    "Please enter a rehabilitation case reference."
                }

                manager.assignToRehabilitationCase(animalId, caseReference)

                JOptionPane.showMessageDialog(
                    this,
                    "Animal assigned to rehabilitation case successfully!"
                )
            } catch (e: IllegalArgumentException) {
                JOptionPane.showMessageDialog(
                    this,
                    e.message ?: "Unable to assign rehabilitation case.",
                    "Assignment Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }

        saveResourcesButton.addActionListener {
            try {
                val animalId = rehabilitationAnimalIdField.text.trim().toIntOrNull()
                    ?: throw IllegalArgumentException("Please enter a valid Animal ID.")

                require(animalId > 0) {
                    "Animal ID must be greater than zero."
                }

                val requirements = resourceRequirementsField.text.trim()

                manager.recordResourceRequirements(animalId, requirements)

                JOptionPane.showMessageDialog(
                    this,
                    "Resource requirements saved successfully!"
                )
            } catch (e: IllegalArgumentException) {
                JOptionPane.showMessageDialog(
                    this,
                    e.message ?: "Unable to save resource requirements.",
                    "Resource Requirements Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
        refreshTable()
    }

    private fun refreshTable() {
        tableModel.rowCount = 0

        manager.getAllocations().forEach { allocation ->
            tableModel.addRow(
                arrayOf<Any>(
                    allocation.allocationId,
                    allocation.animal.id,
                    allocation.facility.name,
                    allocation.allocationDate,
                    allocation.active
                )
            )
        }
    }
}
