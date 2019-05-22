package com.example.mobilneprojekt

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.mobilneprojekt.services.CarDTO
import com.example.mobilneprojekt.services.ServiceBuilder
import com.squareup.picasso.Picasso

class MyCarsAdapter(values: List<CarDTO>, clickListener: ClickListener): RecyclerView.Adapter<MyCarsAdapter.ViewHolder>()
{
    private var values: List<CarDTO>
    private var clickListener: ClickListener

    init
    {
        this.values = values
        this.clickListener = clickListener
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = values[position].name
        holder.type.text = values[position].category
        holder.price.text = "${values[position].price}zł"
        val url = "${ServiceBuilder.getUrl()}${values[position].image}"
        Picasso.get().load(url).centerCrop().fit().into(holder.image)
    }

    override fun getItemCount(): Int {
        return values.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_cars_row, parent, false)
        return ViewHolder(itemView, clickListener)
    }

    class ViewHolder(view: View, private var clickListener: ClickListener) : RecyclerView.ViewHolder(view), View.OnClickListener
    {
        var name: TextView = view.findViewById(R.id.name)
        var type : TextView = view.findViewById(R.id.type)
        var price: TextView = view.findViewById(R.id.price)
        var image: ImageView = view.findViewById(R.id.imageView2)

        init
        {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            Log.v("click", "onClick: $adapterPosition")
            clickListener.onItemClick(adapterPosition)
        }
    }

    fun update(results: List<CarDTO>) {
        values = results
        notifyDataSetChanged()
    }

    interface ClickListener {
        fun onItemClick(position: Int)
    }
}