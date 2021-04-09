package com.octopus.controlproject

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.util.Log
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.zemaillib.ZMailManager
import com.android.zemaillib.callback.IEmailSendListener
import com.smailnet.eamil.entity.To
import com.smailnet.emailkit.EmailKit
import com.smailnet.emailkit.Message
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * @Author： zh
 * @Date： 11/12/20
 */
class MainActivity : AppCompatActivity() {


    private var notempty = false
    private val poolPeriod = 15
    private var mEtFrom: EditText? =null
    private var mEtTo: EditText? = null
    private var mEtPassword : EditText? = null


    private val mScheduledExecutorService: ScheduledExecutorService =
        Executors.newScheduledThreadPool(4)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (lacksPermissions()) {
            requestPermissions(permissions, 100)
        }


        val user = intent.getStringExtra("user")
        val password = intent.getStringExtra("password")

        btn_Start.setOnClickListener {
//
            if ( et_to.text.isNotBlank()) {
                notempty = true
                Log.e(TAG, "notempty: $notempty")

                Toast.makeText(this,"start service",Toast.LENGTH_LONG).show()
//                et_from.isEnabled = false
//                et_password.isEnabled = false
                et_to.isEnabled = false

                mScheduledExecutorService.scheduleAtFixedRate({
//            if (lacksPermissions()) {
//                return@scheduleAtFixedRate
//            }
                    if (!Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "No permission, please authorize", Toast.LENGTH_SHORT)
                            .show()
                        return@scheduleAtFixedRate
                    }

                    Log.e("TAG", "ExecutorService: $isAction  $time $photoToast")
                    if (isAction) {

                        if (time < 0) return@scheduleAtFixedRate
                        time += unitTime
                        Log.e("TAG", "ExecutorService: $isAction  $time")
                        if (time >= countTime) {
                            mCameraUtil?.stopRecord()
                            if (audioUtil != null) {
                                audioUtil?.stopRecord()
                            }

                            if (notempty) {
                                Log.e("TAG", "onCreate: start send mail")
                                runOnUiThread {
                                    sendMail(user, password)
                                }
                            }
                            return@scheduleAtFixedRate
                        }
                        return@scheduleAtFixedRate
                    }

                    getAllSms()
                    receiveMail()

                }, 5, poolPeriod.toLong(), TimeUnit.SECONDS)
            }else{Toast.makeText(this,"please enter correct Email and Password!",Toast.LENGTH_LONG).show()}

        }

        btn_Shutdown.setOnClickListener {

            Toast.makeText(this, "stop service", Toast.LENGTH_LONG).show()
            mScheduledExecutorService.shutdown()
            super.onBackPressed()

            val isShutdown = mScheduledExecutorService.isShutdown
            Log.e(TAG, "isShutdown $isShutdown")
        }


        /**
         * button sets
         * can do it manually without SMS order and Email order
         */
        btnMedia.setOnClickListener {
            if (isAction) {
                Toast.makeText(
                    this@MainActivity,
                    "Please wait, The mission is in progress...",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            mCameraUtil?.startRecord()
        }
        btnStop.setOnClickListener {
            if (isAction) {
                Toast.makeText(
                    this@MainActivity,
                    "Please wait, The mission is in progress...",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            mCameraUtil?.stopRecord()
        }
        btnSend.setOnClickListener {
            if (isAction) {
                Toast.makeText(
                    this@MainActivity,
                    "Please wait, The mission is in progress...",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            sendMail(user,password)
        }
        btnPhoto.setOnClickListener {
            if (isAction) {
                Toast.makeText(
                    this@MainActivity,
                    "Please wait, The mission is in progress...",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            mCameraUtil?.initCamera()
        }
        btnAudio.setOnClickListener {
            if (isAction) {
                Toast.makeText(
                    this@MainActivity,
                    "Please wait, The mission is in progress...",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            if (audioUtil == null) audioUtil = AudioUtil()
            audioUtil?.startRecord(this)
        }
        btnStopAudio.setOnClickListener {
            if (isAction) {
                Toast.makeText(
                    this@MainActivity,
                    "Please wait, The mission is in progress...",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            if (audioUtil != null) {
                audioUtil?.stopRecord()
            }
        }

        /**
         * permission of floating window
         */
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "No permission, please authorize", Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivityForResult(
                    Intent(
                        ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ), 101
                )
            }
        } else {
            startFloat()
        }
    }

    private var audioUtil: AudioUtil? = null

    private var isAction = false

    private var photoToast = false

    private val unitTime = 10

    private var countTime = 15

    private var time = 0


    private val TAG = "TAG"

    private val host = "imap.gmail.com"
    private val port = 993
    private val protocol = "imap"

//    private val host = "pop.gmail.com"
//    private val port = 995
//    private val protocol = "pop3"
//
    private val smtp = "smtp.gmail.com"
    private val ssl = true
    private val username = "z354746852@gmail.com"
    private val password = "mirdwqppsssbbtcx"

//    //Configure mail server parameters
//    var config = Email.Config()
//        .setIMAP(host, port, ssl)
//        .setAccount(username) //Email address
//        .setPassword(password) //Email password or authorization code

    /**
     * receive Email and get the content of each Email
     */

    private fun receiveMail() {

        val config: EmailKit.Config = EmailKit.Config()
                .setSMTP(smtp, 465, true)
                .setIMAP(host, port, ssl)
                .setAccount(username)
                .setPassword(password)

//        try {

            //with IMAP protocol receive E-mail
            EmailKit.useIMAPService(config)
                    .inbox
                    .load(-1, object : EmailKit.GetLoadCallback {
                        override fun onSuccess(msgList: List<Message>) {
                            for (msg in msgList) {

                                if (msg.subject == "order" && !msg.flags.isRead) {

                                    Log.e(TAG, "connect successful, start to receive email")

                                    Log.i(TAG, "uid：" + msg.uid)
                                    Log.i(TAG, "subject：" + msg.subject)
                                    Log.i(TAG, "date：" + msg.sentDate.text)
                                    Log.i(TAG, "from：" + msg.sender.address)
                                    Log.i(TAG, "is Read：" + msg.flags.isRead)
                                    Log.i(TAG, "---------------------------------------")

                                    val content = msg.content.mainBody.text
                                    Log.e(TAG, "content ：$content")

                                    val index: Int = content.indexOf("order")
                                    val cont: String = content.substring(index)
                                    Log.e(TAG, "index $index")
                                    Log.e(TAG, "cont $cont")

                                    when {
                                        cont.startsWith("order:photo") -> {
                                            countTime = 0
                                            mCameraUtil?.initCamera()
                                            Toast.makeText(this@MainActivity,"taking photo",Toast.LENGTH_LONG).show()
                                            Log.e("TAG", "start to take photo")

                                        }
                                        cont.startsWith("order:video") -> {
                                            val bodys = content.split(";")
                                            if (bodys.size > 1) {
                                                try {
                                                    countTime = unitTime * bodys[1].toInt()
                                                } catch (e: Exception) {
                                                    Log.e("TAG", "getAllEmail: time format wrong")
                                                    countTime = unitTime * 1
                                                }
                                            }
                                            mCameraUtil?.startRecord()
                                            Toast.makeText(this@MainActivity,"video recording",Toast.LENGTH_LONG).show()
                                            Log.e("TAG", "start to record")

                                        }
                                        cont.startsWith("order:audio") -> {
                                            val bodys = content.split(";")
                                            if (bodys.size > 1) {
                                                try {
                                                    countTime = unitTime * bodys[1].toInt()
                                                } catch (e: Exception) {
                                                    Log.e("TAG", "getAllEmail: time format wrong")
                                                    countTime = unitTime * 1
                                                }
                                            }
                                            if (audioUtil == null) audioUtil = AudioUtil()
                                            audioUtil?.startRecord(this@MainActivity)
                                            Toast.makeText(this@MainActivity,"audio recording",Toast.LENGTH_LONG).show()
                                            Log.e("TAG", "start to take record audio")
                                        }
                                    }

                                    isAction = true
                                    time = 0
                                    photoToast = true

                                } else {
                                    Log.e(TAG, "no order")
                                }
                            }

                        }

                        override fun onFailure(errMsg: String) {
                            Log.i(TAG, errMsg)
                        }
                    })


//    }catch (e: Exception) {
//            e.printStackTrace()
//        }
    }


    /**
     * get all SMS and filter the content
     */

    private fun getAllSms() {
        val message = Uri.parse("content://sms/")
        val cr = contentResolver
//        val c = cr.query(message, null, "read = 0", null, null)
        val c = cr.query(message, null, null, null, null)
        startManagingCursor(c)
        val totalSMS = c!!.count
        Log.e("TAG", "getAllSms: count" + totalSMS)
        Log.e("TAG", "getAllSms: start --->")
        if (c.moveToFirst()) {
            for (i in 0 until totalSMS) {
                val body = c.getString(c.getColumnIndexOrThrow("body"))
                val read = c.getString(c.getColumnIndexOrThrow("read"))//0 unread, 1 read
                val SmsMessageId: String = c.getString(c.getColumnIndex("_id"))
                Log.e("TAG", "getAllSms:    body:$body   read:$read   id:$SmsMessageId")

                var bodyStr = SharedPreferencesUtil.getString(applicationContext, "body", "")
//                Log.e("TAG", "getAllSmsBodyStr:  body: $bodyStr")
                val bodyArr = bodyStr.split("+")
                if (bodyArr.contains(body)) {
                    c.moveToNext()
                    continue
                } else if (body.startsWith("order:video")) {
                    val bodys = body.split(";")
                    if (bodys.size > 1) {
                        try {
                            countTime = unitTime * bodys[1].toInt()
                        } catch (e: Exception) {
                            Log.e("TAG", "getAllSms: time format wrong")
                            countTime = unitTime * 1
                        }
                    }
                    mCameraUtil?.startRecord()
                } else if (body.startsWith("order:photo")) {
                    countTime = 0
                    mCameraUtil?.initCamera()
                } else if (body.startsWith("order:audio")) {
                    val bodys = body.split(";")
                    if (bodys.size > 1) {
                        try {
                            countTime = unitTime * bodys[1].toInt()
                        } catch (e: Exception) {
                            Log.e("TAG", "getAllSms: time format wrong")
                            countTime = unitTime * 1
                        }
                    }
                    if (audioUtil == null) audioUtil = AudioUtil()
                    audioUtil?.startRecord(this)
                } else {
                    c.moveToNext()
                    continue
                }

                isAction = true
                time = 0
                photoToast = true

                bodyStr += "+$body"
                SharedPreferencesUtil.putString(applicationContext, "body", bodyStr)

                val values = ContentValues()
                values.put("read", "0")
                values.put("body", "read $body")
                contentResolver.update(
                    Uri.parse("content://sms/"),
                    values,
                    "_id=?",
                    arrayOf(SmsMessageId)
                )

                c.moveToNext()

                break
            }
        }
        //In the version above android 4.0, Cursor will be closed automatically, and the user does not need to close it.
    }

    /**
     * send file with Email
     */

    private fun sendMail(user:String, password:String) {
        time = -1
        val files = CameraUtil.getMediaStorageDir(this@MainActivity).listFiles()
        var file: File? = null
        if (files != null && files.isNotEmpty()) {
            file = files[0]
        }
        ZMailManager
            .fromAddr(user)
            .nickName("controlProject")
            .password(password)
            .host("smtp.gmail.com")
//            .host("smtp.163.com")
            .isSSLvertify(true)
            .port(465)
//            .port(587)
            .subject("current image")
            .content("this is current image!")
            .file(arrayOf(file?.absolutePath))
            .toAddrs(arrayOf(et_to.text.toString())) // can add more Email address(,xxx@gmail,xxx@outlook,....)

            .listener(object : IEmailSendListener {
                override fun sendStart() {
                    Toast.makeText(this@MainActivity, "send start", Toast.LENGTH_SHORT).show()
                }

                override fun sendSuccess() {
                    Toast.makeText(this@MainActivity, "send success", Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "onSuccess: send successfully")
                    file?.delete()
                    isAction = false
                    photoToast = false
                }

                override fun sendFailed(errorMsg: String?) {
                    Toast.makeText(this@MainActivity, "send failure: $errorMsg", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("TAG", "onError: send failed: $errorMsg")
                    isAction = false
                }

            })
            .send()
    }

    private var mCameraUtil: CameraUtil? = null

    private var surfaceHolder: SurfaceHolder? = null

    override fun onDestroy() {
        mCameraUtil?.stopRecord()
        super.onDestroy()
    }

    private val permissions: Array<String>
        get() =
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

    // judge whether the user contains the permissions
    private fun lacksPermissions(): Boolean {
        for (permission in permissions) {
            if (lacksPermission(permission)) {
                return true
            }
        }
        return false
    }

    // whether there is a lack of permissions
    private fun lacksPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_DENIED
    }

    private var surfaceview: SurfaceView? = null


    /**
     * set Flaot
     */

    private fun startFloat() {
        // Get WindowManager service
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 2. Get the WindowManager.LayoutParams object to prepare for the subsequent setting of related parameters:
        val wmParams = WindowManager.LayoutParams()

        //3. Set the relevant window layout parameters:

        //3.1 Set the window type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }

        //3.2 Set the image format, the effect is transparent background //wmParams.format = PixelFormat.RGBA_8888;
        wmParams.format = 1
        // The effect of the flags attribute is the same as "locked".
        // The floating window is not touchable, does not accept any events, and does not affect subsequent event responses.
        wmParams.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        // 4.Set the length and width data of the floating window
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        // 5. the position of the floating window
        wmParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER
        // 6. Set the initial value of x and y based on the origin of the screen center
        wmParams.x = 350
        wmParams.y = -450

        surfaceview = SurfaceView(this)
        surfaceHolder = surfaceview?.holder
        val params_sur: WindowManager.LayoutParams = WindowManager.LayoutParams()
        // width and height
        params_sur.width = 225
        params_sur.height = 300
        //  params_sur.alpha = 255;
        //  params_sur.alpha = 255;
        surfaceview?.layoutParams = params_sur

        surfaceview?.holder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        // surface.getHolder().setFixedSize(800, 1024);
        // surface.getHolder().setFixedSize(800, 1024);
        surfaceview?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surfaceHolder = holder
            }

            override fun surfaceChanged(holder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
                surfaceHolder = holder
                mCameraUtil = CameraUtil(holder, this@MainActivity)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                surfaceview = null
                surfaceHolder = null
            }
        })

        val relLay = LinearLayout(this)
        val params_rel = WindowManager.LayoutParams()
        params_rel.width = WindowManager.LayoutParams.WRAP_CONTENT
        params_rel.height = WindowManager.LayoutParams.WRAP_CONTENT
        relLay.layoutParams = params_rel

        relLay.addView(surfaceview)
        wm.addView(relLay, wmParams) // Create View
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            startFloat()
        }
    }

    private var exitTime: Long = 0

    override fun onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(
                this@MainActivity,
                "Press again to exit",
                Toast.LENGTH_SHORT
            ).show()
            exitTime = System.currentTimeMillis()
        } else {
            super.onBackPressed()
            mScheduledExecutorService.shutdown()
        }
    }

}