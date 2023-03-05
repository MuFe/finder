package com.camera.finder.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.mufe.mvvm.library.extension.toast
import java.io.*


class GoogleUtil(private val mContext: Context, val preferenceUtil: PreferenceUtil) {
    private var mBillingClient: BillingClient? = null
    private var haveSub = false
    private lateinit var activity:Activity
    val subId = "com.camera.finder.week"
    val subId1 = "com.camera.finder.month"
    val subId2 = "com.camera.finder.year"
    var price1 = ""
    var price2 = ""
    var price3 = ""
    fun initGoogle(mActivity: Activity,listener: () -> Unit) {
        activity=mActivity
        if (mBillingClient == null) {
            mBillingClient = BillingClient.newBuilder(mContext)
                .setListener(object : PurchasesUpdatedListener {
                    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
                        if (p0.responseCode == BillingClient.BillingResponseCode.OK && p1 != null) {
                            handlePurchase(p1.get(0))
                        }
                    }
                })
                .enablePendingPurchases()
                .build();
            if (!mBillingClient!!.isReady()) {
                mBillingClient!!.startConnection(object : BillingClientStateListener {
                    override fun onBillingServiceDisconnected() {
                        Log.e("TAG","1111")
                        mContext.toast("谷歌服务不可用")
                    }

                    override fun onBillingSetupFinished(p0: BillingResult) {
                        if (p0.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            //这里代表接通谷歌成功
                            checkSub(listener)
                            getDetail() {

                            }
                        } else {
                            Log.e("TAG","11112")
                            mContext.toast("谷歌服务不可用")
                        }
                    }
                });
            }
        }
    }

    fun handlePurchase(purchase: Purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                val acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                mBillingClient!!.acknowledgePurchase(acknowledgePurchaseParams,
                    AcknowledgePurchaseResponseListener {

                    });
            }
        }
    }

    fun buy(index: Int) {
        startBuy(index)
    }

    fun startBuy(index: Int) {
        var id = ""
        if (index == 0) {
            id = subId
        } else if (index == 1) {
            id = subId1
        } else if(index==2){
            id = subId2
        }else{
            id = subId1
        }
        val str = preferenceUtil.getDetail(id)
        if (!str.equals("")) {
            val temp = SkuDetails(str)
            val purchaseParams = BillingFlowParams.newBuilder()
                .setSkuDetails(temp)
                .build()
            val response = mBillingClient!!.launchBillingFlow(activity, purchaseParams)
            if (response.getResponseCode() == 0) {
                // 调起成功
            } else {
                // 调起失败
            }
        }
    }

    fun isHaveSub(): Boolean {
        return haveSub
    }

    fun checkSub(listener: () -> Unit) {
        mBillingClient!!.queryPurchasesAsync(
            BillingClient.SkuType.SUBS,
            PurchasesResponseListener() { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                ) {
                    for (purchase in purchasesList) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            for (str in purchase.skus) {
                                if (str.equals(subId)) {
                                    haveSub = true
                                } else if (str.equals(subId1)) {
                                    haveSub = true
                                } else if(str.equals(subId2)){
                                    haveSub = true
                                }
                            }
                        }
                    }
                    if (!haveSub) {
                        listener()
                    }
                }
            })
    }

    fun getDetail(listener: () -> Unit) {
        if (price1.equals("") || price2.equals("") || price3.equals("")) {
            val skuList = mutableListOf<String>()
            skuList.add(subId)
            skuList.add(subId1)
            skuList.add(subId2)
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
            mBillingClient?.querySkuDetailsAsync(params.build(),
                SkuDetailsResponseListener { billingResult, skuDetailsList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                        && skuDetailsList != null && skuDetailsList.size > 0
                    ) {
                        for (v in skuDetailsList) {
                            preferenceUtil.saveDetail(v.sku,v.originalJson)
                            if (v.sku.equals(subId)) {
                                price1 = "" + v.priceAmountMicros / 1000000
                            } else if (v.sku.equals(subId1)) {
                                price2 = "" + v.priceAmountMicros / 1000000
                            }else if (v.sku.equals(subId2)) {
                                price3 = "" + v.priceAmountMicros / 1000000
                            }
                        }
                        listener()
                    } else {
                        mContext.toast("请联系管理员")
                    }
                })
        } else {
            listener()
        }
    }
}


