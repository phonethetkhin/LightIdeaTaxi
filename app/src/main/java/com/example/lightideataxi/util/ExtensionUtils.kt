package com.example.lightideataxi.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.isUserSignedIn(): Boolean {
    // Check for existing Google Sign In account, if the user is already signed in
    // the GoogleSignInAccount will be non-null.
    val account = GoogleSignIn.getLastSignedInAccount(this)
    return account != null

}

fun Context.getUserSignedInData(): GoogleSignInAccount? {
    return GoogleSignIn.getLastSignedInAccount(this)
}

inline fun <reified VM : ViewModel, T> T.kodeinViewModel(): Lazy<VM> where T : DIAware, T : AppCompatActivity {
    return lazy { ViewModelProvider(this, direct.instance()).get(VM::class.java) }
}

inline fun <reified VM : ViewModel, T> T.kodeinViewModel(): Lazy<VM> where T : DIAware, T : Fragment {
    return lazy { ViewModelProvider(requireActivity(), direct.instance()).get(VM::class.java) }
}

fun Context.bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
    return ContextCompat.getDrawable(this, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}