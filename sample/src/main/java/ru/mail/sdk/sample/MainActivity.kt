package ru.mail.sdk.sample

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import io.github.rybalkinsd.kohttp.dsl.httpPost
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import ru.mail.auth.sdk.AuthError
import ru.mail.auth.sdk.AuthResult
import ru.mail.auth.sdk.MailRuAuthSdk
import ru.mail.auth.sdk.MailRuCallback
import java.net.URL
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        job = Job()
        findViewById<View>(R.id.login).setOnClickListener {
            MailRuAuthSdk.getInstance().startLogin(this)
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!MailRuAuthSdk.getInstance().handleActivityResult(
                requestCode,
                resultCode,
                data,
                SDKResultCallback()
            )
        ) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private inner class SDKResultCallback : MailRuCallback<AuthResult, AuthError> {
        override fun onResult(result: AuthResult) {
            Log.d("SDK", "code: ${result.authCode}")

            launch {
                val userInfo = withContext(Dispatchers.IO) {
                    val token = getAccessToken(result.authCode, result.codeVerifier)
                    return@withContext getUserInfo(token)
                }
                bindUserInfo(userInfo)
            }
        }

        override fun onError(error: AuthError?) {
            Toast.makeText(this@MainActivity, error?.errorReason, Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun bindUserInfo(userInfo: UserInfo?) {
        if (userInfo != null) {
            findViewById<TextView>(R.id.name).text = userInfo.name
            findViewById<TextView>(R.id.email).text = userInfo.email
            loadImage(userInfo.avatar)?.let { drawable ->
                findViewById<ImageView>(R.id.avatar).setImageDrawable(drawable)
            }
        } else {
            findViewById<TextView>(R.id.name).text = getString(R.string.error)
        }
    }

    private suspend fun loadImage(url: String): Drawable? = withContext(Dispatchers.IO) {
        try {
            RoundedBitmapDrawableFactory.create(
                resources,
                URL(url).openConnection().getInputStream()
            ).apply {
                isCircular = true
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getAccessToken(
        authCode: String,
        codeVerifier: String?
    ): String {
        val clientSecret = "f7b771d84bd14b5f81b9937911682214" // Keep this secret on server-side
        val resp = httpPost {
            val params = MailRuAuthSdk.getInstance().oAuthParams
            scheme = "https"
            host = "oauth.mail.ru"
            path = "/token"

            body {
                form {
                    "code" to authCode
                    "client_id" to params.clientId
                    "client_secret" to clientSecret
                    "grant_type" to "authorization_code"
                    "redirect_uri" to params.redirectUrl
                    codeVerifier?.let {
                        "code_verifier" to it
                    }
                }
            }
        }
        return JSONObject(resp.body()!!.string()).optString("access_token")
    }

    private fun getUserInfo(accessToken: String): UserInfo? {
        return try {
            val resp = httpPost {
                scheme = "https"
                host = "oauth.mail.ru"
                path = "/userinfo"

                body {
                    form {
                        "access_token" to accessToken
                    }
                }
            }
            val info = JSONObject(resp.body()!!.string())
            UserInfo(
                info.optString("name"),
                info.getString("email"),
                info.optString("image")
            )
        } catch (e: JSONException) {
            null
        }
    }

    data class UserInfo(val name: String, val email: String, val avatar: String)
}
