package ru.tinkoff.acquiring.sdk.localization;

import com.google.gson.annotations.SerializedName;

/**
 * @author a.shishkin1
 */
public class AsdkLocalization {

    @SerializedName("Pay.Title") public String payTitle;

    @SerializedName("Pay.Card.NewCard") public String payCardNewCard;

    @SerializedName("Pay.Card.SavedCard") public String payCardSavedCard;

    @SerializedName("Pay.Card.ChangeCard") public String payCardChangeCard;

    @SerializedName("Pay.Card.ChooseLinkedCard") public String payCardChooseLinkedCard;

    @SerializedName("Pay.Card.PanHint") public String payCardPanHint;

    @SerializedName("Pay.Card.PanHint.Recurrent") public String payCardPanHintRecurrent;

    @SerializedName("Pay.Card.ExpireDateHint") public String payCardExpireDateHint;

    @SerializedName("Pay.Card.CvcHint") public String payCardCvcHint;

    @SerializedName("Pay.Money.Amount") public String payMoneyAmount;

    @SerializedName("Pay.Email") public String payEmail;

    @SerializedName("Pay.PayButton") public String payPayButton;

    @SerializedName("Pay.Dialog.Error.Title") public String payDialogErrorTitle;

    @SerializedName("Pay.Dialog.Error.FallbackMessage") public String payDialogErrorFallbackMessage;

    @SerializedName("Pay.Dialog.Error.Network") public String payDialogErrorNetwork;

    @SerializedName("Pay.Dialog.Error.AcceptButton") public String payDialogErrorAcceptButton;

    @SerializedName("Pay.Dialog.Validation.Title") public String payDialogValidationTitle;

    @SerializedName("Pay.Dialog.Validation.InvalidEmail") public String payDialogValidationInvalidEmail;

    @SerializedName("Pay.Dialog.Validation.InvalidCard") public String payDialogValidationInvalidCard;

    @SerializedName("Pay.Dialog.Progress.PayMessage") public String payDialogProgressPayMessage;

    @SerializedName("Pay.Dialog.CardScan.Nfc") public String payDialogCardScanNfc;

    @SerializedName("Pay.Dialog.CardScan.Camera") public String payDialogCardScanCamera;

    @SerializedName("Pay.Dialog.Cvc.Message") public String payDialogCvcMessage;

    @SerializedName("Pay.Dialog.Cvc.AcceptButton") public String payDialogCvcAcceptButton;

    @SerializedName("Pay.Nfc.Fail") public String payNfcFail;

    @SerializedName("Pay.NoScanProviders") public String payNoScanProviders;

    @SerializedName("ConfirmationLoop.Title") public String confirmationLoopTitle;

    @SerializedName("ConfirmationLoop.Description") public String confirmationLoopDescription;

    @SerializedName("ConfirmationLoop.Amount") public String confirmationLoopAmount;

    @SerializedName("ConfirmationLoop.Dialog.Validation.InvalidAmount") public String confirmationLoopDialogValidationInvalidAmount;

    @SerializedName("ConfirmationLoop.CheckButton") public String confirmationLoopCheckButton;

    @SerializedName("Confirmation3DS.Title") public String confirmation3DSTitle;

    @SerializedName("AddCard.Title") public String addCardTitle;

    @SerializedName("AddCard.AddCardButton") public String addCardAddCardButton;

    @SerializedName("AddCard.Dialog.Error.Title") public String addCardDialogErrorTitle;

    @SerializedName("AddCard.Dialog.Error.Network") public String addCardDialogErrorNetwork;

    @SerializedName("AddCard.Nfc.Fail") public String addCardNfcFail;

    @SerializedName("AddCard.Dialog.Progress.AddCardMessage") public String addCardDialogProgressAddCardMessage;

    @SerializedName("AddCard.Dialog.Error.FallbackMessage") public String addCardDialogErrorFallbackMessage;

    @SerializedName("AddCard.NoScanProviders") public String addCardNoScanProviders;

    @SerializedName("CardList.Title") public String cardListTitle;

    @SerializedName("CardList.NewCard") public String cardListNewCard;

    @SerializedName("CardList.Delete") public String cardListDelete;

    @SerializedName("CardList.RemoveCard.FailMessage") public String cardListRemoveCardFailMessage;

    @SerializedName("Nfc.Title") public String nfcTitle;

    @SerializedName("Nfc.Description") public String nfcDescription;

    @SerializedName("Nfc.CloseButton") public String nfcCloseButton;

    @SerializedName("Nfc.Dialog.Disable.Title") public String nfcDialogDisableTitle;

    @SerializedName("Nfc.Dialog.Disable.Message") public String nfcDialogDisableMessage;

}
