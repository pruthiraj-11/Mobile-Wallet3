package org.mifos.mobilewallet.mifospay.registration.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.hbb20.CountryCodePicker
import dagger.hilt.android.AndroidEntryPoint
import org.mifos.mobilewallet.mifospay.R
import org.mifos.mobilewallet.mifospay.base.BaseActivity
import org.mifos.mobilewallet.mifospay.registration.RegistrationContract
import org.mifos.mobilewallet.mifospay.registration.RegistrationContract.MobileVerificationView
import org.mifos.mobilewallet.mifospay.registration.presenter.MobileVerificationPresenter
import org.mifos.mobilewallet.mifospay.utils.Constants
import org.mifos.mobilewallet.mifospay.utils.Toaster
import org.mifos.mobilewallet.mifospay.utils.Utils.hideSoftKeyboard
import javax.inject.Inject
import java.util.concurrent.TimeUnit;

@AndroidEntryPoint
class MobileVerificationActivity : BaseActivity(), MobileVerificationView {
    @JvmField
    @Inject
    var mPresenter: MobileVerificationPresenter? = null
    private var mMobileVerificationPresenter: RegistrationContract.MobileVerificationPresenter? = null

    @JvmField
    @BindView(R.id.ccp_code)
    var mCcpCode: CountryCodePicker? = null

    @JvmField
    @BindView(R.id.et_mobile_number)
    var mEtMobileNumber: EditText? = null

    @JvmField
    @BindView(R.id.btn_get_otp)
    var mBtnGetOtp: TextView? = null

    @JvmField
    @BindView(R.id.et_otp)
    var mEtOtp: EditText? = null

    @JvmField
    @BindView(R.id.fab_next)
    var mFabNext: FloatingActionButton? = null

    @JvmField
    @BindView(R.id.progressBar)
    var mProgressBar: ProgressBar? = null

