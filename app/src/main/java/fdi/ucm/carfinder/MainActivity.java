package fdi.ucm.carfinder;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SettingsFragment.OnFragmentInteractionListener,
        CarsFragment.OnFragmentInteractionListener, MainFragment.OnFragmentInteractionListener{

    private static final int REQUEST_GPS = 2;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        this.setTitle("Inicio");

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_GPS);

            }
            else {
                Fragment fragment;
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.contenedor);
                if(currentFragment == null) {
                    fragment = new MainFragment();
                } else if (currentFragment instanceof CarsFragment) {
                    fragment = new CarsFragment();
                } else if (currentFragment instanceof SettingsFragment) {
                    fragment = new SettingsFragment();
                } else {
                    fragment = new MainFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, fragment).commit();
            }
        }
        else {
            Fragment fragment;
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.contenedor);
            if(currentFragment == null) {
                fragment = new MainFragment();
            } else if (currentFragment instanceof CarsFragment) {
                fragment = new CarsFragment();
            } else if (currentFragment instanceof SettingsFragment) {
                fragment = new SettingsFragment();
            } else {
                fragment = new MainFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, fragment).commit();
        }

    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contenedor);

        if (fragment instanceof MainFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.back_exit, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        } else {
            fragment = new MainFragment();
            this.setTitle("Inicio");
            getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, fragment).commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_GPS) {
            Fragment fragment = new MainFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, fragment).commitAllowingStateLoss();
        }
    }

    /**
     * Elimina los datos almacenados en el dispositivo y procede a mostrar el inicio de sesión.
     */
    private void cerrarSesion() {
        SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor Ed=sp.edit();
        Ed.remove("User");
        Ed.remove("Pass");
        Ed.remove("name");
        Ed.remove("lastName");
        Ed.remove("date");
        Ed.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        this.finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        Boolean select = false;
        if (id == R.id.inicio) {
            select = true;
            fragment = new MainFragment();
            this.setTitle("Inicio");
        } else if (id == R.id.coches) {
            select = true;
            fragment = new CarsFragment();
            this.setTitle(getString(R.string.title_cars));
        } else if (id == R.id.ajustes) {
            select = true;
            fragment = new SettingsFragment();
            this.setTitle(getString(R.string.title_settings));
        } else if (id == R.id.cerrarSesion) {
            cerrarSesion();
        }

        if (select) {
            getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
