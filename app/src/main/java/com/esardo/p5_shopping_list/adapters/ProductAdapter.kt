package com.esardo.p5_shopping_list.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.esardo.p5_shopping_list.R
import com.esardo.p5_shopping_list.database.entities.ProductEntity

class ProductAdapter(
    private val productList: List<ProductEntity>,
    private val updateProduct: (ProductEntity) -> Unit,
    private val deleteProduct: (ProductEntity) -> Unit) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_product, parent, false))
    }

    // To update or delete the product that belongs to the position sended
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = productList[position]
        holder.bind(item, updateProduct, deleteProduct)
    }

    override fun getItemCount() = productList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvQuantity = view.findViewById<TextView>(R.id.tvQuantity)
        val tvUnitPrice = view.findViewById<TextView>(R.id.tvUnitPrice)
        val tvTotalProductPrice = view.findViewById<TextView>(R.id.tvTotalProductPrice)
        val ivTrash = view.findViewById<ImageView>(R.id.ivDelete)

        // Binds the MainActivity elements to it's value
        fun bind(product: ProductEntity, updateProduct: (ProductEntity) -> Unit, deleteProduct: (ProductEntity) -> Unit) {
            tvName.text = product.name
            tvQuantity.text = product.quantity.toString()
            tvUnitPrice.text = product.unitPrice.toString()
            tvTotalProductPrice.text = product.totalPrice.toString()

            // When the user clicks on any part of the item the function updateProduct will be called
            itemView.setOnClickListener { updateProduct(product) }
            // When the user clicks on trash icon the function deleteProduct will be called
            ivTrash.setOnClickListener { deleteProduct(product) }
        }
    }
}
