package com.pizza.giros.burger.goodstreetfood.Activiy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain
import com.pizza.giros.burger.goodstreetfood.R
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.mainform.MainFormLauncher
import ru.tinkoff.acquiring.sdk.utils.Money
import java.math.BigDecimal


class PaymentActivity : AppCompatActivity() {

    private lateinit var imageView5: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var textView22: TextView
    private lateinit var textView23: TextView
    private lateinit var textView20: TextView
    private lateinit var textView17: TextView

    private val launcher = registerForActivityResult(MainFormLauncher.Contract) { result ->
        handlePaymentResult(result)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        bottomNavigation()

        imageView5 = findViewById(R.id.imageView5)
        imageView4 = findViewById(R.id.imageView4)
        textView22 = findViewById(R.id.textView22)
        textView23 = findViewById(R.id.textView23)
        textView20 = findViewById(R.id.textView20)
        textView17 = findViewById(R.id.textView17)

        val textViewPaymentOptions = findViewById<TextView>(R.id.textView_payment_options)
        textViewPaymentOptions.setOnClickListener {
            showPaymentOptions(it)
        }

        AcquiringSdk.isDebug = false
        AcquiringSdk.isDeveloperMode = false

        textView17.setOnClickListener {

            startPayment()
        }

        textView23.setOnClickListener {
            val intent = Intent(this@PaymentActivity, ZakazNalActivity::class.java)
            val totalAmount = getIntent().getIntExtra("TOTAL_AMOUNT", 0)
            val foodDomains =
                getIntent().getSerializableExtra("foodDomains") as ArrayList<FoodDomain>?

            intent.putExtra("TOTAL_AMOUNT", totalAmount)
            intent.putExtra("foodDomains", foodDomains)
            startActivity(intent)
        }
    }

    private fun startPayment() {
        // Генерация уникального номера заказа (например, с использованием временной метки)
        val ORDERID = System.currentTimeMillis().toString()
        val totalTxt = intent.getIntExtra("TOTAL_AMOUNT", 0)
        val options = PaymentOptions().setOptions {
            setTerminalParams("1714766466399", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5yse9ka3ZQE0feuGtemYv3IqOlLck8zHUM7lTr0za6lXTszRSXfUO7jMb+L5C7e2QNFs+7sIX2OQJ6a+HG8kr+jwJ4tS3cVsWtd9NXpsU40PE4MeNr5RqiNXjcDxA+L4OsEm/BlyFOEOh2epGyYUd5/iO3OiQFRNicomT2saQYAeqIwuELPs1XpLk9HLx5qPbm8fRrQhjeUD5TLO8b+4yCnObe8vy/BMUwBfq+ieWADIjwWCMp2KTpMGLz48qnaD9kdrYJ0iyHqzb2mkDhdIzkim24A3lWoYitJCBrrB2xM05sm9+OdCI1f7nPNJbl5URHobSwR94IRGT7CJcUjvwIDAQAB")
            orderOptions {
                orderId = ORDERID
                title = "Ваш заказ"
                description = "Оплата заказа"
                amount = Money.ofRubles(BigDecimal(totalTxt))
                recurrentPayment = false
            }
        }
        val startData = MainFormLauncher.StartData(paymentOptions = options)
        launcher.launch(startData)
    }

    private fun handlePaymentResult(result: MainFormLauncher.Result) {
        when (result) {
            MainFormLauncher.Canceled -> {
                Toast.makeText(this, "Отменено", Toast.LENGTH_LONG).show()
            }
            is MainFormLauncher.Error -> {
                Toast.makeText(
                    this,
                    "Ошибка: ${result.error.message} Код: ${result.errorCode}",
                    Toast.LENGTH_LONG
                ).show()
            }
            is MainFormLauncher.Success -> {
                Toast.makeText(
                    this,
                    "Успешная оплата",
                    Toast.LENGTH_LONG
                ).show()
                handleSuccessfulTokenization()
            }
        }
    }

    private fun showPaymentOptions(view: View) {
        val options = arrayOf("Оплата картой", "Оплата наличными")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите тип оплаты")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        textView23.visibility = View.GONE
                        imageView4.visibility = View.GONE
                        textView20.visibility = View.GONE
                        textView17.visibility = View.VISIBLE
                        imageView5.visibility = View.VISIBLE
                        textView22.visibility = View.VISIBLE
                    }
                    1 -> {
                        textView23.visibility = View.VISIBLE
                        imageView4.visibility = View.VISIBLE
                        textView20.visibility = View.VISIBLE
                        textView17.visibility = View.GONE
                        imageView5.visibility = View.GONE
                        textView22.visibility = View.GONE
                    }
                }
            }
        builder.create().show()
    }
    private fun handleSuccessfulTokenization() {
        val intent = Intent(this@PaymentActivity, ZakazActivity::class.java)
        val totalAmount = getIntent().getIntExtra("TOTAL_AMOUNT", 0)
        val foodDomains =
            getIntent().getSerializableExtra("foodDomains") as ArrayList<FoodDomain>?

        intent.putExtra("TOTAL_AMOUNT", totalAmount)
        intent.putExtra("foodDomains", foodDomains)
        startActivity(intent)
    }

    private fun bottomNavigation() {
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.card_btn)
        val homeBtn = findViewById<LinearLayout>(R.id.homeBtn)
        val settingsBtn = findViewById<LinearLayout>(R.id.settings_btn)
        val profileBtn = findViewById<LinearLayout>(R.id.profile_btn)
        val helpBtn = findViewById<LinearLayout>(R.id.help_btn)

        floatingActionButton.setOnClickListener {
            startActivity(Intent(this@PaymentActivity, CardListActivity::class.java))
        }

        homeBtn.setOnClickListener {
            startActivity(Intent(this@PaymentActivity, MainActivity::class.java))
        }

        settingsBtn.setOnClickListener {
            startActivity(Intent(this@PaymentActivity, SettingsActivity::class.java))
        }

        profileBtn.setOnClickListener {
            startActivity(Intent(this@PaymentActivity, ProfileActivity::class.java))
        }

        helpBtn.setOnClickListener {
            startActivity(Intent(this@PaymentActivity, HelpActivity::class.java))
        }
    }
}
