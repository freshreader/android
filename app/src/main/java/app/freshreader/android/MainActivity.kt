package app.freshreader.android
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result;
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        FuelManager.instance.basePath = "https://freshreader.app"
        saveUriFromIntentIfAny()
    }

    private fun saveUriFromIntentIfAny() {
        val uriFromIntentData: String? = intent?.data?.toString()
        val uriFromClipData: String? = intent?.clipData?.getItemAt(0)?.text?.toString()

        val accountNumber = getSharedPrefs().getString(getString(R.string.shared_prefs_account_number_key), "")
        val apiAuthToken = getSharedPrefs().getString(getString(R.string.shared_prefs_api_auth_token_key), "")

        val uriToSave = when {
            !uriFromIntentData.isNullOrBlank() -> { uriFromIntentData }
            !uriFromClipData.isNullOrBlank() -> { uriFromClipData }
            else -> null
        }

        if (uriToSave != null) {
            val encodedUri: String = Uri.encode(uriToSave.toString())
            val uri = "/api/v1/articles?url=$encodedUri"
            saveUri(uri, apiAuthToken, accountNumber)
        }
    }

    private fun saveUri(uri: String, apiAuthToken: String?, accountNumber: String?) {
        Fuel
            .post(uri)
            .header(
                Headers.AUTHORIZATION,
                "Token token=\"${apiAuthToken}\", account_number=\"${accountNumber}\""
            )
            .responseJson { request, response, result ->
                result.fold(success = { json ->
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.saved_toast_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }, failure = { _error ->
                    if (_error.response.statusCode == 401) {
                        getNewApiTokenAndRetry(uri, accountNumber)
                    }
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.failed_to_save_toast_msg),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                })
                runOnUiThread(Runnable {
                    run {
                        finishAffinity()
                    }
                })
            }
    }

    private fun getNewApiTokenAndRetry(uriToSave: String, accountNumber: String?) {
        Fuel.get("/api/v1/users/${accountNumber}").appendHeader(Headers.ACCEPT to "application/json")
            .responseJson { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.failed_to_save_toast_msg),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    is Result.Success -> {
                        val newApiAuthToken = result.get().obj().get("api_auth_token").toString()

                        with(getSharedPrefs().edit()) {
                            putString(
                                getString(R.string.shared_prefs_api_auth_token_key),
                                newApiAuthToken
                            )
                            apply()
                            Toast.makeText(
                                applicationContext,
                                "Account number saved. API token: $newApiAuthToken",
                                Toast.LENGTH_SHORT
                            ).show()

                            saveUri(uriToSave, newApiAuthToken, accountNumber)
                        }
                    }
                }
            }
    }

    private fun getSharedPrefs(): SharedPreferences {
        return getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
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
            R.id.action_settings -> {
                Toast.makeText(applicationContext, getString(R.string.easter_egg), Toast.LENGTH_SHORT).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
