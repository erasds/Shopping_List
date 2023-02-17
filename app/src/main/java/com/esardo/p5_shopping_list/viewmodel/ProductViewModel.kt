package com.esardo.p5_shopping_list.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.esardo.p5_shopping_list.database.MyDao
import com.esardo.p5_shopping_list.database.ProductDatabase
import com.esardo.p5_shopping_list.database.entities.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductViewModel(application: Application): AndroidViewModel(application) {
    val context = application

    var myDao: MyDao = ProductDatabase.getInstance(context)

    val productListLD: MutableLiveData<MutableList<ProductEntity>> = MutableLiveData()
    val updateProductLD: MutableLiveData<ProductEntity?> = MutableLiveData()
    val deleteProductLD: MutableLiveData<Int> = MutableLiveData()
    val insertProductLD: MutableLiveData<ProductEntity> = MutableLiveData()

    // To get all products
    fun getAllProducts(){
        viewModelScope.launch(Dispatchers.IO) {
            productListLD.postValue(myDao.getAllProducts())
        }
    }

    // To insert a product, it makes a call to the addProduct function that inserts the data into the database
    // I pass as parameters the data of the EditText filled by the user
    fun add(product:String, quantity:Int, unitPrice:Double, totalPrice:Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = myDao.addProduct(ProductEntity(name = product, quantity = quantity, unitPrice = unitPrice, totalPrice = totalPrice))
            val recoveryProduct = myDao.getProductById(id)
            insertProductLD.postValue(recoveryProduct)
        }
    }

    // To delete a product
    fun delete(product:ProductEntity){
        viewModelScope.launch(Dispatchers.IO) {
            val res = myDao.deleteProduct(product)
            if(res>0){
                deleteProductLD.postValue(product.id)
            }else{
                deleteProductLD.postValue(-1)
            }
        }
    }

    // To update a product
    fun update(product:ProductEntity){
        viewModelScope.launch(Dispatchers.IO) {
            val res = myDao.updateProduct(product)
            if(res>0)
                updateProductLD.postValue(product)
            else
                updateProductLD.postValue(null)
        }
    }
}