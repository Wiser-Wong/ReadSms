package com.example.readsms

import android.Manifest
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

/**
 * @author Wiser
 */
class MainActivity : AppCompatActivity() {

    private var smsHandler: SmsHandler? = null

    private var smsObserver: SmsObserver? = null

    companion object {

        const val MSG_RECEIVED_CODE = 1000

        /**
         * 获取短信验证码并设置
         */
        class SmsHandler(activity: MainActivity?) : Handler() {
            private var reference: WeakReference<MainActivity?>? = null

            init {
                reference = WeakReference(activity)
            }

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == MSG_RECEIVED_CODE) {
                    reference?.get()?.let {
                        Toast.makeText(it,"验证码："+msg.obj?.toString(),Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        applyPermission()
    }

    /**
     * 初始化短息观察者
     */
    private fun initSmsObserve() {
        if (smsHandler == null)
            smsHandler = SmsHandler(this)
        if (smsObserver == null) {
            smsObserver = SmsObserver(this, smsHandler, MSG_RECEIVED_CODE)
            val uri: Uri = Uri.parse("content://sms")
            contentResolver?.registerContentObserver(
                uri,
                true,
                smsObserver as ContentObserver
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                initSmsObserve()
                // 请求验证码
            }
        }
    }

    /**
     * 申请权限
     */
    private fun applyPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS
                    ), 1001
                )
            } else {
                initSmsObserve()
                // 请求验证码
            }
        } else {
            initSmsObserve()
            // 请求验证码
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        smsObserver?.let {
            this.contentResolver?.unregisterContentObserver(it)
            smsObserver = null
        }
        smsHandler = null
    }
}