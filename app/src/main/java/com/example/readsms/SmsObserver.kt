package com.example.readsms

import android.content.ClipboardManager
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log
import java.util.regex.Pattern

/**
 * @author Wiser
 * 用途: 短信观察者
 */
class SmsObserver(
    private val mContext: Context,
    private val mHandler: Handler?,
    received_code: Int
) : ContentObserver(mHandler) {
    private var mReceivedCode = 1
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        var code = ""
        if (uri.toString() == "content://sms/raw") {
            return
        }
        val inboxUri = Uri.parse("content://sms/inbox")
        val c =
            mContext.contentResolver.query(inboxUri, null, null, null, "date desc")
        if (c != null) {
            if (c.moveToFirst()) {
                val address = c.getString(c.getColumnIndex("address"))
                val body = c.getString(c.getColumnIndex("body"))
                Log.e("发件人为：$address","短信内容为：$body")
                val pattern = Pattern.compile("匹配文案")
                val matcher = pattern.matcher(body)
                if (matcher.find()) {
                    code = matcher.group(0)
                    Log.e("验证码",code)
                    val cmb =
                        mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cmb.text = code
                    mHandler?.obtainMessage(mReceivedCode, code)?.sendToTarget()
                }
            }
            c.close()
        }
    }

    init {
        mReceivedCode = received_code
    }
}