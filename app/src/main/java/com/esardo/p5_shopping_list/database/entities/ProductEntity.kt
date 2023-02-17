package com.esardo.p5_shopping_list.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_entity")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,
    var name:String = "",
    var quantity:Int = 0,
    var unitPrice:Double = 0.00,
    var totalPrice:Double = 0.00
)
