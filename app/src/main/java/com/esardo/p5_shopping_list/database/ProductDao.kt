package com.esardo.p5_shopping_list.database

import androidx.room.*
import com.esardo.p5_shopping_list.database.entities.ProductEntity

@Dao
interface ProductDao:MyDao {
    @Query("SELECT * FROM product_entity")
    override fun getAllProducts(): MutableList<ProductEntity>

    @Insert
    override fun addProduct(productEntity : ProductEntity):Long

    @Query("SELECT * FROM product_entity WHERE id LIKE :id")
    override fun getProductById(id: Long): ProductEntity

    @Update
    override fun updateProduct(productEntity: ProductEntity):Int

    @Delete
    override fun deleteProduct(productEntity: ProductEntity):Int
}