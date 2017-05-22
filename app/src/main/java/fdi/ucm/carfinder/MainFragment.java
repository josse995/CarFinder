package fdi.ucm.carfinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fdi.ucm.carfinder.connection.Coches;
import fdi.ucm.carfinder.modelo.Coche;
import fdi.ucm.carfinder.modelo.Posiciones;

import static android.R.attr.id;

/**
 * Created by Mauri on 09/05/2017.
 */

public class MainFragment extends MapMainFragment {

    private ArrayList<Coche> coches;
    private CarsFragment.CustomListAdapter adapter;
    private int lastSelected;
    private CarsTask mAuthTask = null;


    public MainFragment() {
        super();
        lastSelected = -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_activity, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mPositionTask = new MapLocationTask(super.email, getContext());
        mPositionTask.execute((Void) null);

        mAuthTask = new CarsTask(super.email, getContext(), 0, null, null, null, null);
        mAuthTask.execute((Void) null);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.borrarposicion) {
            mPositionTask = new MapLocationTask(posicionesCoches.get(lastSelected).getMatricula(), getContext(), lastSelected);
            mPositionTask.execute((Void) null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init(JSONObject datos) throws JSONException {
        if(getView() != null) {
            ListView ll = (ListView) getView().findViewById(R.id.table_cars_main);
            this.coches = new ArrayList<>();

            final JSONArray cochesServidor;
            if (datos != null) {
                cochesServidor = datos.getJSONArray("coches");
            } else {
                cochesServidor = new JSONArray();
            }

            for (int i = 0; i < cochesServidor.length(); i++) {

                JSONObject coche = cochesServidor.getJSONObject(i);

                //COGEMOS UN COCHE
                Coche aux = new Coche(coche.getString("matricula"), coche.getString("marca"),
                        coche.getString("modelo"));
                this.coches.add(aux);

            }

            adapter = new CarsFragment.CustomListAdapter(getContext(), this.coches);
            ll.setAdapter(adapter);

            int totalHeight = 0;

            for (int i = 0; i < adapter.getCount(); i++) {
                View listItem = adapter.getView(i, null, ll);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams par = ll.getLayoutParams();
            par.height = totalHeight + (ll.getDividerHeight() * (adapter.getCount() - 1));
            ll.setLayoutParams(par);
            ll.requestLayout();

            ll.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            ll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                    rowListener(position, view);
                }
            });
        }
    }

    private void rowListener(int position, View view) {
        for(int i = 0; i < coches.size(); i++) {
            coches.get(i).setSelected(false);
        }

        if (lastSelected != position) {
            lastSelected = position;
            Coche temp = coches.get(position);
            temp.setSelected(true);
            adapter.notifyDataSetChanged();
            view.setSelected(true);

            if(!posicionesCoches.isEmpty()) {
                if (position < posicionesCoches.size()) {
                    if (coches.get(position).getMatricula().equals(posicionesCoches.get(position).getMatricula())) {
                        Double lat = Double.parseDouble(posicionesCoches.get(position).getLatitud());
                        Double lon = Double.parseDouble(posicionesCoches.get(position).getLongitud());
                        cargarWeb(view, lat, lon, temp.getMatricula());
                    }
                } else {
                    int i = 0;
                    Boolean encontrado = false;
                    while (i < posicionesCoches.size() && !encontrado) {
                        if (coches.get(position).getMatricula().equals(posicionesCoches.get(i).getMatricula()))
                            encontrado = true;
                        else
                            ++i;
                    }
                    if (encontrado) {
                        Double lat = Double.parseDouble(posicionesCoches.get(i).getLatitud());
                        Double lon = Double.parseDouble(posicionesCoches.get(i).getLongitud());
                        cargarWeb(view, lat, lon, temp.getMatricula());
                    } else
                        cargarWeb(view, super.latitude, super.longitude, null);
                }
            }
            final FloatingActionButton fb = (FloatingActionButton) getView().findViewById(R.id.fb_addLocation);
            fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Coche temp = coches.get(lastSelected);
                    String matr = temp.getMatricula();
                    String lat = new Double(latitude).toString();
                    String lon = new Double(longitude).toString();

                    Posiciones posicion = new Posiciones();
                    posicion.setMatricula(matr);
                    posicion.setLatitud(lat);
                    posicion.setLongitud(lon);

                    posicionesCoches.add(posicion);

                    mPositionTask = new MapLocationTask(matr, lat, lon, getContext(), posicionesCoches.size() - 1);
                    mPositionTask.execute((Void) null);
                }
            });
        }
        else {
            lastSelected = -1;
            adapter.notifyDataSetChanged();
            cargarWeb(view, super.latitude, super.longitude, null);
            FloatingActionButton fb = (FloatingActionButton) getView().findViewById(R.id.fb_addLocation);
            fb.setOnClickListener(null);
        }
    }

    private class CarsTask extends AsyncTask<Void, Void, Boolean> {

        private int opcion;
        private final String mEmail;
        private final Context contexto;
        private String msgError;
        private JSONObject datos;
        private String brand;
        private String model;
        private String matr;
        private AlertDialog alertAbierto;

        CarsTask(String email, Context cont, int opcion, String brand, String model, String matr, AlertDialog alerta) {
        mEmail = email;
        contexto = cont;
        msgError = "";
        this.opcion = opcion;
        this.brand = brand;
        this.model = model;
        this.matr = matr;
        this.alertAbierto = alerta;
    }

        @Override
        protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        if(opcion == 0) {
            Coches conexion = new Coches();
            JSONObject resultado = conexion.cargarCoches(mEmail, "0");
            try {
                if (Integer.parseInt(resultado.get("errorno").toString()) == 0) {
                    datos = resultado;
                    return true;
                } else if (Integer.parseInt(resultado.get("errorno").toString()) != 2){
                    msgError = resultado.get("errorMessage").toString();
                    return false;
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }else if (opcion == 1){
            Coches conexion = new Coches();
            JSONObject resultado = conexion.insertarCoche(matr, brand, model, mEmail);
            try {
                if (Integer.parseInt(resultado.get("errorno").toString()) != 0) {
                    msgError = resultado.get("errorMessage").toString();
                    return false;
                } else {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }else if (opcion == 2){
            Coches conexion = new Coches();
            JSONObject resultado = conexion.eliminarCoche(matr, mEmail);
            try {
                if (Integer.parseInt(resultado.get("errorno").toString()) != 0) {
                    msgError = resultado.get("errorMessage").toString();
                    return false;
                } else {
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
        mAuthTask = null;
        if (success) {
            if (opcion == 0) {
                try {
                    init(datos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (opcion == 1){
                alertAbierto.dismiss();
                //agregarCocheTabla(matr, brand, model);
            } else if (opcion == 2){
                //eliminarCocheTabla();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
            builder.setMessage(msgError).setTitle("Error");
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

        @Override
        protected void onCancelled() {
        mAuthTask = null;
    }


    }
}
