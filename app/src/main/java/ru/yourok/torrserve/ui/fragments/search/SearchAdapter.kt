package ru.yourok.torrserve.ui.fragments.search

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.yourok.torrserve.R
import ru.yourok.torrserve.search.TorrentInfo
import ru.yourok.torrserve.ui.activities.play.PlayActivity
import java.text.SimpleDateFormat
import java.util.*

class SearchAdapter :
    RecyclerView.Adapter<SearchAdapter.SearchHolder>() {

    private var searchResults = mutableListOf<TorrentInfo>()

    companion object {
        @SuppressLint("ConstantLocale")
        val format = SimpleDateFormat("dd MMM yy", Locale.getDefault())
    }

    class SearchHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView by lazy { itemView.findViewById(R.id.torrentSearchTitle) }
        private val seedersView: TextView by lazy { itemView.findViewById(R.id.torrentSeedsCount) }
        private val leechesView: TextView by lazy { itemView.findViewById(R.id.torrentLeechesCount) }
        private val sizeView: TextView by lazy { itemView.findViewById(R.id.torrentSize) }
        private val dateView: TextView by lazy { itemView.findViewById(R.id.torrentDate) }

        fun setData(info: TorrentInfo) {
            itemView.tag = info
            titleView.text = info.title
            sizeView.text = info.size
            seedersView.text = info.seeds.toString()
            leechesView.text = info.leeches.toString()
            dateView.text = if (info.added != null) format.format(info.added) else ""
            // TODO set torrentTracker
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)

        itemView.setOnFocusChangeListener { view, isFocused ->
            val visibility = if (isFocused) View.VISIBLE else View.INVISIBLE
            view.findViewById<View>(R.id.searchDivider).visibility = visibility

            // TODO scroll full text
            // view.findViewById<TextView>(R.id.torrentSearchTitle).isSelected = isFocused
        }

        itemView.setOnClickListener {
            // TODO open details
            Log.i("click", it.toString())
        }

        itemView.setOnKeyListener { _, _, keyEvent ->
            // play torrent on right arrow button
            if (keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyEvent.action == KeyEvent.ACTION_DOWN) {
                val intent = Intent(parent.context, PlayActivity::class.java)
                if (itemView.tag is TorrentInfo) {
                    val magnet = (itemView.tag as TorrentInfo).magnet
                    if (magnet != null) {
                        intent.data = Uri.parse(magnet)
                        intent.putExtra("action", "play") // in "play" action we don't save torrent: PlayActivity.kt:126
                        parent.context.startActivity(intent)
                    } else {
                        // TODO fetch detail information
                        Log.i("search", "TODO fetch detail information")
                    }
                }
                true
            } else false // return false to continue keyEvent propagation
        }

        return SearchHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        holder.setData(searchResults[position])
    }

    override fun getItemCount() = searchResults.count()

    @SuppressLint("NotifyDataSetChanged")
    fun setSearchResults(list: List<TorrentInfo>) {
        searchResults = list.toMutableList()
        notifyDataSetChanged()
    }
}