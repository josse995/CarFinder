package fdi.ucm.carfinder.connection;

import org.json.JSONObject;

/**
 * Created by Mauri on 24/04/2017.
 */

public class Usuarios extends Conexion {

    private final String usuarios = "Usuarios.php";

    public JSONObject iniciarSesion(String usuario, String pass) {
        final String datos = "action=leer&email="+usuario+"&password="+pass;

        return this.ejecutar(datos, this.usuarios);
    }

    public JSONObject registrarUsuario(String email, String pass, String name, String last, String date) {
        final String datos = "action=insertar&email="+email+"&password="+pass+"&nombre="+name+
                "&apellidos="+last+"&fecha_nac="+date;

        return this.ejecutar(datos, this.usuarios);
    }

}
