package fdi.ucm.carfinder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import fdi.ucm.carfinder.connection.Usuarios;

public class RegisterActivity extends AppCompatActivity {

    private UserRegisterTask mAuthTask = null;

    private EditText nameTextField;
    private EditText lastnameTextField;
    private EditText emailTextField;
    private EditText dateTextField;
    private Calendar c;
    private EditText pass1TextField;
    private EditText pass2TextField;
    private View mProgressView;
    private View mRegisterFormView;
    private View mStartingView;

    private String name;
    private String lastname;
    private String email;
    private String date;
    private String pass1;
    private String pass2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //
        nameTextField = (EditText)findViewById(R.id.nameTextField);
        lastnameTextField = (EditText)findViewById(R.id.lastnameTextField);
        emailTextField = (EditText)findViewById(R.id.emailTextField);
        dateTextField = (EditText)findViewById(R.id.dateTextField);
        pass1TextField = (EditText)findViewById(R.id.password1TextField);
        pass2TextField = (EditText)findViewById(R.id.password2TextField);

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.progress_register);
        mStartingView = findViewById(R.id.progress_register_message);

        gestionarFechaNacimiento();

        Button button = (Button)findViewById(R.id.register_ok_button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameTextField.getText().toString();
                lastname = lastnameTextField.getText().toString();
                email = emailTextField.getText().toString();
                date = dateTextField.getText().toString();
                pass1 = pass1TextField.getText().toString();
                pass2 = pass2TextField.getText().toString();
                registro(name, lastname, email,date, pass1, pass2);
            }
        });
    }

    private void gestionarFechaNacimiento() {
        c = Calendar.getInstance();

        //Genero un Action Listener para lanzar un Date Picker
        final EditText editText = (EditText)findViewById(R.id.dateTextField);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                establecerFecha(editText);
            }

        };

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(RegisterActivity.this, date, c
                        .get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



    }

    private void establecerFecha(EditText editText) {
        //Obtengo la fecha de hoy
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());

        editText.setText(formattedDate, TextView.BufferType.EDITABLE);
    }

    /**
     * Realiza el registro en el sistema, notifica de cualquier error producido en el proceso.
     */
    private void registro(String name, String lastName, String email, String date,
                          String pass1, String pass2) {
        if (mAuthTask != null) {
            return;
        }

        nameTextField.setError(null);
        lastnameTextField.setError(null);
        emailTextField.setError(null);
        dateTextField.setError(null);
        pass1TextField.setError(null);
        pass2TextField.setError(null);

        boolean cancel = false;
        View focusView = null;

        if(name.isEmpty() || !isNameValid(name)){
            nameTextField.setError("Nombre incorrecto");
            focusView = nameTextField;
            cancel = true;
        }
        else if(lastName.isEmpty() || !isNameValid(lastName)){
            lastnameTextField.setError("Apellidos incorrectos");
            focusView = lastnameTextField;
            cancel = true;
        }
        else if (TextUtils.isEmpty(pass1) && !isPasswordValid(pass1)) {
            pass1TextField.setError(getString(R.string.error_invalid_password));
            focusView = pass1TextField;
            cancel = true;
        }else if(!pass1.equals(pass2)){
            pass2TextField.setError(getString(R.string.error_passwords_doesnt_match));
            focusView = pass2TextField;
            cancel = true;
        }
        else if (TextUtils.isEmpty(email)) {
            emailTextField.setError(getString(R.string.error_field_required));
            focusView = emailTextField;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailTextField.setError(getString(R.string.error_invalid_email));
            focusView = emailTextField;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserRegisterTask(name, lastName, email, date, pass1, this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isNameValid(String name){
        char[] chars = name.toCharArray();
        for(char c : chars){
            if(!Character.isLetter(c)){
                return false;
            }
        }
        return true;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }


    /**
     * Muestra la interfaz de "Cargando"
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mStartingView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                mStartingView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mStartingView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mStartingView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private class UserRegisterTask extends AsyncTask<Void, Void, Boolean>{

        private final String mName;
        private final String mLastName;
        private final String mEmail;
        private final String mDate;
        private final String mPass1;
        private final Context contexto;
        private String msgError;


        UserRegisterTask(String name, String lastName, String email, String date,
                         String pass1, Context cont){
            mName = name;
            mLastName = lastName;
            mEmail = email;
            mDate = date;
            mPass1 = pass1;
            contexto = cont;
            msgError = "";
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Usuarios conexion = new Usuarios();
            JSONObject resultado = conexion.registrarUsuario(mEmail, mPass1, mName, mLastName, mDate);
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

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                msgError = "Cuenta creada con éxito!\nInicie sesión";
                AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
                builder.setMessage(msgError);
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
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
            showProgress(false);
        }
    }

}
