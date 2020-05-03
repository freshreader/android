package app.freshreader.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.json.responseJson

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appVersionNameTextView = view.findViewById<TextView>(R.id.app_version_name_text_view)
        appVersionNameTextView.text = getString(R.string.freshreader_version_name_prefix, BuildConfig.VERSION_NAME)

        val accountNumberFromSharedPrefs = getSharedPrefs().getString(getString(R.string.shared_prefs_account_number_key), "")

        val editText = view.findViewById<EditText>(R.id.account_number_input)
        editText.setText(accountNumberFromSharedPrefs)

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            getApiTokenForUserAccountNumber(editText)
        }
    }

    private fun FirstFragment.getApiTokenForUserAccountNumber(editText: EditText) {
        val accountNumber = editText.text.toString()

        val uri = "/api/v1/users/${accountNumber}"

        Fuel.get(uri).appendHeader(Headers.ACCEPT to "application/json")
            .responseJson { request, response, result ->
                if (response.statusCode == 204) {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_save_toast_msg),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@responseJson
                }

                result.fold(success = { json ->
                    val accountNumber = editText.text.toString()
                    val apiAuthToken = json.obj().get("api_auth_token").toString()

                    with(getSharedPrefs().edit()) {
                        putString(
                            getString(R.string.shared_prefs_account_number_key),
                            accountNumber
                        )
                        putString(getString(R.string.shared_prefs_api_auth_token_key), apiAuthToken)
                        apply()
                        Toast.makeText(context, getString(R.string.valid_account_number), Toast.LENGTH_SHORT).show()
                    }
                }, failure = { _error ->
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_save_toast_msg),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                })
            }
    }

    private fun getSharedPrefs(): SharedPreferences {
        return requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }
}
