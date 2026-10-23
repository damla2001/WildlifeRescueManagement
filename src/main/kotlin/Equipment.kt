package org.example

class Equipment(
    val equipmentId: Int,
    val equipmentType: String,
    var quantity: Int,
    val location: String,
    var available: Boolean
)   {
    init {
        require(quantity >= 0) {
            "Equipment quantity cannot be negative"
        }
    }
}


