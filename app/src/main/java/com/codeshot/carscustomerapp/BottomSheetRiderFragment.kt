package com.codeshot.carscustomerapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.codeshot.carscustomerapp.Common.Common.getPrice
import com.codeshot.carscustomerapp.Common.Common.iGoogleAPI
import com.codeshot.carscustomerapp.Remote.IGoogleAPI
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BottomSheetRiderFragment : BottomSheetDialogFragment() {
    private var tvLastLocation: TextView? = null
    private var tvDestination: TextView? = null
    private var tvBalance: TextView? = null
    private var mLocation: String? = null
    private var locationLatLng: String? = null
    private var mDestination: String? = null
    private var destinationLatLng: String? = null
    private var pBCalPrice: ProgressBar? = null
    private var isTapMap: Boolean? = null
    private var googleAPI: IGoogleAPI? = null
    public val MDLRoutes=MutableLiveData<JSONArray>()

    fun setRoutes(routes:JSONArray){
        if(routes!=null)
        this.MDLRoutes.value=routes;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        assert(arguments != null)
        mLocation = requireArguments().getString("location")
        locationLatLng = requireArguments().getString("locationLatLng")
        mDestination = requireArguments().getString("destination")
        destinationLatLng = requireArguments().getString("destinationLatLng")
        isTapMap = requireArguments().getBoolean("isTapMap")

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tvLastLocation!!.text = mLocation
        tvDestination!!.text = mDestination
        googleAPI = iGoogleAPI
        pBCalPrice!!.visibility = View.VISIBLE
        getPrice(locationLatLng, destinationLatLng)
    }

    private fun getPrice(locationLatLng: String?, destinationLatLng: String?) {
        MDLRoutes.observe(viewLifecycleOwner, Observer { routess->
            if (routess!=null){
                try {
                    val `object` = routess.getJSONObject(0)
                    val legs = `object`.getJSONArray("legs")
                    val legsObject = legs.getJSONObject(0)
                    //Get distance
                    val distanceObject = legsObject.getJSONObject("distance")
                    val distance = distanceObject.getString("text")
                    //Use regex to extract double from string
                    //this reges will remove all text not is digital
                    val distanceValue = distance.replace("[^0-9\\\\.]".toRegex(), "").toDouble()
                    //Get Time
                    val timeObject = legsObject.getJSONObject("duration")
                    val time = timeObject.getString("text")
                    val timeValue = time.replace("\\D+".toRegex(), "").toInt()
                    val finalCalculet = String.format("%s,%s=$%.2f", distance, time, getPrice(distanceValue, timeValue))
                    pBCalPrice!!.visibility = View.INVISIBLE
                    tvBalance!!.text = finalCalculet
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_rider, container, false)
        tvLastLocation = view.findViewById(R.id.tvLocationSheet)
        tvDestination = view.findViewById(R.id.tvDestinationSheet)
        tvBalance = view.findViewById(R.id.tvBalanceSheet)
        pBCalPrice = view.findViewById(R.id.pBCalPrice)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(mLocation: String?, locationLatLng: String?, mDestination: String?, destinationLatLng: String?, isTapMap: Boolean?,routes:JSONArray?): BottomSheetRiderFragment {
            val bottomSheetDialogFragment = BottomSheetRiderFragment()
            val bundleArgs = Bundle()
            bundleArgs.putString("location", mLocation)
            bundleArgs.putString("destination", mDestination)
            bundleArgs.putString("locationLatLng", locationLatLng)
            bundleArgs.putString("destinationLatLng", destinationLatLng)
            bundleArgs.putBoolean("isTapMap", isTapMap!!)
            bundleArgs.putString("routes",routes.toString())
            bottomSheetDialogFragment.arguments = bundleArgs
            return bottomSheetDialogFragment
        }


    }
}