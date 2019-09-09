package io.horizontalsystems.bankwallet.core.factories

import com.nhaarman.mockito_kotlin.mock
import io.horizontalsystems.bankwallet.entities.*
import io.horizontalsystems.bankwallet.entities.Currency
import io.horizontalsystems.bankwallet.modules.transactions.TransactionStatus
import io.horizontalsystems.bankwallet.modules.transactions.TransactionViewItem
import org.junit.Assert
import org.junit.Test
import java.util.*

class TransactionViewItemFactoryTest {

    private val txViewItemFactory = TransactionViewItemFactory(mock())
    private val fromTxAddress = TransactionAddress("fromAddress", false)
    private val myTxAddress = TransactionAddress("myAddress", true)
    private val toTxAddress = TransactionAddress("toAddress", false)
    private val hash = "efwewegweg32rf234"
    private val bitCoin = Coin("Bitcoin", "BTC", 8, CoinType.Bitcoin)
    private val bitWallet = Wallet(bitCoin, mock(), mock())
    private val rate = CurrencyValue(Currency(code = "USD", symbol = "$"), 3900.toBigDecimal())
    private val lastBlockHeight = 1000

    private val txRecordOutgoing = TransactionRecord(
            transactionHash = hash,
            transactionIndex = 2,
            interTransactionIndex = 3,
            blockHeight = 900,
            amount = (-123).toBigDecimal(),
            timestamp = 1553769996L,
            from = listOf(myTxAddress),
            to = listOf(toTxAddress)
    )

    private val txRecordIncoming = TransactionRecord(
            transactionHash = hash,
            transactionIndex = 2,
            interTransactionIndex = 3,
            blockHeight = 900,
            amount = 123.toBigDecimal(),
            timestamp = 1553769996L,
            from = listOf(fromTxAddress),
            to = listOf(myTxAddress)
    )

    @Test
    fun getItem_incoming() {
        val txItem = TransactionItem(bitWallet, txRecordIncoming)
        val currencyValue = CurrencyValue(rate.currency, txRecordIncoming.amount * rate.value)
        val incoming = true

        val expectedItem = TransactionViewItem(
                bitWallet,
                txRecordIncoming.transactionHash,
                CoinValue(txItem.wallet.coin.code, txRecordIncoming.amount),
                currencyValue,
                null,
                txRecordIncoming.from.firstOrNull { it.mine != incoming }?.address,
                null,
                false,
                false,
                incoming,
                Date(txRecordIncoming.timestamp * 1000),
                TransactionStatus.Completed,
                rate
        )

        val item = txViewItemFactory.item(bitWallet, txItem, lastBlockHeight, 6, rate)
        Assert.assertEquals(expectedItem, item)
    }

    @Test
    fun getItem_outgoing() {
        val txItem = TransactionItem(bitWallet, txRecordOutgoing)
        val currencyValue = CurrencyValue(rate.currency, txRecordOutgoing.amount * rate.value)
        val incoming = false

        val expectedItem = TransactionViewItem(
                bitWallet,
                txRecordOutgoing.transactionHash,
                CoinValue(txItem.wallet.coin.code, txRecordOutgoing.amount),
                currencyValue,
                null,
                null,
                toTxAddress.address,
                false,
                false,
                incoming,
                Date(txRecordOutgoing.timestamp * 1000),
                TransactionStatus.Completed,
                rate
        )

        val item = txViewItemFactory.item(bitWallet, txItem, lastBlockHeight, 6, rate)
        Assert.assertEquals(expectedItem, item)
    }

    @Test
    fun getItem_forTransactionToYourself() {

        val txRecordToMyself = TransactionRecord(
                transactionHash = hash,
                transactionIndex = 2,
                interTransactionIndex = 3,
                blockHeight = 900,
                amount = 123.toBigDecimal(),
                timestamp = 1553769996L,
                from = listOf(myTxAddress),
                to = listOf(myTxAddress)
        )

        val txItem = TransactionItem(bitWallet, txRecordToMyself)
        val currencyValue = CurrencyValue(rate.currency, txRecordToMyself.amount * rate.value)
        val incoming = true

        val expectedItem = TransactionViewItem(
                bitWallet,
                txRecordToMyself.transactionHash,
                CoinValue(txItem.wallet.coin.code, txRecordToMyself.amount),
                currencyValue,
                null,
                null,
                null,
                true,
                false,
                incoming,
                Date(txRecordToMyself.timestamp * 1000),
                TransactionStatus.Completed,
                rate
        )

        val item = txViewItemFactory.item(bitWallet, txItem, lastBlockHeight, 6, rate)

        Assert.assertEquals(expectedItem, item)
    }

}
