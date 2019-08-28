package io.horizontalsystems.bankwallet.modules.send

import io.horizontalsystems.bankwallet.modules.send.submodules.address.SendAddressModule
import io.horizontalsystems.bankwallet.modules.send.submodules.amount.SendAmountModule
import io.horizontalsystems.bankwallet.modules.send.submodules.fee.SendFeeModule
import io.reactivex.Single
import java.math.BigDecimal

class SendEosHandler(private val interactor: SendModule.ISendEosInteractor,
                     private val router: SendModule.IRouter) : SendModule.ISendHandler,
        SendAmountModule.IAmountModuleDelegate,
        SendAddressModule.IAddressModuleDelegate {

    private fun syncValidation() {
        try {
            amountModule.validAmount()
            addressModule.validAddress()

            delegate.onChange(true)

        } catch (e: Exception) {
            delegate.onChange(false)
        }
    }

    private fun syncAvailableBalance() {
        amountModule.setAvailableBalance(interactor.availableBalance)
    }

    // SendModule.ISendHandler

    override lateinit var amountModule: SendAmountModule.IAmountModule

    override lateinit var addressModule: SendAddressModule.IAddressModule

    override lateinit var feeModule: SendFeeModule.IFeeModule

    override val inputItems: List<SendModule.Input> = listOf(
            SendModule.Input.Amount,
            SendModule.Input.Address,
            SendModule.Input.ProceedButton)

    override lateinit var delegate: SendModule.ISendHandlerDelegate

    override fun confirmationViewItems(): List<SendModule.SendConfirmationViewItem> {
        TODO("not implemented")
    }

    override fun sendSingle(): Single<Unit> {
        return interactor.send(amountModule.validAmount(), addressModule.validAddress(), null)
    }

    override fun onModulesDidLoad() {
        syncAvailableBalance()
    }

    override fun onAddressScan(address: String) {
        addressModule.didScanQrCode(address)
    }

    // SendAmountModule.IAmountModuleDelegate

    override fun onChangeAmount() {
        syncValidation()
    }

    override fun onChangeInputType(inputType: SendModule.InputType) {

    }

    // SendAddressModule.IAddressModuleDelegate

    override fun validate(address: String) {
        interactor.validate(address)
    }

    override fun onUpdateAddress() {
        syncValidation()
    }

    override fun onUpdateAmount(amount: BigDecimal) {
        amountModule.setAvailableBalance(amount)
    }

    override fun scanQrCode() {
        router.scanQrCode()
    }

}
