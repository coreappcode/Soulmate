package com.example.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class GooglePlayPlan(
    val productId: String,
    val displayName: String,
    val formattedPrice: String,
    val details: ProductDetails,
    val offerToken: String
)

class GooglePlayBillingManager(
    context: Context,
    private val scope: CoroutineScope,
    private val verifyPurchase: suspend (String, String) -> Unit,
    private val onAccessUpdated: () -> Unit
) {
    private val _plans = MutableStateFlow<List<GooglePlayPlan>>(emptyList())
    val plans: StateFlow<List<GooglePlayPlan>> = _plans.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val productDetailsById = mutableMapOf<String, GooglePlayPlan>()
    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(PurchasesUpdatedListener(::onPurchasesUpdated))
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun connect() {
        if (billingClient.isReady) {
            onConnected()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    _message.value = result.debugMessage.ifBlank {
                        "Google Play billing is unavailable right now."
                    }
                    return
                }
                _isReady.value = true
                onConnected()
            }

            override fun onBillingServiceDisconnected() {
                _isReady.value = false
            }
        })
    }

    fun purchase(activity: Activity, productId: String) {
        val plan = productDetailsById[productId]
        if (!billingClient.isReady || plan == null) {
            _message.value = "This plan is not available from Google Play right now."
            return
        }
        _message.value = null
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(plan.details)
            .setOfferToken(plan.offerToken)
            .build()
        val result = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build()
        )
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _message.value = result.debugMessage.ifBlank { "Google Play could not start checkout." }
        }
    }

    fun restorePurchases() {
        queryOwnedPurchases(showEmptyMessage = true)
    }

    private fun queryOwnedPurchases(showEmptyMessage: Boolean) {
        if (!billingClient.isReady) {
            _message.value = "Google Play is still connecting. Try restoring again shortly."
            return
        }
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _message.value = result.debugMessage.ifBlank {
                    "Google Play could not restore purchases."
                }
                return@queryPurchasesAsync
            }
            val hasPurchasedSubscription =
                purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            purchases.forEach(::processPurchase)
            if (showEmptyMessage && !hasPurchasedSubscription &&
                purchases.none { it.purchaseState == Purchase.PurchaseState.PENDING }
            ) {
                _message.value = "No active Soulmate Pro purchase was found for this Google account."
            }
        }
    }

    fun disconnect() {
        _isReady.value = false
        billingClient.endConnection()
    }

    private fun onConnected() {
        queryPlans()
        queryOwnedPurchases(showEmptyMessage = false)
    }

    private fun queryPlans() {
        val products = listOf(
            "soulmate_pro_monthly",
            "soulmate_pro_quarterly",
            "soulmate_pro_yearly"
        ).map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()

        billingClient.queryProductDetailsAsync(
            params,
            ProductDetailsResponseListener { result, queryResult ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    _message.value = result.debugMessage.ifBlank {
                        "Google Play could not load subscription plans."
                    }
                    return@ProductDetailsResponseListener
                }
                val planLabels = mapOf(
                    "soulmate_pro_monthly" to "Monthly",
                    "soulmate_pro_quarterly" to "Quarterly",
                    "soulmate_pro_yearly" to "Yearly"
                )
                val availablePlans = queryResult.productDetailsList.mapNotNull { details ->
                    val productId = details.productId
                    val offer = details.subscriptionOfferDetails?.firstOrNull { it.offerId == null }
                        ?: return@mapNotNull null
                    val formattedPrice = offer.pricingPhases.pricingPhaseList
                        .lastOrNull()?.formattedPrice
                        ?: return@mapNotNull null
                    val plan = GooglePlayPlan(
                        productId = productId,
                        displayName = planLabels[productId] ?: details.title,
                        formattedPrice = formattedPrice,
                        details = details,
                        offerToken = offer.offerToken
                    )
                    productDetailsById[productId] = plan
                    plan
                }
                _plans.value = availablePlans.sortedBy { plan ->
                    planLabels.keys.indexOf(plan.productId)
                }
                if (availablePlans.isEmpty()) {
                    _message.value =
                        "Soulmate Pro plans are not available in Google Play yet."
                }
            }
        )
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach(::processPurchase)
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _message.value = result.debugMessage.ifBlank {
                "Google Play could not complete this purchase."
            }
        }
    }

    private fun processPurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING ->
                _message.value = "Purchase is pending. Pro will activate after Google confirms payment."
            Purchase.PurchaseState.PURCHASED -> {
                val allowedProducts = setOf(
                    "soulmate_pro_monthly",
                    "soulmate_pro_quarterly",
                    "soulmate_pro_yearly"
                )
                val productId = purchase.products.firstOrNull { it in allowedProducts }
                if (productId == null) {
                    _message.value = "Google Play returned an unrecognized Soulmate Pro plan."
                    return
                }
                scope.launch(Dispatchers.IO) {
                    _isProcessing.value = true
                    try {
                        verifyPurchase(purchase.purchaseToken, productId)
                        if (!purchase.isAcknowledged) {
                            acknowledge(purchase.purchaseToken)
                        }
                        _message.value = "Soulmate Pro is active."
                        onAccessUpdated()
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        _message.value = exception.localizedMessage
                            ?: "Google Play could not verify the purchase."
                    } finally {
                        _isProcessing.value = false
                    }
                }
            }
            else -> Unit
        }
    }

    private suspend fun acknowledge(purchaseToken: String) {
        suspendCancellableCoroutine { continuation ->
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchaseToken)
                    .build()
            ) { result ->
                if (!continuation.isActive) return@acknowledgePurchase
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(
                        IllegalStateException(
                            result.debugMessage.ifBlank {
                                "Subscription is verified, but Google Play acknowledgement failed."
                            }
                        )
                    )
                }
            }
        }
    }
}
