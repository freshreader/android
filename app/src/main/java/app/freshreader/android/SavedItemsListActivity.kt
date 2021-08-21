package app.freshreader.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import app.freshreader.android.databinding.ActivitySavedItemsListBinding
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.json.responseJson
import android.R
import android.view.View
import android.R.id

import android.R.array
import android.content.ClipData
import android.widget.*

import org.json.JSONObject
import android.widget.TextView

import android.content.ClipData.Item
import android.content.Intent
import android.net.Uri

import android.view.LayoutInflater

import android.view.ViewGroup

import android.widget.ArrayAdapter

class SavedItem(var title: String, var url: String)

class SavedItemsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedItemsListBinding
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySavedItemsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listView = findViewById(app.freshreader.android.R.id.sil)
        getSavedArticlesForAccount()
    }

    private fun getSavedArticlesForAccount() {
        val accountNumber = getSharedPrefs().getString(getString(app.freshreader.android.R.string.shared_prefs_account_number_key), "")
        val apiAuthToken = getSharedPrefs().getString(getString(app.freshreader.android.R.string.shared_prefs_api_auth_token_key), "")
        val uri = "/api/v1/articles"

        Fuel.get(uri)
            .header(
                Headers.AUTHORIZATION,
                "Token token=\"${apiAuthToken}\", account_number=\"${accountNumber}\""
            ).appendHeader(Headers.ACCEPT to "application/json")
            .responseJson { request, response, result ->
                if (response.statusCode == 204) {
                    Toast.makeText(
                        applicationContext,
                        getString(app.freshreader.android.R.string.failed_to_save_toast_msg),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@responseJson
                }

                result.fold(success = { json ->
                    val savedItemsForAdapter: ArrayList<SavedItem> = ArrayList<SavedItem>()
                    val savedStringsForAdapter: ArrayList<String> = ArrayList<String>()
                    var items = json.array()
                    for (i in 0 until items.length()) {
                        val row: JSONObject = items.getJSONObject(i)
                        val title = row.getString("title")
                        val url = row.getString("url")
                        var item = SavedItem(title, url)
                        savedStringsForAdapter.add(title)
                        savedItemsForAdapter.add(item)
                    }
                    val itemsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedStringsForAdapter)
                    listView.onItemClickListener = AdapterView.OnItemClickListener {
                            adapterView, view, i, l ->
                        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(savedItemsForAdapter[i].url)));
                        Toast.makeText(applicationContext,
                            "you selected item " + (i + 1),
                            Toast.LENGTH_LONG).show()
                    }
                    listView.setAdapter(itemsAdapter)

                }, failure = { _error ->
                    Toast.makeText(
                        applicationContext,
                        getString(app.freshreader.android.R.string.failed_to_save_toast_msg),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                })
            }
    }


    private fun getSharedPrefs(): SharedPreferences {
        return getSharedPreferences(getString(app.freshreader.android.R.string.preference_file_key), Context.MODE_PRIVATE)
    }
}