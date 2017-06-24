package fdi.ucm.carfinder.connection;

import org.json.JSONObject;


public class Coches extends Conexion {

    private final String coches = "Coches.php";

    public JSONObject insertarCoche(String matricula, String marca, String modelo, String email) {
        final String datos = "action=insertar&matricula="+matricula+"&marca="+marca+"&modelo="+modelo+"&email="+email;

        return this.ejecutar(datos, this.coches);
    }

    public JSONObject cargarCoches(String email, String matr) {
        final String datos = "action=leer&email="+email+"&matricula="+matr;

        return this.ejecutar(datos, this.coches);
    }

    public JSONObject eliminarCoche(String matricula, String email) {
        final String datos = "action=borrar&matricula="+matricula+"&email="+email;

        return this.ejecutar(datos, this.coches);
    }
}