    @JvmField
    @BindView(R.id.tv_verifying_otp)
    var mTvVerifyingOtp: TextView? = null
    var verificationId: String?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_verification)
        window.statusBarColor=resources.getColor(R.color.primaryDarkBlue,null)
        ButterKnife.bind(this)
        mPresenter!!.attachView(this)
        setToolbarTitle("")
        showColoredBackButton(Constants.WHITE_BACK_BUTTON)
        mCcpCode!!.registerCarrierNumberEditText(mEtMobileNumber)
    }

    @OnClick(R.id.btn_get_otp)
    fun onGetOTp() {
        hideSoftKeyboard(this)
        if (mCcpCode!!.isValidFullNumber) {
            showProgressDialog(Constants.SENDING_OTP_TO_YOUR_MOBILE_NUMBER)
            val handler = Handler()
            handler.postDelayed(Runnable {
//                mMobileVerificationPresenter!!.requestOTPfromServer(
//                    mCcpCode!!.fullNumber,
//                    mEtMobileNumber!!.text.toString().trim { it <= ' ' })
                sendVerificationCode("+"+mCcpCode!!.fullNumber)
                onRequestOtpSuccesss()
            }, 1500)
        } else {
            showToast(getString(R.string.enter_valid_mob_num))
        }
    }

    override fun onRequestOtpSuccess() {
        hideProgressDialog()
        mEtMobileNumber!!.isClickable = false
        mEtMobileNumber!!.isFocusableInTouchMode = false
        mEtMobileNumber!!.isFocusable = false
        mCcpCode!!.setCcpClickable(false)
        mEtOtp!!.visibility = View.VISIBLE
        mBtnGetOtp!!.isClickable = false
        mBtnGetOtp!!.setBackgroundResource(R.drawable.ic_done)
        mFabNext!!.visibility = View.VISIBLE
    }
    fun onRequestOtpSuccesss() {
        hideProgressDialog()
        mEtMobileNumber!!.isClickable = false
        mEtMobileNumber!!.isFocusableInTouchMode = false
        mEtMobileNumber!!.isFocusable = false
        mCcpCode!!.setCcpClickable(false)
        mEtOtp!!.visibility = View.VISIBLE
        mBtnGetOtp!!.isClickable = false
        mBtnGetOtp!!.setBackgroundResource(R.drawable.ic_done)
        mFabNext!!.visibility = View.VISIBLE
    }

    override fun onRequestOtpFailed(s: String?) {
        hideProgressDialog()
        showToast(s)
    }

    fun onRequestOtpFailedd(s: String?) {
        hideProgressDialog()
        showToast(s)
    }

    @OnClick(R.id.fab_next)
    fun onNextClicked() {
        hideSoftKeyboard(this)
        if (mEtOtp!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("OTP not Entered")
            mEtOtp!!.requestFocus()
        } else {
            mFabNext!!.isClickable = false
            mProgressBar!!.visibility = View.VISIBLE
            mTvVerifyingOtp!!.visibility = View.VISIBLE
            mEtOtp!!.isClickable = false
            mEtOtp!!.isFocusableInTouchMode = false
            mEtOtp!!.isFocusable = false
            val handler = Handler()
            handler.postDelayed({
                                verifyCode(mEtOtp!!.text.toString().trim { it <= ' ' })
//                mMobileVerificationPresenter!!.verifyOTP(mEtOtp!!.text.toString().trim { it <= ' ' })
            }, 1500)
        }
    }

    override fun onOtpVerificationSuccess() {
        val intent = Intent(this@MobileVerificationActivity, SignupActivity::class.java)
        intent.putExtra(
            Constants.MIFOS_SAVINGS_PRODUCT_ID,
            getIntent().getIntExtra(Constants.MIFOS_SAVINGS_PRODUCT_ID, 0)
        )
        intent.putExtra(
            Constants.GOOGLE_PHOTO_URI, getIntent().getParcelableExtra<Parcelable>(
                Constants.GOOGLE_PHOTO_URI
            ).toString()
        )
        intent.putExtra(
            Constants.GOOGLE_DISPLAY_NAME,
            getIntent().getStringExtra(Constants.GOOGLE_DISPLAY_NAME)
        )
        intent.putExtra(
            Constants.GOOGLE_EMAIL,
            getIntent().getStringExtra(Constants.GOOGLE_EMAIL)
        )
        intent.putExtra(
            Constants.GOOGLE_FAMILY_NAME,
            getIntent().getStringExtra(Constants.GOOGLE_FAMILY_NAME)
        )
        intent.putExtra(
            Constants.GOOGLE_GIVEN_NAME,
            getIntent().getStringExtra(Constants.GOOGLE_GIVEN_NAME)
        )
        intent.putExtra(Constants.COUNTRY, mCcpCode!!.selectedCountryName)
        intent.putExtra(Constants.MOBILE_NUMBER, mCcpCode!!.fullNumber)
        startActivity(intent)
        finish()
    }

    override fun onOtpVerificationFailed(s: String?) {
        mFabNext!!.isClickable = true
        mProgressBar!!.visibility = View.GONE
        mTvVerifyingOtp!!.visibility = View.GONE
        mEtOtp!!.isClickable = true
        mEtOtp!!.isFocusableInTouchMode = true
        mEtOtp!!.isFocusable = true
        showToast(s)
    }

    override fun setPresenter(presenter: RegistrationContract.MobileVerificationPresenter?) {
        mMobileVerificationPresenter = presenter
    }

    override fun showToast(s: String?) {
        Toaster.showToast(this, s)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onOtpVerificationSuccess()
                } else {
                    onOtpVerificationFailed(task.exception?.message)
//                    Toast.makeText(
//                        this@MobileVerificationActivity,
//                        task.exception?.message,
//                        Toast.LENGTH_LONG
//                    ).show()
                }
            }
    }


    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallBack) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                verificationId = s
            }


            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    mEtOtp?.setText(code)
                    verifyCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onOtpVerificationFailed(e.message)
//                Toast.makeText(this@MobileVerificationActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }

    private fun verifyCode(code: String) {
        val credential = verificationId?.let { PhoneAuthProvider.getCredential(it, code) }
        credential?.let { signInWithCredential(it) }
    }
}