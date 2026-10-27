package com.rideshare.malawi.v2.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun formatMwk(amount: Int): String {
    return "MWK %,d".format(amount)
}

fun internationalPhone(national: String): String {
    return "+265$national"
}

fun displayPhone(national: String): String {
    val digits = national.filter { it.isDigit() }
    return if (digits.length == 9) {
        "+265 ${digits.substring(0, 2)} ${digits.substring(2, 5)} ${digits.substring(5)}"
    } else {
        "+265 $national"
    }
}

fun Context.openDialer(national: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${internationalPhone(national)}"))
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show()
    }
}

fun Context.openSms(national: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${internationalPhone(national)}"))
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No messaging app found", Toast.LENGTH_SHORT).show()
    }
}
