package fdi.ucm.carfinder.connection;

import org.json.JSONObject;

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

    public JSONObject cambiarEmail(String email, String new_email) {
        final String datos = "action=modificarEmail&email="+email+"&new_email="+new_email;

        return this.ejecutar(datos, this.usuarios);
    }

    public JSONObject cambiarPasswpord(String email, String password, String new_pass) {
        final String datos = "action=modificarPassword&email="+email+"&password="+password+
                "&new_password="+new_pass;

        return this.ejecutar(datos, this.usuarios);
    }

}
