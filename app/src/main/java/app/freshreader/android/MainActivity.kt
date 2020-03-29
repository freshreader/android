package app.freshreader.android

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val uriFromIntentData: String? = intent?.data?.toString()
        val uriFromClipData: String? = intent?.clipData?.getItemAt(0)?.text?.toString()
        val textViewFirst: TextView = findViewById<TextView>(R.id.textview_activity)

        val uriToSave = when {
            !uriFromIntentData.isNullOrBlank() -> {
                uriFromIntentData
            }
            !uriFromClipData.isNullOrBlank() -> {
                uriFromClipData
            }
            else -> null
        }

        if (uriToSave != null) {
            textViewFirst.text = uriToSave.toString()

            val accountNumber: String = "0000000000000000" // TODO: persist to (and fetch from) shared prefs
            val encodedUri: String = Uri.encode(uriToSave.toString())
            val finalUri: String = "https://freshreader.app/save-mobile?account_number=$accountNumber&url=$encodedUri"

            val httpAsync = finalUri
                .httpGet()
                .responseString { _request, response, result ->
                    if (result is com.github.kittinunf.result.Result.Success && response.statusCode == 200) {
                        Toast.makeText(applicationContext,"Saved!", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(applicationContext,"Failed to save.", Toast.LENGTH_SHORT).show()
                    }
                    runOnUiThread(Runnable {
                        run {
                            finishAffinity()
                        }
                    })
                }

            httpAsync.join()
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
