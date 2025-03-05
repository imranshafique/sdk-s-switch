package com.msn.dataselectionviewpager.Utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.IntRange
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel


class Generator private constructor(context: Context) {
    private var mErrorCorrectionLevel: ErrorCorrectionLevel? = null
    private var mMargin = 0
    private var mContent: String? = null
    private var mWidth: Int
    private var mHeight: Int
    val TAG = javaClass.simpleName


    val qRCOde: Bitmap?
        get() = generate()


    fun setErrorCorrectionLevel(level: ErrorCorrectionLevel?): Generator {
        mErrorCorrectionLevel = level
        return this
    }


    fun setContent(content: String?): Generator {
        mContent = content
        return this
    }


    fun setWidthAndHeight(
        @IntRange(from = 1) width: Int,
        @IntRange(from = 1) height: Int
    ): Generator {
        mWidth = width
        mHeight = height
        return this
    }


    fun setMargin(@IntRange(from = 0) margin: Int): Generator {
        mMargin = margin
        return this
    }

    private fun generate(): Bitmap? {
        val hintsMap: MutableMap<EncodeHintType, Any?> = HashMap()
        hintsMap[EncodeHintType.CHARACTER_SET] = "utf-8"
        hintsMap[EncodeHintType.ERROR_CORRECTION] = mErrorCorrectionLevel
        hintsMap[EncodeHintType.MARGIN] = mMargin
        try {
            val bitMatrix =
                QRCodeWriter().encode(mContent, BarcodeFormat.QR_CODE, mWidth, mHeight, hintsMap)
            val pixels = IntArray(mWidth * mHeight)
            for (i in 0 until mHeight) {
                for (j in 0 until mWidth) {
                    if (bitMatrix[j, i]) {
                        pixels[i * mWidth + j] = -0x1
                    } else {
                        pixels[i * mWidth + j] = 0x282946
                    }
                }
            }
            return Bitmap.createBitmap(pixels, mWidth, mHeight, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            Log.d(TAG, "generate:  ${e.message}")
        }
        return null
    }

    companion object {
        private var qrCodeHelper: Generator? = null

        fun newInstance(context: Context): Generator? {
            if (qrCodeHelper == null) {
                qrCodeHelper = Generator(context)
            }
            return qrCodeHelper
        }
    }

    init {
        mHeight = (context.resources.displayMetrics.heightPixels / 2.4).toInt()
        mWidth = (context.resources.displayMetrics.widthPixels / 1.3).toInt()
    }
}