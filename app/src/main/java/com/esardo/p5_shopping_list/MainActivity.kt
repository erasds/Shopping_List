package com.esardo.p5_shopping_list

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.esardo.p5_shopping_list.adapters.ProductAdapter
import com.esardo.p5_shopping_list.database.MyDao
import com.esardo.p5_shopping_list.database.ProductDatabase
import com.esardo.p5_shopping_list.database.entities.ProductEntity
import com.esardo.p5_shopping_list.databinding.ActivityMainBinding
import com.esardo.p5_shopping_list.viewmodel.ProductViewModel
import com.opencsv.CSVReader
import kotlinx.android.synthetic.main.item_product.*
import kotlinx.android.synthetic.main.new_product_dialog.view.*
import java.io.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var recyclerView: RecyclerView
    var productMutableList: MutableList<ProductEntity> = mutableListOf()

    private lateinit var productViewModel: ProductViewModel

    lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).also {
            binding = it
        }.root)

        setSupportActionBar(binding.toolbar)

        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        // Everytime that the activity starts it will get all the products...
        productViewModel.getAllProducts()

        // ...and also it will make the sum of it's total prices and bind it to the totalPurchase TextView
        productViewModel.productListLD.observe(this){
            productMutableList.clear()
            productMutableList.addAll(it)
            var totalPurchase = productMutableList.sumOf {it.totalPrice}
            binding.tvTotalPurchase.text = totalPurchase.toString()
            recyclerView.adapter?.notifyDataSetChanged()
        }

        productViewModel.updateProductLD.observe(this){ productUpdated ->
            if(productUpdated == null){
                showMessage("Error updating product")
            }
        }

        // Removes the product at the id position
        productViewModel.deleteProductLD.observe(this){ id ->
            if(id != -1){
                val product = productMutableList.filter {
                    it.id == id
                }[0]
                val pos = productMutableList.indexOf(product)
                productMutableList.removeAt(pos)
                recyclerView.adapter?.notifyItemRemoved(pos)
            }else{
                showMessage("Error deleting product")
            }
        }

        productViewModel.insertProductLD.observe(this){
            productMutableList.add(it)
            recyclerView.adapter?.notifyItemInserted(productMutableList.size)
        }

        // When floating action button is clicked the activity creates a dialog
        binding.fabAdd.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.new_product_dialog, null)

            builder.setView(view)

            val dialog = builder.create()

            dialog.show()

            // When the user clicks on Add button and if the name isn't empty...
            view.btnAdd.setOnClickListener {
                val pName = view.etName.text.toString()
                if (pName != ""){
                    val pQuantity =
                        if(view.etQuantity.text.toString() == "") { 0 } else { view.etQuantity.text.toString().toInt() }
                    var pUnitPrice =
                        if(view.etUnitPrice.text.toString() == "") { 0.00 } else { view.etUnitPrice.text.toString().toDouble() }
                    pUnitPrice = String.format("%.2f", pUnitPrice).toDouble()
                    val pTotalPrice = String.format("%.2f", (pUnitPrice * pQuantity)).toDouble()
                    // ...saves the data of the EditText into variables and send them as attributes to the add function
                    productViewModel.add(pName, pQuantity, pUnitPrice, pTotalPrice)
                    // Calculates the total price of the product inserted and shows it
                    var totalPurchase =
                        if(binding.tvTotalPurchase.text.toString() == "") { 0.00 } else { binding.tvTotalPurchase.text.toString().toDouble() }
                    // Adds the product price to the total purchase price
                    totalPurchase += pTotalPrice
                    binding.tvTotalPurchase.text = totalPurchase.toString()
                    // Closes the dialog
                    dialog.cancel()
                }else{
                    showMessage("You must insert Product Name")
                }

            }
        }

        setUpRecyclerView()
    }

