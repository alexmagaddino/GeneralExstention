package it.alexm.exstentionutils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.cketti.mailto.EmailIntentBuilder
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.io.File


/**
 * Created by alexm
 */
fun Activity.showToast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    try {
        Toast.makeText(this, id, length).show()
    } catch (e: Exception) {
        //nothing
    }
}

fun Fragment.showToast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    activity?.showToast(id, length)
}

fun Activity.showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        Toast.makeText(this, msg, length).show()
    } catch (e: Exception) {
        //nothing
    }
}

fun Fragment.showToast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    activity?.showToast(msg, length)
}

fun Activity.keyboardVisibilityListener(callback: (Boolean) -> Unit) {
    KeyboardVisibilityEvent.setEventListener(this, callback)
}

fun Fragment.keyboardVisibilityListener(callback: (Boolean) -> Unit) {
    KeyboardVisibilityEvent.setEventListener(activity, callback)
}

/**
 * Nuove funzioni per nascondere o richiamare la soft keyboard
 * */
fun Activity.setSoftKeyboardVisibility(toShow: Boolean?) {
    (getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
        currentFocus?.windowToken?.also {
            if (toShow == true)
                toggleSoftInputFromWindow(it, InputMethodManager.SHOW_FORCED, 0)
            else
                hideSoftInputFromWindow(it, 0)
        }
    }
}

fun Fragment.setSoftKeyboardVisibility(toShow: Boolean?) {
    activity?.setSoftKeyboardVisibility(toShow)
}

fun Activity.browser(url: String, requestInt: Int = -1) {

    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {

        if (requestInt > -1) {
            startActivityForResult(browserIntent, requestInt)
        } else {
            startActivity(browserIntent)
        }
    } catch (e: ActivityNotFoundException) {
        startActivityForResult(
            Intent.createChooser(browserIntent, "Apri con..."), requestInt
        )
    }
}

fun Activity.dial(phoneNumber: String, requestInt: Int = -1) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse(if (!phoneNumber.contains("tel:")) "tel:$phoneNumber" else phoneNumber)
    }
    if (intent.resolveActivity(packageManager) != null) {
        if (requestInt > -1) {
            startActivityForResult(intent, requestInt)
        } else {
            startActivity(intent)
        }
    }
}

fun Activity.whatsapp(phoneNumber: String, requestInt: Int = -1): Boolean {
    return try {
        browser(
            if (!phoneNumber.contains("https://wa.me/")) "https://wa.me/$phoneNumber" else phoneNumber,
            requestInt
        )
        true
    } catch (e: java.lang.Exception) {
        false
    }
}

fun Activity.share(chooserText: String, subject: String, text: String) {
    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }, chooserText))
}

fun Activity.email(toEmail: String, subject: String = "", body: String = "") {

    EmailIntentBuilder.from(this)
        .to(toEmail)
        .subject(subject)
        .body(body)
        .start()
}

fun Activity.openPdf(pdf: File) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                FileProvider.getUriForFile(
                    this@openPdf,
                    applicationContext.packageName + ".provider", pdf
                ),
                "application/pdf"
            )
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        })
    } catch (e: ActivityNotFoundException) {
        showToast(R.string.no_activity_to_open_pdf, Toast.LENGTH_LONG)
    }
}

/**
 * Metodo per cambiare lo style del bottone passando un booleano
 * con true si espande con false si restringe
 * */
@Throws(Exception::class)
fun Button.buttonExpansion(toExpand: Boolean) {
    if (parent !is ConstraintLayout)
        throw Exception("Button's parent is not a ConstraintLayout")

    val originLayoutParams = layoutParams as ConstraintLayout.LayoutParams

    if (toExpand) {

        val param = ConstraintLayout.LayoutParams(
            ConstraintLayout
                .LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        param.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        param.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        param.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        param.setMargins(0, 0, 0, 0)
        param.circleRadius = 0
        this.layoutParams = param
        this.background = ContextCompat.getDrawable(
            context,
            R.drawable.button_white_accent_background_keyboard
        )

    } else {
        this.layoutParams = originLayoutParams
        this.background = ContextCompat.getDrawable(
            context,
            R.drawable.button_white_accent_background
        )
    }
}

/**
 * Metodo per cambiare lo style del bottone all'apertura o chiusura della keyboard
 * necessario che il bottone sia direttamente dentro un constraint layout
 * */
fun Button.keyboardChangeButtonStyle(
    activity: Activity?,
    callback: (Boolean) -> Unit = {}
) {
    activity?.keyboardVisibilityListener {
        this.buttonExpansion(it)
        callback.invoke(it)
    }
}

/**
 * non mi piace la gestione della visibilità delle view
 * */
fun <V : View> V.setVisible(vis: Boolean?) {
    this.isVisible = vis ?: false
}

fun Fragment.initSupportActionBar(
    toolbar: Toolbar,
    navigationOnClickListener: (View) -> Unit = {}
) {

    (activity as AppCompatActivity).apply {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.title = ""
        toolbar.setNavigationOnClickListener(navigationOnClickListener)
    }
}

private fun AppCompatActivity.setSupportActionBar(
    toolbar: Toolbar,
    showBackArrow: Boolean = true
) {
    setSupportActionBar(toolbar)
    toolbar.title = ""
    if (showBackArrow) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)
    }
}