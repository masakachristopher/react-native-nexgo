package com.reactnativenexgo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.nexgo.oaf.apiv3.APIProxy
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.SdkResult
import com.nexgo.oaf.apiv3.device.printer.AlignEnum
import com.nexgo.oaf.apiv3.device.printer.BarcodeFormatEnum
import com.nexgo.oaf.apiv3.device.printer.OnPrintListener
import com.nexgo.oaf.apiv3.device.printer.Printer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class NexgoModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext),
  OnPrintListener {
  private val TAG = "PrinterSample"
  private var deviceEngine: DeviceEngine? = null
  private var printer: Printer? = null

  override fun getName(): String {
    return "Nexgo"
  }


  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun printReceipt(
    coverName: String,
    insuredName: String,
    paymentMethod: String,
    paymentAccount: String,
    amount: String,
    promise: Promise){

    //Initialize the SDK components
    deviceEngine = APIProxy.getDeviceEngine(reactApplicationContext)
    printer = deviceEngine?.getPrinter()

    //Initialize the printer
    printer?.initPrinter()

    val initResult: Int? = printer?.getStatus()

    when (initResult) {
//        SdkResult.Success -> promise.resolve("Printer init success")
      SdkResult.Success -> {
        printMerchantSummary(
          coverName,
          insuredName,
          paymentMethod,
          paymentAccount,
          amount
        )
      }

      SdkResult.Printer_PaperLack -> {
        promise.resolve("Printer is out of paper")
        Toast.makeText(reactApplicationContext, "Out of Paper!", Toast.LENGTH_LONG).show()
      }
      else -> {
        promise.resolve( "Printer Init Misc Error: $initResult")
        Toast.makeText(reactApplicationContext, "Printer Init Misc Error: $initResult", Toast.LENGTH_LONG).show()
      }
    }

  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun printMerchantSummary(
    coverName: String,
    insuredName: String,
    paymentMethod: String,
    paymentAccount: String,
    amount: String,
  ){
    // initialize date
    val current  = LocalDateTime.now()

    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    val formatted = current.format(formatter)
    //Add sample text on top of receipt
    printer?.appendPrnStr("***** MERCHANT COPY *****",26, AlignEnum.CENTER, true)

    //Add Panda logo to the receipt
    val receiptLogo: Bitmap? = StringToBitMap(
      "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"
    )
    if (receiptLogo != null) {
      printer?.appendImage(
        receiptLogo,
        AlignEnum.CENTER
      )
    }
    // End Panda Logo to the receipt

    // Checkout Details
    printer?.appendPrnStr("NAME",insuredName, 24, false)
    printer?.appendPrnStr("COVER NAME",coverName, 24, false)
    printer?.appendPrnStr("AMOUNT (TZS)",amount, 24, false)
    printer?.appendPrnStr("PAYMENT METHOD",paymentMethod, 24, false)
    printer?.appendPrnStr("PAYMENT ACCOUNT", paymentAccount, 24, false)
    printer?.appendPrnStr("DATE",formatted.toString(),24,false)

    printer?.appendPrnStr("--- END OF LEGAL RECEIPT ---",26, AlignEnum.CENTER, true)

    //Start the print job
    printer?.startPrint(true, this)
  }


  fun StringToBitMap(encodedString: String?): Bitmap? {
    return try {
      val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
      BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    } catch (e: Exception) {
//        e.getMessage()
      null
    }
  }

  @Override
  override fun onPrintResult(resultCode: Int) {
    when (resultCode) {
      SdkResult.Success -> Log.d(TAG, "Printer job finished successfully!")
      SdkResult.Printer_Print_Fail -> Log.e(TAG, "Printer Failed: $resultCode")
      SdkResult.Printer_Busy -> Log.e(TAG, "Printer is Busy: $resultCode")
      SdkResult.Printer_PaperLack -> Log.e(TAG, "Printer is out of paper: $resultCode")
      SdkResult.Printer_Fault -> Log.e(TAG, "Printer fault: $resultCode")
      SdkResult.Printer_TooHot -> Log.e(TAG, "Printer temperature is too hot: $resultCode")
      SdkResult.Printer_UnFinished -> Log.w(TAG, "Printer job is unfinished: $resultCode")
      SdkResult.Printer_Other_Error -> Log.e(TAG, "Printer Other_Error: $resultCode")
      else -> Log.e(TAG, "Generic Fail Error: $resultCode")
    }
  }

}


