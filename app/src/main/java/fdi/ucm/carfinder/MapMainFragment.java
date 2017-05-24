package fdi.ucm.carfinder;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fdi.ucm.carfinder.connection.Mapa;
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
    protected boolean gps_enabled = false;
    protected boolean network_enabled = false;

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

        Location net_loc = null, gps_loc = null, finalLoc = null;

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            gps_enabled = false;
            network_enabled = false;
        }

        else {
            gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        if (gps_enabled)
            gps_loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (network_enabled)
            net_loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gps_loc != null && net_loc != null) {
            if (gps_loc.getAccuracy() > net_loc.getAccuracy())
                finalLoc = net_loc;
            else
                finalLoc = gps_loc;
        } else {
            if (gps_loc != null)
                finalLoc = gps_loc;
            else if (net_loc != null)
                finalLoc = net_loc;
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

        this.cargarWeb(view);
        return view;
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
            cargarWeb(null);
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

    protected void cargarWeb(View view) {
        String url;
        if (!gps_enabled && !network_enabled) {
            url = "file:///android_asset/error.html";
        } else {
            url = "file:///android_asset/mapa.html" + "?lat="
                    + latitude + "&lng="+ longitude + "&description=actual";
        }

        if (this.webView == null && view != null) {
            webView = (WebView) view.findViewById(R.id.web_view_map);
            webView.getSettings().setJavaScriptEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            } //Para depuración
        }

        webView.loadUrl(url);
    }

    protected void cargarWeb(View view, final Double latitud, final Double longitude, String matr) {
        String descripcion;
        if (matr == null)
            descripcion = "&description=actual";
        else
            descripcion = "&description="+matr;
        String url = "file:///android_asset/mapa.html" + "?lat="
                + Double.valueOf(latitud).toString()+"&lng="+ Double.valueOf(longitude).toString() +
                descripcion;
        if (this.webView == null && view != null) {
            webView = (WebView) view.findViewById(R.id.web_view_map);
            webView.getSettings().setJavaScriptEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            } //Para depuración
        }

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading (WebView view, WebResourceRequest request) {
                if (request.getUrl().getScheme().equals("carfinder")) {

                    String lat = request.getUrl().getQueryParameter("lat");
                    String lon = request.getUrl().getQueryParameter("lon");
                    String matr = request.getUrl().getQueryParameter("description");

                    String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lon + " (" + matr + ")";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);

                    return true;
                }
                return false;
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("carfinder://")) {
                    String[] datos = url.split("=");
                    String lat = datos[1].split("&")[0];
                    String lon = datos[2].split("&")[0];
                    String matr = datos[3];

                    String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lon + " (" + matr + ")";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);

                    return true;
                }
                return false;
            }

        });
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
        private final int index;

        private String msgError;

        /*MapLocationTask(String email, Context cont) {
            mEmail = email;
            mMatricula = null;
            mLatitud = null;
            mLongitud = null;
            contexto = cont;
            msgError = "";
            opcion = 0;
            index = -1;
        }

        MapLocationTask(String matricula, Context cont, int ind) {
            mEmail = null;
            mMatricula = matricula;
            mLatitud = null;
            mLongitud = null;
            contexto = cont;
            msgError = "";
            opcion = 2;
            index = ind;
        }*/

        MapLocationTask(String email, String matricula, String latitud, String longitud,
                        Context cont, int ind, int opt) {
            mEmail = email;
            mMatricula = matricula;
            mLatitud = latitud;
            mLongitud = longitud;
            contexto = cont;
            msgError = "";
            opcion = opt;
            index = ind;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Mapa conexion = new Mapa();
            if (opcion == 0) {
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
            } else if (opcion == 1) {
                JSONObject resultado = conexion.insertarPosicion(mMatricula, mLatitud, mLongitud);
                try {
                    if (Integer.parseInt(resultado.get("errorno").toString()) != 0) {
                        msgError = resultado.get("errorMessage").toString();
                        posicionesCoches.remove(index);
                        return false;
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            } else if (opcion == 2) {
                JSONObject resultado = conexion.eliminarPosicion(mMatricula);
                try {
                    if (Integer.parseInt(resultado.get("errorno").toString()) != 0) {
                        msgError = resultado.get("errorMessage").toString();
                        return false;
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
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
                            temp.setLatitud(coordenadas.getJSONObject(i).getString("latitud"));
                            temp.setLongitud(coordenadas.getJSONObject(i).getString("longitud"));
                            posicionesCoches.add(temp);
                        }

                    } catch (JSONException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
                        builder.setMessage(e.getMessage()).setTitle("Error");
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } else if(opcion == 1) {
                    Toast.makeText(
                            getContext(),
                            R.string.saved_position, Toast.LENGTH_SHORT
                    ).show();
                    cargarWeb(getView(), Double.parseDouble(mLatitud), Double.parseDouble(mLongitud),
                            mMatricula);
                }
                else if (opcion == 2) {
                    int i = 0;
                    Boolean encontrado = false;
                    while (i < posicionesCoches.size() && !encontrado) {
                        if (posicionesCoches.get(i).getMatricula().equals(mMatricula)) {
                            posicionesCoches.remove(i);
                            encontrado = true;
                        } else {
                            i++;
                        }
                    }
                    Toast.makeText(
                            getContext(),
                            R.string.deleted_position, Toast.LENGTH_SHORT
                    ).show();
                    cargarWeb(getView());
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
