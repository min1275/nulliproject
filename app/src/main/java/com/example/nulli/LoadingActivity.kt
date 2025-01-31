package com.example.nulli

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil.setContentView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.nulli.databinding.ActivitySplashBinding
import com.example.nulli.model.UserData
import com.example.nulli.ui.auth.AuthMailActivity
import com.example.nulli.ui.auth.JoinActivity
import com.example.nulli.ui.auth.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.io.ByteArrayOutputStream

class LoadingActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 9001
    private val db = Firebase.database.reference
    private val storage = Firebase.storage.reference

    val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    var permissionlistener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            Handler(Looper.myLooper()!!).postDelayed({
                val user = Firebase.auth.currentUser
                updateUi(user)

            }, 2000)
        }

        override fun onPermissionDenied(deniedPermissions: List<String>) {

        }
    }

        private fun updateUi(user: FirebaseUser?) {
            if (user != null) {
                user.reload()
                val database = Firebase.database.reference
                database.child("user").child(user.uid).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData = snapshot.getValue(UserData::class.java)!!
                        if (userData.activation.isNullOrBlank() || userData.activation == "true") {
                            if (user.isEmailVerified) {
                                val intent = if (user.isAnonymous) {
                                    Intent(this@LoadingActivity, MainActivity::class.java)
                                } else {
                                    Intent(this@LoadingActivity, AuthMailActivity::class.java)
                                }
                                startActivity(intent)
                                finishAffinity()
                            }
                            val intent = Intent(this@LoadingActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoadingActivity, "접근 차단된 유저 입니다.",Toast.LENGTH_SHORT).show()
                            auth.signOut()

                            val intent = Intent(this@LoadingActivity, LoginJoinActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

        }else{
            val intent = Intent(this, LoginJoinActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        TedPermission.create()
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
            .check();

        auth = Firebase.auth


    }
}