//region Options Menu
    // To make a backup of the database
    private fun exportCSV() {
        // First thing I do is search for the folder's path
        val path = this.getExternalFilesDir(null)
        val folder = File(path, "RoomDbBackup")
        var isFolderCreated = false

        // If folder doesn't exists it has to create it
        if (!folder.exists()) {
            isFolderCreated = folder.mkdir()
        }
        // I define the name of the backup file and attach it to the folder's path
        val csvFileName = "RoomDB_Backup.csv"
        val filePathAndName: String = "$folder/$csvFileName"
        val db: ProductDatabase = Room.databaseBuilder(applicationContext, ProductDatabase::class.java, "shoppinglist-db")
            .allowMainThreadQueries().build()
        val myDao: MyDao = db.productDao()
        val products: List<ProductEntity> = myDao.getAllProducts()

        try {
            val fileWriter = FileWriter(filePathAndName)
            // Writes a line in the csv file for every product in the database
            for (i in products.indices) {
                fileWriter.append("" + products[i].id)
                fileWriter.append(",")
                fileWriter.append("" + products[i].name)
                fileWriter.append(",")
                fileWriter.append("" + products[i].quantity)
                fileWriter.append(",")
                fileWriter.append("" + products[i].unitPrice)
                fileWriter.append(",")
                fileWriter.append("" + products[i].totalPrice)
                fileWriter.append("\n")
            }
            fileWriter.flush()
            fileWriter.close()
            showMessage("Backup has been completed")
        } catch (e: Exception) {
            Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    // To restore the database backup
    private fun importCSV() {
        // First I search the backup file
        val path = this.getExternalFilesDir(null)
        val folder = File(path, "RoomDbBackup")
        val filePathAndName = "$folder/RoomDB_Backup.csv"
        val csvFile = File(filePathAndName)

        //If it exists
        if (csvFile.exists()) {
            try {
                val csvReader = CSVReader(FileReader(csvFile.absoluteFile))
                var nextLine: Array<String>? = csvReader.readNext()
                // I read every line of the file...
                while (nextLine != null) {
                    val id = nextLine[0]
                    val name = nextLine[1]
                    val quantity = nextLine[2]
                    val unitPrice = nextLine[3]
                    val totalPrice = nextLine[4]
                    val db: ProductDatabase = Room.databaseBuilder(applicationContext, ProductDatabase::class.java, "shoppinglist-db")
                        .allowMainThreadQueries().build()
                    val myDao: MyDao = db.productDao()
                    // ...and insert the data into the database one by one
                    myDao.addProduct(ProductEntity(
                        id = id.toInt(),
                        name = name,
                        quantity = quantity.toInt(),
                        unitPrice = unitPrice.toDouble(),
                        totalPrice = totalPrice.toDouble())
                    )
                    nextLine = csvReader.readNext()
                }
                finish()
                // Restart the activity
                overridePendingTransition(0, 0)
                startActivity(intent)
                overridePendingTransition(0, 0)
                showMessage("Products has been restored")
            } catch (e: Exception) {
                Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            showMessage("No Backup found")
        }
    }

    // To delete all products
    private fun deleteAll(){
        val db: ProductDatabase = Room.databaseBuilder(applicationContext, ProductDatabase::class.java, "shoppinglist-db")
            .allowMainThreadQueries().build()
        db.clearAllTables()
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // When the id of the option is...
        return when (item.itemId) {
            R.id.deleteAll -> {
                // ...calls it's function and restart activity
                deleteAll()
                startActivity(intent)
                overridePendingTransition(0, 0)
                return true
            }
            R.id.backup -> {
                // ...calls it's function
                exportCSV()
                return true
            }
            R.id.restore -> {
                // ...first deletes all the products to avoid db errors with Unique key,
                // then calls it's function
                deleteAll()
                importCSV()
                onResume()
                return true

            }
            else -> {
                return false
            }
        }
    }
//endregion

//region Crud functions
    // To update a product
    private fun updateProduct(productEntity: ProductEntity) {
        // Opens the same dialog that uses to insert products...
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.new_product_dialog, null)

        builder.setView(view)

        val dialog = builder.create()

        // ...but changes the button dialog to Edit and it's functionality
        view.btnAdd.text = "EDIT"
        // Also has to show the product's data at the EditText
        var pName = productEntity.name
        view.etName.setText(pName)
        view.etName.isEnabled = false
        var pQuantity = productEntity.quantity
        view.etQuantity.setText(pQuantity.toString())
        var pUnitPrice = productEntity.unitPrice
        view.etUnitPrice.setText(pUnitPrice.toString())

        dialog.show()

        view.btnAdd.setOnClickListener {
            var totalPurchase =
                if(binding.tvTotalPurchase.text.toString() == "") { 0.00 } else { binding.tvTotalPurchase.text.toString().toDouble() }
            // Just in case I rest the price of the product to the totalPurchase
            totalPurchase -= pUnitPrice * pQuantity
            val newQuantity =
                if(view.etQuantity.text.toString() == "") { 0 } else { view.etQuantity.text.toString().toInt() }
            var newUnitPrice =
                if(view.etUnitPrice.text.toString() == "") { 0.00 } else { view.etUnitPrice.text.toString().toDouble() }
            newUnitPrice = String.format("%.2f", newUnitPrice).toDouble()
            val newTotalPrice = String.format("%.2f", (newUnitPrice * newQuantity)).toDouble()
            productEntity.quantity = newQuantity
            productEntity.unitPrice = newUnitPrice
            productEntity.totalPrice = newTotalPrice
            // Updates the product with the new data and notifies the adapter
            productViewModel.update(productEntity)
            adapter.notifyDataSetChanged()
            // Now I sum the total product price to the totalPurchase again
            totalPurchase += newTotalPrice
            binding.tvTotalPurchase.text = totalPurchase.toString()
            // Closes dialog
            dialog.cancel()
        }
    }

    // To delete a product
    private fun deleteProduct(productEntity: ProductEntity) {
        var totalPurchase =
            if(binding.tvTotalPurchase.text.toString() == "") { 0.00 } else { binding.tvTotalPurchase.text.toString().toDouble() }
        // If there's more than one unit of the product I rest 1 to the quantity
        if( productEntity.quantity > 1){
            productEntity.quantity--
            tvQuantity.text = productEntity.quantity.toString()
            // TotalProductPrice and TotalPurchase have to be calculated again
            var totalProductPrice = String.format("%.2f", (productEntity.unitPrice * productEntity.quantity)).toDouble()
            tvTotalProductPrice.text = totalProductPrice.toString()
            productEntity.totalPrice = tvTotalProductPrice.text.toString().toDouble()
            adapter.notifyDataSetChanged()
            totalPurchase -= productEntity.unitPrice
            binding.tvTotalPurchase.text = totalPurchase.toString()
        }else{
            // If there's only one unit I delete the product from the database
            productViewModel.delete(productEntity)
            // I rest it's price from the totalPurchase
            totalPurchase -= productEntity.unitPrice
            binding.tvTotalPurchase.text = totalPurchase.toString()
        }
    }
//endregion

//region Other functions
    fun setUpRecyclerView() {
        adapter = ProductAdapter(productMutableList, { productEntity ->  updateProduct(productEntity) }, { productEntity -> deleteProduct(productEntity) })
        recyclerView = binding.rvItems
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
//endregion
}
