package fdi.ucm.carfinder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd");
        String formattedDate = df.format(c.getTime());

        editText.setText(formattedDate, TextView.BufferType.EDITABLE);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void registro(String name, String lastName, String email, String date,
                          String pass1, String pass2) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        nameTextField.setError(null);
        lastnameTextField.setError(null);
        emailTextField.setError(null);
        dateTextField.setError(null);
        pass1TextField.setError(null);
        pass2TextField.setError(null);

        boolean cancel = false;
        View focusView = null;

        //Check for valid name
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
        // Check for a valid password, if the user entered one and check if they are equals.
        else if (TextUtils.isEmpty(pass1) && !isPasswordValid(pass1)) {
            pass1TextField.setError(getString(R.string.error_invalid_password));
            focusView = pass1TextField;
            cancel = true;
        }else if(!pass1.equals(pass2)){
            pass2TextField.setError(getString(R.string.error_passwords_doesnt_match));
            focusView = pass2TextField;
            cancel = true;
        }
        // Check for a valid email address.
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
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserRegisterTask(name, lastName, email, date, pass1, pass2, this);
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
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mStartingView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private class UserRegisterTask extends AsyncTask<Void, Void, Boolean>{

        private final String mName;
        private final String mLastName;
        private final String mEmail;
        private final String mDate;
        private final String mPass1;
        private final String mPass2;
        private final Context contexto;
        private String msgError;


        UserRegisterTask(String name, String lastName, String email, String date,
                         String pass1, String pass2, Context cont){
            mName = name;
            mLastName = lastName;
            mEmail = email;
            mDate = date;
            mPass1 = pass1;
            mPass2 = pass2;
            contexto = cont;
            msgError = "";
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Usuarios conexion = new Usuarios();
            JSONObject resultado = conexion.registrarUsuario(email, pass1, name, lastname, date);
            try {
                if (Integer.parseInt(resultado.get("errorno").toString()) != 0) {
                    msgError = resultado.get("errorMessage").toString();
                    return false;
                } else {
                    msgError = "Cuenta creada con éxito!\nInicie sesión";
                    AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
                    builder.setMessage(msgError);
                    AlertDialog alert = builder.create();
                    alert.show();
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
                finish();
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
