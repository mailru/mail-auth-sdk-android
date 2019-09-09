package ru.mail.sdk.sample

import android.app.Application
import ru.mail.auth.sdk.MailRuAuthSdk


class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MailRuAuthSdk.initialize(this)
    }
}