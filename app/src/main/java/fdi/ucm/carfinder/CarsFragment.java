package fdi.ucm.carfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView.ViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import fdi.ucm.carfinder.connection.Coches;
import fdi.ucm.carfinder.connection.Conexion;
import fdi.ucm.carfinder.connection.Usuarios;
import fdi.ucm.carfinder.modelo.Coche;

import static fdi.ucm.carfinder.R.id.email;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CarsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CarsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CarsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private CarsTask mAuthTask = null;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ArrayList<Coche> coches;

    public CarsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CarsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CarsFragment newInstance(String param1, String param2) {
        CarsFragment fragment = new CarsFragment();
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
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cars, container, false);

        SharedPreferences sp = getActivity().getSharedPreferences("Login",0);
        final String user = sp.getString("User", null);




        //Esto te abre el popup para añadir un coche
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_addCar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View mView = inflater.inflate(R.layout.popup_coches, container, false);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());

                Button button = (Button)mView.findViewById(R.id.button_new_car);
                button.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        EditText mBrand = (EditText)mView.findViewById(R.id.new_car_brand);
                        EditText mModel = (EditText)mView.findViewById(R.id.new_car_model);
                        EditText mMatr = (EditText)mView.findViewById(R.id.new_car_matr);
                        String brand = mBrand.getText().toString();
                        String model = mModel.getText().toString();
                        String matr = mMatr.getText().toString();

                        mAuthTask = new CarsTask(user, getContext(), 1, brand, model, matr);
                        mAuthTask.execute((Void) null);
                    }
                });

                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });

        mAuthTask = new CarsTask(user, getContext(), 0, null, null, null);
        mAuthTask.execute((Void) null);

        return view;
    }

    public void init(JSONObject datos) throws JSONException{
        ListView ll = (ListView) getView().findViewById(R.id.table_cars);
        this.coches = new ArrayList<>();

        JSONArray coches = datos.getJSONArray("coches");



        for (int i = 0; i < coches.length(); i++) {

            JSONObject coche = coches.getJSONObject(i);

            //COGEMOS UN COCHE
            Coche aux = new Coche(coche.getString("matricula"), coche.getString("marca"),
                    coche.getString("modelo"));
            this.coches.add(aux);

            // instantiate the custom list adapter


        }
        CustomListAdapter adapter = new CustomListAdapter(getContext(), this.coches);
        ll.setAdapter(adapter);
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

        CarsTask(String email, Context cont, int opcion, String brand, String model, String matr) {
            mEmail = email;
            contexto = cont;
            msgError = "";
            this.opcion = opcion;
            this.brand = brand;
            this.model = model;
            this.matr = matr;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            if(opcion == 0) {
                Coches conexion = new Coches();
                JSONObject resultado = conexion.cargarCoches(mEmail, "0");
                try {
                    if (Integer.parseInt(resultado.get("errorno").toString()) != 0) {
                        msgError = resultado.get("errorMessage").toString();
                        return false;
                    } else {
                        datos = resultado;
                        //Crear actividad del menú principal

                        return true;
                    }
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
                //TODO
                return false;
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
            // Prueba1
            // inflate the layout for each list row


            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.prueba, parent, false);
            }


            /*Prueba 2
            ViewHolder holder;
            if(v == null){
                v = mInflater.inflate(R.layout.fragment_cars, null);
                holder = new ViewHolder();
                holder.brand = (TextView)convertView.findViewById(R.id.textView_brand);
                holder.model = (TextView)convertView.findViewById(R.id.textView_model);
                holder.matr = (TextView)convertView.findViewById(R.id.textView_matr);

                v.setTag(holder);
            }else
                holder = (ViewHolder) v.getTag();
            */
            // get current item to be displayed
            Coche currentItem = (Coche) getItem(position);

            //Prueba1
            // get the TextView for item name and item description
            TextView brand = (TextView) v.findViewById(R.id.textView_brand);
            TextView model = (TextView) v.findViewById(R.id.textView_model);
            TextView matr = (TextView) v.findViewById(R.id.textView_matr);

            brand.setText(currentItem.getMarca()+"\t");
            model.setText(currentItem.getModelo()+"\t");
            matr.setText(currentItem.getMatricula());


            /*prueba2
            holder.brand.setText(currentItem.getMarca());
            holder.model.setText(currentItem.getModelo());
            holder.matr.setText(currentItem.getMatricula());
            */
            // returns the view for the current row
            return v;
        }

        static class ViewHolder{
            TextView brand, model, matr;
        }
    }
}
