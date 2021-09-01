package com.codeshot.carscustomerapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.codeshot.carscustomerapp.Common.Common
import com.codeshot.carscustomerapp.nav_fragments.MapFragment
import com.codeshot.carscustomerapp.nav_fragments.ProfileFragment
import com.codeshot.carscustomerapp.nav_fragments.RequestsFragment
import com.codeshot.carscustomerapp.starting.LoginActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    //View
    private var toolbar: Toolbar? = null
    private var toggle: ActionBarDrawerToggle? = null
    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var nav_imgCustomerAtDrawer: CircleImageView? = null
    private var nav_usernameCustomerAtDrawer: TextView? = null
    private var nav_emailCustomerAtDrawer: TextView? = null
    private var atMap = true
    //Firebase
    private var mAuth: FirebaseAuth? = null
    private var currentUserID: String? = null
    private var rootRef: DatabaseReference? = null
    private var ridersRef: DatabaseReference? = null
    private var myRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initializations()
        setDrawer()
    }

    private fun initializations() { //Init View
        toolbar = findViewById(R.id.toolBarHomeActivity)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_customer_layout)
        navigationView = findViewById(R.id.navigationViewOfHomeActivity)
        toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        nav_imgCustomerAtDrawer = navigationView!!.getHeaderView(0).findViewById(R.id.nav_imgCustomerAtDrawer)
        nav_usernameCustomerAtDrawer = navigationView!!.getHeaderView(0).findViewById(R.id.nav_usernameCustomerAtDrawer)
        nav_emailCustomerAtDrawer = navigationView!!.getHeaderView(0).findViewById(R.id.nav_emailCustomerAtDrawer)
        rootRef = FirebaseDatabase.getInstance().reference
        ridersRef = rootRef!!.child(Common.riders_tbl)
        mAuth = FirebaseAuth.getInstance()
        if (mAuth!!.currentUser != null) currentUserID = mAuth!!.currentUser!!.uid
        myRef = ridersRef!!.child(currentUserID!!)
        retrieveUserData()
    }

    private fun retrieveUserData() {
        myRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("userName").exists()) {
                        val userName = dataSnapshot.child("userName").value.toString()
                        nav_usernameCustomerAtDrawer!!.text = userName
                    }
                    if (dataSnapshot.child("email").exists()) {
                        val email = dataSnapshot.child("email").value.toString()
                        nav_emailCustomerAtDrawer!!.text = email
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setDrawer() {
        drawerLayout!!.addDrawerListener(toggle!!)
        toggle!!.syncState()
        navigationView!!.setNavigationItemSelectedListener(this)
        //start main fragment
        supportFragmentManager.beginTransaction().replace(R.id.contentOfHomeActivity, MapFragment(this)).commit()
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_home_activity_drawer, menu);
//        return true;
//    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.nav_LogOut -> {
                mAuth!!.signOut()
                sendToLoginActivity()
            }
        }
        return true
    }

    private fun sendToLoginActivity() {
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.nav_Profile -> {
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
                supportFragmentManager.beginTransaction().replace(R.id.contentOfHomeActivity, ProfileFragment()).commit()
                atMap = false
            }
            R.id.nav_PaymentHistory -> {
                Toast.makeText(this, "PaymentHistory", Toast.LENGTH_SHORT).show()
                atMap = false
            }
            R.id.nav_RideHistory -> {
                supportFragmentManager.beginTransaction().replace(R.id.contentOfHomeActivity, RequestsFragment()).commit()
                Toast.makeText(this, "RideHistory", Toast.LENGTH_SHORT).show()
                atMap = false
            }
            R.id.nav_Notifications -> {
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
                atMap = false
            }
            R.id.nav_Help -> {
                Toast.makeText(this, "Help", Toast.LENGTH_SHORT).show()
                atMap = false
            }
            R.id.nav_LogOut -> {
                mAuth!!.signOut()
                sendToLoginActivity()
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    //
    override fun onStart() {
        super.onStart()
        //        loadDrivers();
        if (intent != null) {
            if (intent.getStringExtra("type") != null) {
                val message = intent.getStringExtra("message")
                showRatingDoialog(message)
            }
        }
    }

    fun showRatingDoialog(message: String?) {
        val ratingDialog = AlertDialog.Builder(this).create()
        val dialogView = LayoutInflater.from(baseContext).inflate(R.layout.dialog_rating_driver, null, false)
        //        ratingDialog.setContentView(R.layout.dialog_rating_driver);
        val tvDriverName = dialogView.findViewById<TextView>(R.id.tvDriverNameRat)
        val rbDriverRat = dialogView.findViewById<RatingBar>(R.id.rbDriverRat)
        val edtMessageRate = ratingDialog.findViewById<EditText>(R.id.edtMessageRate)
        ratingDialog.setCancelable(false)
        ratingDialog.show()
        ratingDialog.setContentView(dialogView)
        ratingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tvDriverName.text = message
        rbDriverRat.rating = 4f
        val btnRate = dialogView.findViewById<Button>(R.id.btnRateDialog)
        btnRate.setOnClickListener { ratingDialog.dismiss() }
    }

    override fun onBackPressed() {
        if (atMap) {
            super.onBackPressed()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.contentOfHomeActivity, MapFragment(this)).commit()
            atMap = true
            try {
                navigationView!!.checkedItem!!.isChecked = false
            } catch (ex: NullPointerException) {
                ex.printStackTrace()
            }
        }
    }
}