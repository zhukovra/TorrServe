package ru.yourok.torrserve.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.yourok.torrserve.R
import ru.yourok.torrserve.app.App
import ru.yourok.torrserve.ui.fragments.search.SearchAdapter

class SearchFragment : TSFragment() {

    private lateinit var recyclerView: RecyclerView
    private val adapter = SearchAdapter()
    private var searchResults = mutableListOf<String>()

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
                    App.Toast(textView.text.toString())
                    // TODO run in background
                    search()
                    adapter.addSearchResults(searchResults)
                }
                true
            }
        }
    }

    // TODO search
    private fun search() {
        val data = mutableListOf<String>()
        (0..50).forEach { i -> data.add("$i element") }
        searchResults = data
    }
}
