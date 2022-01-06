import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.ptkako.nv.novusvision.utility.delegateutil.FragmentViewBindingDelegate

/** Activity binding delegate, may be used since onCreate up to onDestroy (inclusive) */
inline fun <T : ViewBinding> AppCompatActivity.activityViewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

/** Fragment binding delegate, may be used since onViewCreated up to onDestroyView (inclusive) */
fun <T : ViewBinding> Fragment.fragmentViewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

/** Binding delegate for DialogFragments implementing onCreateDialog (like Activities, they don't
 *  have a separate view lifecycle), may be used since onCreateDialog up to onDestroy (inclusive) */
inline fun <T : ViewBinding> DialogFragment.dialogFragmentViewBinding(crossinline factory: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        factory(layoutInflater)
    }

/** Not really a delegate, just a small helper for RecyclerView.ViewHolders */
inline fun <T : ViewBinding> ViewGroup.adapterViewBinding(factory: (LayoutInflater, ViewGroup, Boolean) -> T) =
    factory(LayoutInflater.from(context), this, false)