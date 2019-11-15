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
import kotlinx.coroutines.*
import ru.mail.auth.sdk.*
import ru.mail.auth.sdk.api.OAuthRequestErrorCodes
import ru.mail.auth.sdk.api.token.OAuthTokensResult
import ru.mail.auth.sdk.api.user.UserInfoResult
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
        if (!MailRuAuthSdk.getInstance().handleAuthResult(
                requestCode,
                resultCode,
                data,
                SDKResultCallback()
            )
        ) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    inner class SDKResultCallback : MailRuCallback<OAuthTokensResult, Int> {
        override fun onResult(result: OAuthTokensResult) {
            print(result.accessToken)
            MailRuAuthSdk.getInstance().requestUserInfo(result, UserInfoCallback())
        }

        override fun onError(error: Int) {
            Toast.makeText(
                applicationContext,
                OAuthRequestErrorCodes.toReadableString(error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private inner class UserInfoCallback : MailRuCallback<UserInfoResult, Int> {
        override fun onError(error: Int) {
            Toast.makeText(
                applicationContext,
                OAuthRequestErrorCodes.toReadableString(error),
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onResult(result: UserInfoResult) {
            launch {
                bindUserInfo(result)
            }
        }
    }

    private suspend fun bindUserInfo(userInfo: UserInfoResult) {
        findViewById<TextView>(R.id.name).text = userInfo.name
        findViewById<TextView>(R.id.email).text = userInfo.email
        loadImage(userInfo.avatarUrl)?.let { drawable ->
            findViewById<ImageView>(R.id.avatar).setImageDrawable(drawable)
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
}
