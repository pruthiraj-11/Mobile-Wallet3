package org.mifos.mobilewallet.mifospay.common.presenter

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.mifos.mobilewallet.core.base.UseCaseHandler
import org.mifos.mobilewallet.core.data.fineract.entity.accounts.savings.SavingAccount
import org.mifos.mobilewallet.core.data.fineract.entity.accounts.savings.TransferDetail
import org.mifos.mobilewallet.core.domain.model.Account
import org.mifos.mobilewallet.core.domain.model.Currency
import org.mifos.mobilewallet.core.domain.model.Transaction
import org.mifos.mobilewallet.core.domain.model.TransactionType
import org.mifos.mobilewallet.core.domain.model.client.Client
import org.mifos.mobilewallet.core.domain.model.client.Client1
import org.mifos.mobilewallet.core.domain.usecase.account.TransferFunds
import org.mifos.mobilewallet.core.domain.usecase.client.SearchClient
import org.mifos.mobilewallet.core.utils.DateHelper.getDateAsStringFromLong
import org.mifos.mobilewallet.mifospay.base.BaseView
import org.mifos.mobilewallet.mifospay.common.TransferContract
import javax.inject.Inject

/**
 * Created by naman on 30/8/17.
 */
class MakeTransferPresenter @Inject constructor(private val mUsecaseHandler: UseCaseHandler) :
    TransferContract.TransferPresenter {
    @JvmField
    @Inject
    var transferFunds: TransferFunds? = null

    @JvmField
    @Inject
    var searchClient: SearchClient? = null
    private var mTransferView: TransferContract.TransferView? = null
    override fun attachView(baseView: BaseView<*>?) {
        mTransferView = baseView as TransferContract.TransferView?
        mTransferView?.setPresenter(this)
    }

    override fun fetchClient(externalId: String?) {
//        mUsecaseHandler.execute(searchClient, SearchClient.RequestValues(externalId),
//            object : UseCaseCallback<SearchClient.ResponseValue?> {
//                override fun onSuccess(response: SearchClient.ResponseValue?) {
//                    val searchResult = response?.results?.get(0)
//                    searchResult?.resultId?.let {
//                        mTransferView?.showToClientDetails(
//                            it.toLong(),
//                            searchResult.resultName, externalId
//                        )
//                    }
//                }
//
//                override fun onError(message: String) {
//                    mTransferView?.showVpaNotFoundSnackbar()
//                }
//            })
        val mFirebaseDatabaseInstances: FirebaseDatabase?
        val mFirebaseDatabase: DatabaseReference?
        mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()
        mFirebaseDatabase= mFirebaseDatabaseInstances.getReference().child("moneypay").child("RegisteredClients")
        mFirebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val client=snapshot.getValue(Client1::class.java)
                        if (client?.externalId.equals(externalId)) {
                            client?.clientId?.let {
                                mTransferView?.showToClientDetails(it, client.name, externalId)
                                return
                            }
                        }
                    }
                    mTransferView?.showVpaNotFoundSnackbar()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun makeTransfer(fromClientId: Long, toClientId: Long, amount: Double) {
//        mTransferView?.enableDragging(false)
//        mUsecaseHandler.execute(transferFunds,
//            TransferFunds.RequestValues(fromClientId, toClientId, amount),
//            object : UseCaseCallback<TransferFunds.ResponseValue?> {
//                override fun onSuccess(response: TransferFunds.ResponseValue?) {
//                    mTransferView?.enableDragging(true)
//                    mTransferView?.transferSuccess()
//                }
//
//                override fun onError(message: String) {
//                    mTransferView?.enableDragging(true)
//                    mTransferView!!.transferFailure()
//                }
//            })
        var toAccountId: Long? =null
        var fromAccountId: Long? =null
        var account1: Account?= null
        var account2: Account?= null
        val mFirebaseDatabaseInstances: FirebaseDatabase?
        val mFirebaseDatabase: DatabaseReference?
        mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()
        mFirebaseDatabase= mFirebaseDatabaseInstances.getReference().child("moneypay").child("accounts")
        mFirebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        if (snapshot.key!!.equals(fromClientId)) {
                            account1=snapshot.getValue(Account::class.java)
                            fromAccountId= account1!!.id
                        }
                        if (snapshot.key!!.equals(toClientId)) {
                            account2=snapshot.getValue(Account::class.java)
                            toAccountId=account2!!.id
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        val date: String= getDateAsStringFromLong(System.currentTimeMillis())
        val trID: Long= 4742344
        val tnID : Long= 469946
        val receiptId: Long=62763881
        account1!!.balance -= amount
        account2!!.balance += amount
        FirebaseDatabase.getInstance().getReference().child("moneypay").child("accounts").child(fromClientId.toString()).setValue(account1);
        FirebaseDatabase.getInstance().getReference().child("moneypay").child("accounts").child(toClientId.toString()).setValue(account2);
        val debittranscation = Transaction()
        val tFID=trID+1
        val tnID1=tnID+1
        debittranscation.transferId=tFID
        debittranscation.transactionId=tnID1.toString()
        debittranscation. clientId=fromClientId
        debittranscation.accountId= fromAccountId!!
        debittranscation.amount=amount
        debittranscation.date= date
        val currency= Currency()
        currency.code="INR"
        currency.displaySymbol= "₹"
        currency.displayLabel= "INR"
        debittranscation.currency=currency
        debittranscation.transactionType= TransactionType.entries[0]
        val tempRID=receiptId+1
        debittranscation.receiptId=tempRID.toString()
        val transferDetail= TransferDetail()
        transferDetail.id=tFID
        transferDetail.fromClient= Client()
        transferDetail.toClient= Client()
        transferDetail.fromAccount= SavingAccount()
        transferDetail.toAccount= SavingAccount()
        FirebaseDatabase.getInstance().getReference().child("moneypay").child("transactions").child(fromAccountId.toString()).setValue(debittranscation)
        val credittranscation = Transaction()
        credittranscation.transferId=tFID
        credittranscation.transactionId=tnID1.toString()
        credittranscation. clientId=toClientId
        credittranscation.accountId= toAccountId!!
        credittranscation.amount=amount
        credittranscation.date= date
        val currency1= Currency()
        currency.code="INR"
        currency.displaySymbol= "₹"
        currency.displayLabel= "INR"
        credittranscation.currency=currency
        credittranscation.transactionType= TransactionType.entries[1]
        credittranscation.receiptId=tempRID.toString()
        val transferDetail1= TransferDetail()
        transferDetail.id=tFID
        transferDetail.fromClient= Client()
        transferDetail.toClient= Client()
        transferDetail.fromAccount= SavingAccount()
        transferDetail.toAccount= SavingAccount()
        FirebaseDatabase.getInstance().getReference().child("moneypay").child("transactions").child(toClientId.toString()).setValue(credittranscation)
        mTransferView?.enableDragging(true)
        mTransferView?.transferSuccess()
    }
}