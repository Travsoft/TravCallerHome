package com.cartravelsdailerapp.ui.adapters

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.cartravelsdailerapp.R
import com.cvaghela.spinner.searchablespinner.interfaces.ISpinnerSelectedView
import java.util.*


class SpinerArrayListAdapter(
    val mContext: Context,
    var mStrings: ArrayList<String>,
    val mBackupStrings: ArrayList<String>
) :
    ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mStrings), Filterable,
    ISpinnerSelectedView {
    private val mStringFilter = StringFilter()


    override fun getNoSelectionView(): View {
        return View.inflate(mContext, R.layout.view_list_no_selection_item, null)
    }

    override fun getSelectedView(position: Int): View? {

        var view: View? = View.inflate(mContext, R.layout.view_list_item, null)
        val dispalyName = view?.findViewById<TextView>(R.id.TxtVw_DisplayName)
        dispalyName?.text = mStrings[position]
        return view
    }

    override fun getCount(): Int {
        return mStrings.size
    }

    override fun getItem(position: Int): String? {
        return  mStrings[position]
    }

    override fun getItemId(position: Int): Long {
        return mStrings[position].hashCode().toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View? = null
        view = View.inflate(mContext, android.R.layout.simple_spinner_item, null)
        val dispalyName = view.findViewById<TextView>(android.R.id.text1)
        dispalyName.text = mStrings[position]
        return view
    }

    override fun getFilter(): Filter {

        return mStringFilter
    }

    inner class StringFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            if (TextUtils.isEmpty(constraint)) {
                filterResults.count = mBackupStrings.size
                filterResults.values = mBackupStrings
                return filterResults
            }
            val filterStrings: ArrayList<String> = ArrayList()
            for (text in mBackupStrings) {
                if (text.lowercase(Locale.getDefault()).contains(constraint)) {
                    filterStrings.add(text)
                }
            }
            filterResults.count = filterStrings.size
            filterResults.values = filterStrings
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            (results.values as ArrayList<*>).also { mStrings = it as ArrayList<String> }
            notifyDataSetChanged()
        }
    }
}