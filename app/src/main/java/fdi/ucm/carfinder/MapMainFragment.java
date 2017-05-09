package fdi.ucm.carfinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fdi.ucm.carfinder.connection.Mapa;
import fdi.ucm.carfinder.connection.Usuarios;
import fdi.ucm.carfinder.modelo.Coche;
import fdi.ucm.carfinder.modelo.Posiciones;


public class MapMainFragment extends Fragment implements LocationListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private WebView webView;

    protected String email;
    protected double latitude; // latitude
    protected double longitude; // longitude
    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    private OnFragmentInteractionListener mListener;
    private LocationManager mLocationManager;
    protected MapLocationTask mPositionTask;

    protected ArrayList<Posiciones> posicionesCoches;

    public MapMainFragment() {
        posicionesCoches = new ArrayList<>();
    }

    public static MapMainFragment newInstance(String param1, String param2) {
        MapMainFragment fragment = new MapMainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location net_loc = null, gps_loc = null, finalLoc = null;

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (gps_enabled)
            gps_loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (network_enabled)
            net_loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gps_loc != null && net_loc != null) {

            //smaller the number more accurate result will
            if (gps_loc.getAccuracy() > net_loc.getAccuracy())
                finalLoc = net_loc;
            else
                finalLoc = gps_loc;

            // I used this just to get an idea (if both avail, its upto you which you want to take as I've taken location with more accuracy)

        } else {

            if (gps_loc != null) {
                finalLoc = gps_loc;
            } else if (net_loc != null) {
                finalLoc = net_loc;
            }
        }
        if (finalLoc != null) {
            latitude = finalLoc.getLatitude();
            longitude = finalLoc.getLongitude();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        SharedPreferences sp = getActivity().getSharedPreferences("Login",0);
        email = sp.getString("User", null);

        this.cargarWeb(view, this.latitude, this.longitude, null);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getLatitude() > latitude + 0.0005 || location.getLatitude() < latitude - 0.0005 ||
                location.getLongitude() > longitude + 0.0005 || location.getLongitude() < longitude - 0.0005) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            cargarWeb(null, this.latitude, this.longitude, null);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    protected void cargarWeb(View view, Double latitud, Double longitude, String matr) {
        String descripcion;
        if (matr == null)
            descripcion = "&description=actual";
        else
            descripcion = "&description="+matr;
        String url = "file:///android_asset/mapa.html" + "?lat="
                + new Double(latitud).toString()+"&lng="+new Double(longitude).toString()+
                descripcion;
        if (this.webView == null && view != null) {
            webView = (WebView) view.findViewById(R.id.web_view_map);
            webView.getSettings().setJavaScriptEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            } //Para depuración
        }

        webView.loadUrl(url);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    protected class MapLocationTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mMatricula;
        private final String mLatitud;
        private final String mLongitud;
        private final Context contexto;
        private JSONObject datos;
        private final int opcion;

        private String msgError;

        MapLocationTask(String email, Context cont) {
            mEmail = email;
            mMatricula = null;
            mLatitud = null;
            mLongitud = null;
            contexto = cont;
            msgError = "";
            opcion = 0;
        }

        MapLocationTask(String matricula, String latitud, String longitud, Context cont) {
            mEmail = null;
            mMatricula = matricula;
            mLatitud = latitud;
            mLongitud = longitud;
            contexto = cont;
            msgError = "";
            opcion = 1;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            if (opcion == 0) {
                Mapa conexion = new Mapa();
                JSONObject resultado = conexion.cargarPosiciones(mEmail);
                try {
                    if (Integer.parseInt(resultado.get("errorno").toString()) != 0) {
                        msgError = resultado.get("errorMessage").toString();
                        return false;
                    } else {
                        datos = resultado;
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mPositionTask = null;

            if (success) {
                if (opcion == 0) {
                    try {
                        final JSONArray posicionesServidor = datos.getJSONArray("coches");

                        for (int i = 0; i < posicionesServidor.length(); i++) {
                            JSONObject posicion = posicionesServidor.getJSONObject(i);
                            Posiciones temp = new Posiciones();
                            temp.setMatricula(posicion.getString("matricula"));
                            JSONArray coordenadas = (JSONArray) posicion.get("coordenadas");
                            temp.setLatitud(coordenadas.getJSONObject(0).getString("latitud"));
                            temp.setLongitud(coordenadas.getJSONObject(0).getString("longitud"));
                            posicionesCoches.add(temp);
                        }

                    } catch (JSONException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
                        builder.setMessage(e.getMessage()).setTitle("Error");
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
                    builder.setMessage(msgError).setTitle("Error");
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mPositionTask = null;
        }
    }
}
