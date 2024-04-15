package org.mifos.mobilewallet.mifospay.payments.presenter

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import org.mifos.mobilewallet.core.base.UseCase.UseCaseCallback
import org.mifos.mobilewallet.core.base.UseCaseHandler
import org.mifos.mobilewallet.core.domain.model.Account
import org.mifos.mobilewallet.core.domain.usecase.account.FetchAccount
import org.mifos.mobilewallet.core.domain.usecase.client.FetchClientData
import org.mifos.mobilewallet.mifospay.base.BaseView
import org.mifos.mobilewallet.mifospay.data.local.LocalRepository
import org.mifos.mobilewallet.mifospay.home.BaseHomeContract
import org.mifos.mobilewallet.mifospay.utils.Constants
import javax.inject.Inject


@HiltViewModel
class TransferPresenter @Inject constructor(
    val mUsecaseHandler: UseCaseHandler,
    val localRepository: LocalRepository
) : ViewModel(), BaseHomeContract.TransferPresenter {

    @JvmField
    @Inject
    var mFetchAccount: FetchAccount? = null

    var mTransferView: BaseHomeContract.TransferView? = null
    override fun attachView(baseView: BaseView<*>?) {
        mTransferView = baseView as BaseHomeContract.TransferView?
        mTransferView!!.setPresenter(this)
    }

    override fun fetchVpa() {
        mTransferView!!.showVpa(localRepository.clientDetails.externalId)
    }

    override fun fetchMobile() {
        mTransferView!!.showMobile(localRepository.preferencesHelper.mobile)
    }

    override fun checkSelfTransfer(externalId: String?): Boolean {
        return externalId == localRepository.clientDetails.externalId
    }

    override fun checkBalanceAvailability(externalId: String?, transferAmount: Double) {
//        mUsecaseHandler.execute(mFetchAccount,
//            FetchAccount.RequestValues(localRepository.clientDetails.clientId),
//            object : UseCaseCallback<FetchAccount.ResponseValue> {
//                override fun onSuccess(response: FetchAccount.ResponseValue) {
//                    mTransferView!!.hideSwipeProgress()
//                    if (transferAmount > response.account.balance) {
//                        mTransferView!!.showSnackbar(Constants.INSUFFICIENT_BALANCE)
//                    } else {
//                        mTransferView!!.showClientDetails(externalId, transferAmount)
//                    }
//                }
//
//                override fun onError(message: String) {
//                    mTransferView!!.hideSwipeProgress()
//                    mTransferView!!.showToast(Constants.ERROR_FETCHING_BALANCE)
//                }
//            })
        val clientId: String= localRepository.clientDetails.clientId.toString()
        val mFirebaseDatabaseInstances: FirebaseDatabase?
        val mFirebaseDatabase: DatabaseReference?
        mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()
        mFirebaseDatabase= mFirebaseDatabaseInstances.getReference().child("moneypay").child("accounts")
        mFirebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        if (snapshot.key.equals(clientId)) {
                            val account1=snapshot.getValue(Account::class.java)
                            mTransferView!!.hideSwipeProgress()
                            if (transferAmount > account1!!.balance) {
                                mTransferView!!.showSnackbar(Constants.INSUFFICIENT_BALANCE)
                            } else {
                                mTransferView!!.showClientDetails(externalId, transferAmount)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                mTransferView!!.hideSwipeProgress()
                mTransferView!!.showToast(Constants.ERROR_FETCHING_BALANCE)
            }

        })
    }
}
