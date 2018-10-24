package br.com.i42.controledeativos

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.beacon_list_item.view.*

class BeaconAdapter(private val beaconItemList: List<BeaconData>, private val clickListener: (BeaconData) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // LayoutInflater: takes ID from layout defined in XML.
        // Instantiates the layout XML into corresponding View objects.
        // Use context from main app -> also supplies theme layout values!
        val inflater = LayoutInflater.from(parent.context)

        // Inflate XML. Last parameter: don't immediately attach new view to the parent view group
        val view = inflater.inflate(R.layout.beacon_list_item, parent, false)
        return BeaconViewHolder(view)
    }

    override fun getItemCount() = beaconItemList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Populate ViewHolder with data that corresponds to the position in the list
        // which we are told to load
        (holder as BeaconViewHolder).bind(beaconItemList[position], clickListener)
    }

    class BeaconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(beacon: BeaconData, clickListener: (BeaconData) -> Unit) {
            itemView.beacon_item_name.text = beacon.name
            itemView.beacon_id.text = beacon.key
            itemView.beacon_distance.text = beacon.distance

            itemView.setOnClickListener { clickListener(beacon) }
        }
    }
}