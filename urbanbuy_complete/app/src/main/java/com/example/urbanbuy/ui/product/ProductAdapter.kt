package com.example.urbanbuy.ui.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.urbanbuy.databinding.ItemProductBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var items: MutableList<Product>,
    private val onMenuClick: (Product, ItemProductBinding) -> Unit,
    private val onAddToCart: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    class VH(val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]

        // Nombre y precio
        val formato = NumberFormat.getNumberInstance(Locale("es", "CO"))
        holder.b.tvName.text = p.name
        holder.b.tvPrice.text = "$" + formato.format(p.price)

        Glide.with(holder.itemView.context)
            .load(p.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.b.imgProduct)


        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        if (p.ownerId != currentUid) {
            holder.b.btnMenu.visibility = View.GONE
        } else {
            holder.b.btnMenu.visibility = View.VISIBLE
        }

        // Botón menú
        holder.b.btnMenu.setOnClickListener {
            onMenuClick(p, holder.b)
        }

        // Botón agregar al carrito
        holder.b.btnAddToCart.setOnClickListener {
            onAddToCart(p)
        }
    }

    override fun getItemCount(): Int = items.size

    fun update(list: List<Product>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}
