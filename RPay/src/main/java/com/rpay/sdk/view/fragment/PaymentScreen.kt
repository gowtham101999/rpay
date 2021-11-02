package com.rpay.sdk.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.rpay.sdk.base.BaseFragment
import com.rpay.sdk.core.RPay
import com.rpay.sdk.core.RPayHandler
import com.rpay.sdk.databinding.RpayPaymentScreenBinding
import com.rpay.sdk.utils.NetworkResponse
import com.rpay.sdk.viewmodel.PaymentScreenViewModel
import java.util.*
import kotlin.collections.HashMap


internal class PaymentScreen : BaseFragment() {

    private lateinit var binding: RpayPaymentScreenBinding
    private lateinit var viewModel: PaymentScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RpayPaymentScreenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setUpUi()
        setUpObserver()
    }

    private fun setUpObserver() {
        val headers: HashMap<String, String> = HashMap()
        headers["secret_key"] = RPay.getMerchantKey()
        headers["auth_token"] = RPay.getAuthToken()
        val params: HashMap<String, String> = HashMap()
        params["amount"] = RPay.getTotalAmount()
        params["currency"] = RPay.getCurrencyCode()
        viewModel.getPaymentDetails(headers, params).observe(viewLifecycleOwner, {
            when (it) {
                is NetworkResponse.Loading -> {
                    showProgressDialog()
                }
                is NetworkResponse.Success -> {
                    hideProgressDialog()
                    binding.amountTextView.text = RPay.getCurrencyCode() + " " + String.format(Locale.ENGLISH, "%.2f", RPay.getTotalAmount().toDouble())
                    binding.walletBalanceTextView.text = it.data?.data?.api_user_currency + " " + it.data?.data?.api_user_balance?.let { it1 -> String.format(Locale.ENGLISH, "%.2f", it1.toDouble()) }
                    binding.orderAmountTextView.text = it.data?.data?.deductable_currency + " " + it.data?.data?.deduct_amount
                    if (it.data?.data?.site_fee?.toDouble() != 0.0) {
                        binding.rPayFeeTextView.visibility = View.VISIBLE
                        binding.rPayFeeTitleTextView.visibility = View.VISIBLE
                        binding.rPayFeeTextView.text = "+ " + it.data?.data?.deductable_currency + " " + it.data?.data?.site_fee
                    }else {
                        binding.rPayFeeTextView.visibility = View.GONE
                        binding.rPayFeeTitleTextView.visibility = View.GONE
                    }
                    binding.totalAmountTextView.text = it.data?.data?.deductable_currency + " " + it.data?.data?.deductable_amount?.let { it1 -> String.format(Locale.ENGLISH, "%.2f", it1.toDouble()) }
                }
                is NetworkResponse.ErrorResponse -> {
                    hideProgressDialog()
                }
            }
        })
    }

    private fun initialize() {
        viewModel = ViewModelProvider(this).get(PaymentScreenViewModel::class.java)
    }

    private fun setUpUi() {
        binding.apply {

            merchantNameTextView.text = RPay.getAppName()

            walletLayout.setOnClickListener {
                walletLayout.alpha = 1f
                walletPayButton.visibility = View.VISIBLE
                cardLayout.alpha = 0.6f
            }

            cardLayout.setOnClickListener {
                walletLayout.alpha = 0.6f
                walletPayButton.visibility = View.GONE
                cardLayout.alpha = 1f
                val proceedToPay = ProceedBottomSheet()
                proceedToPay.show(parentFragmentManager, "Proceed")
            }

            walletPayButton.setOnClickListener {
                if (context?.let { RPay.isNetConnected(it) } == false){
                    showNoInternetDialog()
                }else {
                    val headers: HashMap<String, String> = HashMap()
                    headers["secret_key"] = RPay.getMerchantKey()
                    headers["auth_token"] = RPay.getAuthToken()
                    val params: HashMap<String, String> = HashMap()
                    params["amount"] = RPay.getTotalAmount()
                    params["currency"] = RPay.getCurrencyCode()
                    params["description"] = ""
                    viewModel.capturePayment(headers, params).observe(viewLifecycleOwner, {
                        val listener = RPay.getListener()
                        when (it) {
                            is NetworkResponse.Loading -> {
                                showProgressDialog()
                            }
                            is NetworkResponse.Success -> {
                                hideProgressDialog()
                                if (it.data?.data?.status.equals("success", true)){
                                    it.data?.data?.TransactionId?.let { it1 ->
                                        activity?.finishAndRemoveTask()
                                        listener.onTransactionSuccess(it1)
                                    }
                                }else {
                                    activity?.finishAndRemoveTask()
                                    listener.onTransactionFailure("Transaction failed")
                                }
                            }
                            is NetworkResponse.ErrorResponse -> {
                                hideProgressDialog()
                                activity?.finishAndRemoveTask()
                                listener.onTransactionFailure("Transaction failed")
                            }
                        }
                    })
                }
            }
        }
    }
}

