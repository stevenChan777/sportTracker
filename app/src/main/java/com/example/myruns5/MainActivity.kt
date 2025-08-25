package com.example.myruns5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import java.util.ArrayList

/**
 * Main activity for the MyRuns2 application which sets up a ViewPager2 with a TabLayout to allow users to navigate
 * between different sections of the app: Start, History, and Settings.
 */

class MainActivity : AppCompatActivity() {
    // Fragment instances for each tab
    private lateinit var start: Start
    private lateinit var history: History
    private lateinit var settings: Settings

    // ViewPager2 and TabLayout for tabbed navigation
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout

    // Adapter for managing the fragments in ViewPager2
    private lateinit var myMyFragmentStateAdapter: MyFragmentStateAdapter
    private lateinit var fragments: ArrayList<Fragment>

    // Titles for each tab
    private val tabTitles = arrayOf("Start", "History", "Settings")

    // Configuration and Mediator  for setting up tabs with their titles and linking ViewPager2
    private lateinit var tabConfigurationStrategy: TabConfigurationStrategy
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup the toolbar
        val toolbar = findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "MyRuns5"

        // Check and request necessary permissions
        if (!Util.hasAllPermissions(this)) {
            Util.requestPermissions(this)
        }
        setupUI()
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Util.handlePermissionResult(
            requestCode,
            grantResults,
            onPermissionsGranted = {
                setupUI()
            }
        )
    }

    // Function to initialize the app UI
    private fun setupUI() {
        // Initialize ViewPager2, TabLayout, and other components
        viewPager2 = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tab)

        // Initialize fragments
        start = Start()
        history = History()
        settings = Settings()

        // Adding fragments to the list
        fragments = ArrayList()
        fragments.add(start)
        fragments.add(history)
        fragments.add(settings)

        // Setting up the adapter for ViewPager2
        myMyFragmentStateAdapter = MyFragmentStateAdapter(this, fragments)
        viewPager2.adapter = myMyFragmentStateAdapter

        // Setting up the TabLayout with the ViewPager2
        tabConfigurationStrategy = TabConfigurationStrategy { tab, position ->
            tab.text = tabTitles[position]
        }
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()
    }

    //Destroy the activity and detach the mediator
    override fun onDestroy() {
        super.onDestroy()
        if (::tabLayoutMediator.isInitialized) {
            tabLayoutMediator.detach()
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.onDestroyView()
    }


}