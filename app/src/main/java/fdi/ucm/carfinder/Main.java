package fdi.ucm.carfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Main extends AppCompatActivity {

    private String user = null;
    private String password = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cargarPreferencias();

        if (user != null && password != null) {
            //Iniciar actividad del menú principal
        }

        else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void cargarPreferencias() {
        SharedPreferences sp = this.getSharedPreferences("Login",0);
        user = sp.getString("User", null);
        password = sp.getString("Pass", null);
    }
}
