package fdi.ucm.carfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import fdi.ucm.carfinder.connection.Usuarios;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ChangeTask mAuthTask = null;
    private String newEmail;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        final SharedPreferences sp = getActivity().getSharedPreferences("Login",0);
        //Correspondiente al ver mis datos
        Button viewDataButton = (Button)view.findViewById(R.id.buttonView_my_data);
        viewDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view5 = inflater.inflate(R.layout.popup_data,null);

                final String user = "Email: \n\t" + sp.getString("User", null);
                final String name = "Nombre: \n\t" + sp.getString("name", null);
                final String lastName = "Apellidos: \n\t" + sp.getString("lastName", null);
                final String date = "Fecha: \n\t" + sp.getString("date", null);

                TextView mName = (TextView)view5.findViewById(R.id.view_data_name);
                TextView mLastName = (TextView)view5.findViewById(R.id.view_data_lastname);
                TextView mDate = (TextView)view5.findViewById(R.id.view_data_date);
                TextView mEmail = (TextView)view5.findViewById(R.id.view_data_email);

                mName.setText(name);
                mLastName.setText(lastName);
                mDate.setText(date);
                mEmail.setText(user);

                mBuilder.setView(view5);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });

        //Correspondiente al modificar mi email

        Button changeEmailButton = (Button)view.findViewById(R.id.buttonChange_email);
        changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view5 = inflater.inflate(R.layout.popup_change_user,null);

                final String user = sp.getString("User", null);
                final EditText mEmail = (EditText) view5.findViewById(R.id.new_user);

                Button okButton = (Button) view5.findViewById(R.id.button_modify_user);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newEmail = mEmail.getText().toString();
                        if(newEmail.contains("@")){
                            mAuthTask = new ChangeTask(user, newEmail, null, null, null, getContext(), 0);
                            mAuthTask.execute((Void) null);
                        }else{
                            mBuilder.setMessage("Las contraseñas no coinciden o no tienen la longitud necesaria").setTitle("Error");
                            AlertDialog alert = mBuilder.create();
                            alert.show();
                        }
                    }
                });
                mBuilder.setView(view5);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });

        //Correspondiente al modificar mi password
        Button changePasswordButton = (Button)view.findViewById(R.id.buttonChange_password);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view5 = inflater.inflate(R.layout.popup_change_password,null);

                final String user = sp.getString("User", null);
                final EditText mOldPass = (EditText) view5.findViewById(R.id.old_password);
                final EditText mNewPass1 = (EditText) view5.findViewById(R.id.new_password1);
                final EditText mNewPass2 = (EditText) view5.findViewById(R.id.new_password2);

                Button okButton = (Button)view5.findViewById(R.id.button_modify_password);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String oldPass = mOldPass.getText().toString();
                        String newPass1 = mNewPass1.getText().toString();
                        String newPass2 = mNewPass2.getText().toString();
                        if((newPass1 == newPass2) && (newPass1.length() > 3)){
                            mAuthTask = new ChangeTask(user, null, oldPass, newPass1, newPass2, getContext(), 1);
                            mAuthTask.execute((Void) null);
                        }else{
                            mBuilder.setMessage("Las contraseñas no coinciden o no tienen la longitud necesaria").setTitle("Error");
                            AlertDialog alert = mBuilder.create();
                            alert.show();
                        }
                    }
                });
                mBuilder.setView(view5);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });

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

    private class ChangeTask extends AsyncTask<Void, Void, Boolean> {

        private String newEmail;
        private String oldEmail;
        private String newPass1;
        private String newPass2;
        private String oldPass;
        private Context context;
        private JSONObject datos;
        private String msgError;
        private AlertDialog alertAbierto;
        private int opcion;

        ChangeTask(String oldEmail, String newEmail, String oldPass, String newPass1, String newPass2, Context context, int opcion) {
            this.oldEmail = oldEmail;
            this.newEmail = newEmail;
            this.oldPass = oldPass;
            this.newPass1 = newPass1;
            this.newPass2 = newPass2;
            this.context = context;
            this.opcion = opcion;
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            final SharedPreferences sp = getActivity().getSharedPreferences("Login",0);
            Usuarios conexion = new Usuarios();
            if (opcion == 0) {
                try {
                    JSONObject resultado = conexion.cambiarEmail(oldEmail, newEmail);
                    if (Integer.parseInt(resultado.get("errorno").toString()) == 0) {
                        datos = resultado;
                        SharedPreferences.Editor Ed = sp.edit();
                        Ed.putString("User", newEmail);
                        Ed.apply();
                        return true;
                    } else if (Integer.parseInt(resultado.get("errorno").toString()) != 2) {
                        msgError = resultado.get("errorMessage").toString();
                        return false;
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            } else if (opcion == 1) {
                try {
                    JSONObject resultado = conexion.cambiarPasswpord(oldEmail, newPass1);
                    if (Integer.parseInt(resultado.get("errorno").toString()) == 0) {
                        datos = resultado;
                        return true;
                    } else if (Integer.parseInt(resultado.get("errorno").toString()) != 2) {
                        msgError = resultado.get("errorMessage").toString();
                        return false;
                    }
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute (final Boolean success){
            mAuthTask = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if(success){
                msgError = "";
                builder.setMessage(msgError).setTitle("Exito!");
                AlertDialog alert = builder.create();
                alert.show();
            }else{
                builder.setMessage(msgError).setTitle("Error");
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
}
