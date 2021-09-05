package ru.yourok.torrserve.ui.fragments.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.yourok.torrserve.R

class SearchAdapter :
    RecyclerView.Adapter<SearchAdapter.SearchHolder>() {

    private var searchResults = mutableListOf<String>()

    class SearchHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleView: TextView? = null

        init {
            titleView = itemView.findViewById(R.id.searchTitle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)

        return SearchHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        holder.titleView?.text = searchResults[position]
    }

    override fun getItemCount() = searchResults.count()

    fun addSearchResults(update: List<String>) {
        searchResults.addAll(update)
        notifyDataSetChanged()
    }
}