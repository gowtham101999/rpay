package com.rpay.sdk.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import com.rpay.sdk.listener.RPayListener
import com.rpay.sdk.utils.Session
import com.rpay.sdk.view.activity.HomeScreen

@SuppressLint("StaticFieldLeak")
object RPayHandler {

    /**
     * View context
     */
    private lateinit var context: Activity

    /**
     * RPay Handler - Handler for all payment related works
     */
    private var rPayHandler: RPayHandler? = null

    /**
     * Initialize Handler
     */
    fun getInstance(context: Activity): RPayHandler {
        if (rPayHandler == null) {
            rPayHandler = RPayHandler
        }
        this.context = context
        this.session = Session.getInstance(context)
        return rPayHandler as RPayHandler
    }

    /**
     * RPay Listener - Listen payment status
     */
    private lateinit var rPayListener: RPayListener


    /**
     * session to store values
     */
    private lateinit var session: Session

    /**
     * Store merchant key
     */
    fun storeMerchantKey(key: String) {
        session.setMerchantKey(key)
    }

    /**
     * Get merchant key
     */
    fun getMerchantKey(): String {
        return session.getMerchantKey().toString()
    }

    /**
     * Store Auth Token
     */
    fun storeAuthToken(key: String) {
        session.setAuthToken(key)
    }

    /**
     * Get Auth Token
     */
    fun getAuthToken(): String {
        return session.getAuthToken().toString()
    }

    /**
     * Get Total Amount
     */
    fun getTotalAmount(): String {
        return session.getTotalAmount().toString()
    }

    /**
     * Get Currency Code
     */
    fun getCurrencyCode(): String {
        return session.getCurrencyCode().toString()
    }

    /**
     * SetUp payment listener
     */
    fun setPaymentListener(rPayListener: RPayListener) {
        this.rPayListener = rPayListener
    }

    /**
     * SetUp App Name
     */
    fun setAppName(name: String) {
        session.setAppName(name)
    }

    /**
     * Get App Name
     */
    fun getAppName(): String {
        return session.getAppName()
    }

    /**
     * SetUp payment listener
     */
    fun getPaymentListener(): RPayListener {
        return rPayListener
    }

    /**
     * SetUp Payment screen
     */
    fun makePayment(amount: String, currencyCode: String) {
        when {
            amount.isEmpty() -> {
                RPayLog.message("Price should not be empty")
            }
            amount.toInt() == 0 -> {
                RPayLog.message("Price should not be 0")
            }
            currencyCode.isEmpty() -> {
                RPayLog.message("Currency should not be empty")
            }
            else -> {
                val intent = Intent(context, HomeScreen::class.java)
                session.setCurrencyCode(currencyCode)
                session.setTotalAmount(amount)
                context.startActivity(intent)
            }
        }
    }

}