package com.cartravelsdailerapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.PrefUtils.INCOMING
import com.cartravelsdailerapp.PrefUtils.MISSED
import com.cartravelsdailerapp.PrefUtils.OUTGOING
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ItemCallhistoryBinding
import com.cartravelsdailerapp.models.CallHistory

class CallHistoryByNumberAdapter :
    RecyclerView.Adapter<CallHistoryByNumberAdapter.CallHistoryByNumberViewHolder>() {
    lateinit var listOfCallHistory: List<CallHistory>
    lateinit var binding: ItemCallhistoryBinding
    lateinit var context: Context

    inner class CallHistoryByNumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CallHistoryByNumberViewHolder {
        binding = ItemCallhistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return CallHistoryByNumberViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return listOfCallHistory.size ?: 0
    }

    override fun onBindViewHolder(holder: CallHistoryByNumberViewHolder, position: Int) {
        val callhistoryItem = listOfCallHistory.get(position)
        binding.txtCallTypeName.setText(callhistoryItem.calType)
        binding.txtDateTime.setText(callhistoryItem.date)
        binding.txtSimType.setText(callhistoryItem.SimName)
        binding.txtCallTime.setText(convertSeconds(Integer.parseInt(callhistoryItem.duration)))
        when (callhistoryItem.calType) {
            OUTGOING -> {
                binding.imgCallTypeImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        R.drawable.arrow_blue
                    )
                )
                binding.txtCallTypeName.text = context.getText(R.string.OutgoingCall)
            }
            INCOMING -> {
                binding.imgCallTypeImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        R.drawable.arrow_green
                    )
                )
                binding.txtCallTypeName.text = context.getText(R.string.IncomingCall)

            }
            MISSED -> {
                binding.imgCallTypeImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        R.drawable.arrow_red
                    )
                )
                binding.txtCallTypeName.text = context.getText(R.string.MissedCall)
            }
        }

    }

    fun updateCallHistoryByNumber(list: List<CallHistory>) {
        listOfCallHistory = list
        notifyDataSetChanged()
    }

    private fun convertSeconds(seconds: Int): String {
        val h = seconds / 3600
        val m = seconds % 3600 / 60
        val s = seconds % 60
        val sh = if (h > 0) "$h h" else ""
        val sm =
            (if (m in 1..9 && h > 0) "0" else "") + if (m > 0) (if (h > 0 && s == 0) m.toString() else "$m min") else ""
        val ss =
            if (s == 0 && (h > 0 || m > 0)) "" else (if (s < 10 && (h > 0 || m > 0)) "0" else "") + s.toString() + " " + "sec"
        return sh + (if (h > 0) " " else "") + sm + (if (m > 0) " " else "") + ss
    }
}