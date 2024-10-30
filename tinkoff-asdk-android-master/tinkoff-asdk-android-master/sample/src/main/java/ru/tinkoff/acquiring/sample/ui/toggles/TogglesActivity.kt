package ru.tinkoff.acquiring.sample.ui.toggles

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sdk.toggles.FeatureToggleManager
import ru.tinkoff.acquiring.sdk.toggles.Storage

/**
 * @author k.shpakovskiy
 */
class TogglesActivity : AppCompatActivity() {

    private lateinit var toggles: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toggles)

        val adapter = TogglesAdapter(FeatureToggleManager(Storage(applicationContext)))
        toggles = findViewById(R.id.toggles)
        toggles.layoutManager = LinearLayoutManager(this)
        toggles.adapter = adapter

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
