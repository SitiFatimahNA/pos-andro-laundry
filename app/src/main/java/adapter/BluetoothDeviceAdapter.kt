package adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.siti.pos.R
import android.annotation.SuppressLint

class BluetoothDeviceAdapter (
    private val list: MutableList<BluetoothDevice>,
    private val onPilih: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return ViewHolder(view)
    }
    @SuppressLint("MissingPermission")

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = list[position]
        holder.tvNama.text = device.name ?: "Unknown"
        holder.tvMac.text = device.address
        holder.tvPilih.setOnClickListener { onPilih(device) }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<BluetoothDevice>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaDevice)
        val tvMac: TextView = itemView.findViewById(R.id.tvMacDevice)
        val tvPilih: TextView = itemView.findViewById(R.id.tvPilih)
    }
}