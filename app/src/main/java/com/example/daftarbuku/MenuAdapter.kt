package com.example.recyclerview

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.daftarbuku.DetailActivity
import com.example.daftarbuku.R
class MenuAdapter(private val listMenu: List<Menu>):
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>(){

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvNama = itemView.findViewById<TextView>(R.id.tv_nama)
        val tvHarga = itemView.findViewById<TextView>(R.id.tv_harga)
        val tvDeskripsi = itemView.findViewById<TextView>(R.id.tv_deskripsi)
        val ivMenu = itemView.findViewById<ImageView>(R.id.iv_menu)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food,parent,false)
        return MenuViewHolder(view)
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val data = listMenu[position]
        holder.tvNama.text = data.nama
        holder.tvHarga.text = data.harga
        holder.tvDeskripsi.text = data.deskripsi
        Glide.with(holder.itemView.context)
            .load(data.gambar)
            .into(holder.ivMenu)
        setAnimation(holder.itemView)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)

            intent.putExtra("NAMA",data.nama)
            intent.putExtra("HARGA",data.harga)
            intent.putExtra("DESKRIPSI",data.deskripsi)
            intent.putExtra("GAMBAR",data.gambar)

            holder.itemView.context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int = listMenu.size
    private fun setAnimation(view: View) {
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.item_animation)
        view.startAnimation(animation)
    }
}