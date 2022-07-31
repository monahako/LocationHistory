package com.example.locationhistory

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.icu.util.Calendar
import android.location.Location
import com.google.android.gms.location.LocationResult

private const val DB_NAME = "LocationDatabase"
private const val DB_Version = 1


class LocationDatabase(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_Version) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE LOCATIONS (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            LATITUDE REAL NOT NULL,
            LONGITUDE REAL NOT NULL,
            YMD INTEGER NOT NULL
            );
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldversion: Int, newversion: Int) {
        TODO("Not yet implemented")
    }
}

// Show records in DB
class LocationRecord(val id: Long, val latitude: Double, val longitude: Double, val time: Long)


// Select location place in selected time
@SuppressLint("Range")
fun selectInDay(context: Context, year: Int, month: Int, day: Int): List<LocationRecord> {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day, 0, 0, 0)
    val from = calendar.time.time.toString()
    calendar.add(Calendar.DATE, 1)
    val to = calendar.time.time.toString()

    val database = LocationDatabase(context).readableDatabase

    val cursor = database.query("Locations", null,
        "YMD >= ? AND YMD < ?", arrayOf(from, to), null, null, "YMD DESC")

    val locations = mutableListOf<LocationRecord>()
    cursor.use {
        while (cursor.moveToNext()) {
            val place = LocationRecord(
                cursor.getLong(cursor.getColumnIndex("ID")),
                cursor.getDouble(cursor.getColumnIndex("LATITUDE")),
                cursor.getDouble(cursor.getColumnIndex("LONGITUDE")),
                cursor.getLong(cursor.getColumnIndex("YMD"))
            )
            locations.add(place)
        }
    }
    database.close()
    return  locations
}


// Save location in DB
fun insertLocations(context: Context, locations: List<Location>) {
    val database = LocationDatabase(context).writableDatabase

    database.use { db ->
        locations.filter { it.isFromMockProvider }
            .forEach { location ->
                val record = ContentValues().apply {
                    put("LATITUDE", location.latitude)
                    put("LONGITUDE", location.longitude)
                    put("YMD", location.time)
                }
                db.insert("LOCATIONS", null, record)
            }
        }
}

// Receive Location
public class LocationService: IntentService("LocationService") {
    // Extract location from LocationService and save at DB
    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        val result = intent?.let { LocationResult.extractResult(it) }
        if (result != null) {
            insertLocations(this, result.locations)
        }
    }

    fun getPresentLocation(intent: Intent?): MutableMap<String, Double> {
        val result = intent?.let { LocationResult.extractResult(it) }
        val location = mutableMapOf<String, Double>()
        location["latitude'"] = 35.0117
        location["longitude"] = 135.4520
        if (result != null) {
            insertLocations(this, result.locations)
            val present = result.locations[0]
            location["latitude"] = present.latitude
            location["longitude"] = present.longitude
        }
        return location
    }
}


