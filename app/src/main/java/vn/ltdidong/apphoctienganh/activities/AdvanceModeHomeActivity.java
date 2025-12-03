package vn.ltdidong.apphoctienganh.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.ltdidong.apphoctienganh.R;

public class AdvanceModeHomeActivity extends AppCompatActivity implements OnMapReadyCallback {
    private BottomNavigationView bottomNav;
    private GoogleMap ggMap;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // lấy dữ liệu đối tượng gg map trả về
        ggMap = googleMap;
        ggMap.getUiSettings().setZoomControlsEnabled(true);

        // gọi lấy vị trí người dùng
        requestLocationPermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance_mode_page);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ánh xạ các thành phần view
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_advance_mode);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(AdvanceModeHomeActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_skills) {
                Intent intent = new Intent(AdvanceModeHomeActivity.this, SkillHomeActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_advance_mode) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(AdvanceModeHomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    // xin quyền truy cập vị trí
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            enableUserLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocation() {
        ggMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                ggMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 18f));
            } else {
                // Nếu không lấy được vị trí, đặt mặc định ở TP.HCM, Việt Nam
                Log.e(">>> GG Map", "Không lấy được vị trí hiện tại, đang thay thế bằng vị trí cố định");
                LatLng defaultLocation = new LatLng(10.762622, 106.660172);
                ggMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 18f));
            }
        });
    }

}
