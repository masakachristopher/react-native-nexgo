package com.reactnativenexgo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.*
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

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun printReceipt(
   receipt: ReadableMap,
   promise: Promise){

    try {
      //Initialize the SDK components
      deviceEngine = APIProxy.getDeviceEngine(reactApplicationContext)
      printer = deviceEngine?.printer


      //Initialize the printer
      printer?.initPrinter()

      // Get printer status from getStatus()
      when (val initResult: Int? = printer?.status) {  // same is printer?.getStatus()
        SdkResult.Success -> {
          printMerchantSummary(
            receipt,
            ""
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

    } catch (e : Exception){
      promise.reject("PRINT_RECEIPT_ERROR", e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun printMerchantSummary(
   receipt: ReadableMap,
   base64encodedImage: String
  ){
    try {
      // initialize date
      val current  = LocalDateTime.now()

      val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
      val formatted = current.format(formatter)

      //Add start of the receipt text on top of receipt
      printer?.appendPrnStr("***** MERCHANT COPY *****",26, AlignEnum.CENTER, true)

      // Company Logo
      val receiptLogo: Bitmap? = stringToBitMap(base64encodedImage)
      if (receiptLogo != null) {
        printer?.appendImage(
          receiptLogo,
          AlignEnum.CENTER
        )
      }
      // End Logo to the receipt

      // Checkout Details
      val receiptMap = receipt.toHashMap() // convert readable map to hashmap

      for((key, value) in receiptMap) {
        printer?.appendPrnStr(key,value.toString(), 24, false)
      }
      // Time
      printer?.appendPrnStr("Date",formatted.toString(),24,false)

      // End of receipt
      printer?.appendPrnStr("--- END OF LEGAL RECEIPT ---",26, AlignEnum.CENTER, true)

      //Start the print job
      printer?.startPrint(true, this)
    } catch (e : Exception){
      Log.e(TAG, "printMerchantSummary: ", e)
    }

  }


  private fun stringToBitMap(encodedString: String?): Bitmap? {
    return try {
      val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
      BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    } catch (e: Exception) {
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
