package fdi.ucm.carfinder;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fdi.ucm.carfinder.connection.Coches;
import fdi.ucm.carfinder.modelo.Coche;
import fdi.ucm.carfinder.modelo.Posiciones;

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

        mPositionTask = new MapLocationTask(super.email, null, null, null, getContext(), -1, 0);
        mPositionTask.execute((Void) null);

        mAuthTask = new CarsTask(super.email, getContext());
        mAuthTask.execute((Void) null);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.borrarposicion) {
            if (lastSelected != -1) {
                int i = 0;
                Boolean encontrado = false;
                while (i < posicionesCoches.size() && !encontrado) {
                    if (coches.get(lastSelected).getMatricula().equals(posicionesCoches.get(i).getMatricula()))
                        encontrado = true;
                    else
                        ++i;
                }
                if (encontrado) {
                    mPositionTask = new MapLocationTask(null, coches.get(lastSelected).getMatricula(),
                            null, null, getContext(), lastSelected, 2);
                    mPositionTask.execute((Void) null);
                    rowListener(lastSelected, getView());
                    return true;
                } else {
                    Toast.makeText(
                            getContext(),
                            R.string.deleted_position_error, Toast.LENGTH_SHORT
                    ).show();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.error_not_car_select).setTitle("Error");
                AlertDialog alert = builder.create();
                alert.show();
            }
            return false;
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
        for(int i = 0; i < coches.size(); ++i) {
            coches.get(i).setSelected(false);
        }

        if (lastSelected != position) {
            lastSelected = position;
            final Coche temp = coches.get(position);
            temp.setSelected(true);
            adapter.notifyDataSetChanged();
            view.setSelected(true);

            if(!posicionesCoches.isEmpty()) {
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
                        cargarWeb(view);
               // }
            }
            final FloatingActionButton fb = (FloatingActionButton) getView().findViewById(R.id.fb_addLocation);
            fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (gps_enabled || network_enabled) {
                        String matr = temp.getMatricula();
                        String lat = Double.valueOf(latitude).toString();
                        String lon = Double.valueOf(longitude).toString();

                        Posiciones posicion = new Posiciones();
                        posicion.setMatricula(matr);
                        posicion.setLatitud(lat);
                        posicion.setLongitud(lon);

                        posicionesCoches.add(posicion);

                        mPositionTask = new MapLocationTask(null, matr, lat, lon,
                                getContext(), posicionesCoches.size() - 1, 1);
                        mPositionTask.execute((Void) null);
                    }
                }
            });
        }
        else {
            lastSelected = -1;
            adapter.notifyDataSetChanged();
            cargarWeb(view);
            FloatingActionButton fb = (FloatingActionButton) getView().findViewById(R.id.fb_addLocation);
            fb.setOnClickListener(null);
        }
    }

    private class CarsTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final Context contexto;
        private String msgError;
        private JSONObject datos;

        CarsTask(String email, Context cont) {
            mEmail = email;
            contexto = cont;
            msgError = "";
        }

        @Override
        protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
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
    }

        @Override
        protected void onPostExecute(final Boolean success) {
        mAuthTask = null;
        if (success) {
            try {
                init(datos);
            } catch (JSONException e) {
                e.printStackTrace();
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
