package ru.yourok.torrserve.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import ru.yourok.torrserve.R
import ru.yourok.torrserve.app.App
import ru.yourok.torrserve.search.Rutor
import ru.yourok.torrserve.ui.fragments.search.SearchAdapter
import java.net.SocketTimeoutException

class SearchFragment : TSFragment() {

    private lateinit var recyclerView: RecyclerView
    private val adapter = SearchAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vi = inflater.inflate(R.layout.fragment_search, container, false)
        // init recycler widget
        recyclerView = vi.findViewById(R.id.search_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        return vi
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            findViewById<EditText>(R.id.search_input).setOnEditorActionListener { textView, i, _ ->
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    search(textView.text.toString())
                }
                false
            }
        }
    }

    // TODO search with multiple trackers
    private fun search(term: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val document = Jsoup.connect("http://rutor.info/search/$term").timeout(5000).get()

                withContext(Dispatchers.Main) {
                    val results = Rutor().parseSearchPage(document)
                    recyclerView.smoothScrollToPosition(0)
                    adapter.setSearchResults(results.first)
                    if (results.second.isNotEmpty()) {
                        for (ex in results.second) {
                            Log.w("Rutor", "Parse error: " + ex.message)
                        }
                    }
                    // TODO show|send exceptions in results.second
                }

            } catch (e: SocketTimeoutException) {
                App.Toast("Error connection to Rutor")
            }
        }
    }
}
