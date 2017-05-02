package fdi.ucm.carfinder.connection;

import org.json.JSONObject;

/**
 * Created by Mauri on 2/5/17.
 */

public class Mapa extends Conexion {

    private final String mapa = "Mapa.php";

    public JSONObject insertarPosicion(String matricula, String latitud, String longitud) {
        final String datos = "action=insertar&matricula="+matricula+"&longitud="+longitud+"&latitud="+latitud;

        return this.ejecutar(datos, this.mapa);
    }

    public JSONObject cargarPosiciones(String usuario) {
        final String datos = "action=leerPorUsuario&email="+usuario;

        return this.ejecutar(datos, this.mapa);
    }

    public JSONObject eliminarPosicion(String matricula) {
        final String datos = "action=borrar&matricula="+matricula;

        return this.ejecutar(datos, this.mapa);
    }
}
