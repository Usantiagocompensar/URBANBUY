package com.example.urbanbuy.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.urbanbuy.databinding.ItemCartBinding
import com.example.urbanbuy.ui.product.Product
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private var items: MutableList<Product>,
    private val onRemove: (Product) -> Unit
) : RecyclerView.Adapter<CartAdapter.VH>() {

    class VH(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]

        // ------- Formato COP -------
        fun formatCOP(value: Double): String {
            val localeCO = Locale("es", "CO")
            val format = NumberFormat.getCurrencyInstance(localeCO)
            format.minimumFractionDigits = 0
            return format.format(value)
        }

        // Imagen
        Glide.with(holder.itemView.context)
            .load(p.imageUrl)
            .into(holder.b.imgProduct)

        // Nombre
        holder.b.tvName.text = p.name

        // Precio unitario ——— CORREGIDO (Int → Double)
        holder.b.tvPrice.text = formatCOP(p.price.toDouble())

        // Cantidad
        val qty = p.quantity ?: 1
        holder.b.tvQty.text = "x$qty"

        // Subtotal ——— CORREGIDO (Int → Double)
        val subtotal = (p.price * qty).toDouble()
        holder.b.tvSubtotal.text = "Subtotal: ${formatCOP(subtotal)}"

        // Estado
        holder.b.tvEstado.text = if (p.price > 0) "Tiene precio" else "Sin precio"

        // Eliminar
        holder.b.btnRemove.setOnClickListener { onRemove(p) }
    }

    override fun getItemCount(): Int = items.size

    fun update(list: List<Product>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}



