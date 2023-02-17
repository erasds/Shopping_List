package com.esardo.p5_shopping_list.database

import com.esardo.p5_shopping_list.database.entities.ProductEntity

interface MyDao {
    fun getAllProducts(): MutableList<ProductEntity>

    fun addProduct(productEntity : ProductEntity):Long //Id of the new product

    fun getProductById(id: Long): ProductEntity

    fun updateProduct(productEntity: ProductEntity):Int //Number of affected rows

    fun deleteProduct(productEntity: ProductEntity):Int //Number of affected rows
}