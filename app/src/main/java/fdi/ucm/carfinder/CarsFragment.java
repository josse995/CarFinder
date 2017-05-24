package fdi.ucm.carfinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fdi.ucm.carfinder.connection.Coches;
import fdi.ucm.carfinder.modelo.Coche;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CarsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CarsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CarsFragment extends Fragment {
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";
    private CarsTask mAuthTask = null;

    //private String mParam1;
    //private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ArrayList<Coche> coches;
    private CustomListAdapter adapter;
    private int lastSelected;

    public CarsFragment() {
        // Required empty public constructor
        lastSelected = -1;
    }

    public static CarsFragment newInstance(String param1, String param2) {
        CarsFragment fragment = new CarsFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cars, container, false);

        SharedPreferences sp = getActivity().getSharedPreferences("Login",0);
        final String user = sp.getString("User", null);

        //Esto te abre el popup_cars para añadir un coche
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_addCar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addListener();
            }
        });

        mAuthTask = new CarsTask(user, getContext(), 0, null, null, null, null);
        mAuthTask.execute((Void) null);

        return view;
    }

    private void addListener() {
        SharedPreferences sp = getActivity().getSharedPreferences("Login",0);
        final String user = sp.getString("User", null);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_cars,null);
        final EditText mBrand = (EditText)view.findViewById(R.id.new_car_brand);
        final EditText mModel = (EditText)view.findViewById(R.id.new_car_model);
        final EditText mMatr = (EditText)view.findViewById(R.id.new_car_matr);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        Button button = (Button)view.findViewById(R.id.button_new_car);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String brand = mBrand.getText().toString();
                String model = mModel.getText().toString();
                String matr = mMatr.getText().toString();

                mAuthTask = new CarsTask(user, getContext(), 1, brand, model, matr, dialog);
                mAuthTask.execute((Void) null);
            }
        });
    }

    private void init(JSONObject datos) throws JSONException{
        if (getView() != null) {
            ListView ll = (ListView) getView().findViewById(R.id.table_cars);
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

            adapter = new CustomListAdapter(getContext(), this.coches);
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

    private void agregarCocheTabla(String matricula, String marca, String modelo) {
        ListView ll = (ListView) getView().findViewById(R.id.table_cars);
        Coche aux = new Coche(matricula, marca, modelo);
        this.coches.add(aux);

        adapter.notifyDataSetChanged();

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

    private void eliminarCocheTabla() {
        ListView ll = (ListView) getView().findViewById(R.id.table_cars);
        this.coches.remove(lastSelected);
        lastSelected = -1;

        adapter.notifyDataSetChanged();

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
    }

    private void rowListener(int position, View view) {
        for(int i = 0; i < coches.size(); i++) {
            coches.get(i).setSelected(false);
        }

        if (lastSelected != position) {
            lastSelected = position;
            coches.get(position).setSelected(true);
            adapter.notifyDataSetChanged();
            view.setSelected(true);
            final FloatingActionButton fb = (FloatingActionButton) getView().findViewById(R.id.fab_addCar);
            fb.setImageResource(R.drawable.ic_delete);
            fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.Delete_car_message).setTitle("Aviso");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Coche temp = coches.get(lastSelected);

                            SharedPreferences sp = getActivity().getSharedPreferences("Login",0);
                            final String user = sp.getString("User", null);

                            mAuthTask = new CarsTask(user, getContext(), 2, null, null, temp.getMatricula(), null);
                            mAuthTask.execute((Void) null);
                            fb.setImageResource(R.drawable.ic_add_black_24dp);
                            fb.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    addListener();
                                }
                            });
                            dialog.dismiss();
                        } });

                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        } });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
        else {
            lastSelected = -1;
            adapter.notifyDataSetChanged();
            FloatingActionButton fb = (FloatingActionButton) getView().findViewById(R.id.fab_addCar);
            fb.setImageResource(R.drawable.ic_add_black_24dp);
            fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addListener();
                }
            });
        }
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
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

            Coches conexion = new Coches();
            if(opcion == 0) {
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
                    agregarCocheTabla(matr, brand, model);
                } else if (opcion == 2){
                    eliminarCocheTabla();
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

    public static class CustomListAdapter extends BaseAdapter {
        private Context context; //context
        private static ArrayList<Coche> items; //data source of the list adapter
        private LayoutInflater mInflater;


        //public constructor
        public CustomListAdapter(Context context, ArrayList<Coche> items) {
            this.items = items;
            this.mInflater = LayoutInflater.from(context);
            this.context = context;
        }

        @Override
        public int getCount() {
            return items.size(); //returns total of items in the list
        }

        @Override
        public Object getItem(int position) {
            return items.get(position); //returns list item at the specified position
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.car_rows, parent, false);
            }

            Coche currentItem = (Coche) getItem(position);
            TextView brand = (TextView) v.findViewById(R.id.textView_brand);
            TextView model = (TextView) v.findViewById(R.id.textView_model);
            TextView matr = (TextView) v.findViewById(R.id.textView_matr);

            brand.setText(currentItem.getMarca()+"\t");
            model.setText(currentItem.getModelo()+"\t");
            matr.setText(currentItem.getMatricula());


            if (items.get(position).getSelected()) {
                ImageView image = (ImageView) v.findViewById(R.id.check);
                image.setVisibility(View.VISIBLE);
                //v.setBackgroundColor(Color.parseColor("#36cf48"));
            }else{
                ImageView image = (ImageView) v.findViewById(R.id.check);
                image.setVisibility(View.GONE);
                //v.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            return v;
        }

        static class ViewHolder{
            TextView brand, model, matr;
        }
    }
}
