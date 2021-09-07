package ru.yourok.torrserve.ui.fragments.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import ru.yourok.torrserve.R
import ru.yourok.torrserve.app.App
import ru.yourok.torrserve.search.parsers.Rutor
import ru.yourok.torrserve.ui.fragments.TSFragment
import java.net.SocketTimeoutException

class SearchFragment : TSFragment() {

    private val adapter = SearchAdapter()

    private val recyclerView by lazy { requireView().findViewById<RecyclerView>(R.id.search_recycler_view) }
    private val settings by lazy { requireView().findViewById<LinearLayout>(R.id.searchSettings) }
    private val preferences by lazy { requireActivity().getPreferences(Context.MODE_PRIVATE) }

    companion object {
        const val chosenSortPrefName = "chosenSort"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vi = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        return vi
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSearchSettings()

        view.apply {
            // save sort setting
            findViewById<RadioGroup>(R.id.searchSettingsSort).setOnCheckedChangeListener { _, id ->
                preferences.edit()?.putInt(chosenSortPrefName, id)?.apply()
            }
            // search torrents
            findViewById<EditText>(R.id.search_input).setOnEditorActionListener { textView, i, _ ->
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    search(textView.text.toString())
                    settings.visibility = View.GONE
                    true
                } else false
            }
            // show search settings on key up (TV)
            findViewById<EditText>(R.id.search_input).setOnKeyListener { view, i, keyEvent ->
                val cursorPos = (view as EditText).selectionStart

                if (cursorPos == 0 && keyEvent.action == KeyEvent.ACTION_DOWN && keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    settings?.visibility = View.VISIBLE
                    false
                } else false
            }

        }
    }

    // Setup saved preferences
    private fun initSearchSettings() {
        val chosenSort = preferences.getInt(chosenSortPrefName, -1) ?: -1
        if (chosenSort != -1) {
            view?.findViewById<RadioGroup>(R.id.searchSettingsSort)?.check(chosenSort)
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
