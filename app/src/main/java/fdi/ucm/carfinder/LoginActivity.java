package fdi.ucm.carfinder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import fdi.ucm.carfinder.connection.Usuarios;

/**
 * Pantalla de inicio de sesión
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Servicio en segundo plano que tramita las peticiones al servidor.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private CheckBox mRememberView;
    private View mProgressView;
    private View mLoginFormView;
    private View mStartingView;
    private String user = null;
    private String password = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mRememberView = (CheckBox) findViewById(R.id.remember);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mStartingView = findViewById(R.id.login_message);

        getSupportActionBar().setTitle(R.string.title_activity_login);

        cargarPreferencias();

        if (user != null && password != null) {
            iniciarSesion(user, password, true);
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    user = mEmailView.getText().toString();
                    password =  mPasswordView.getText().toString();
                    iniciarSesion(user, password, mRememberView.isChecked());
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                user = mEmailView.getText().toString();
                password =  mPasswordView.getText().toString();
                iniciarSesion(user, password, mRememberView.isChecked());
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.button_register);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

    }


    /**
     * Inicia la sesión
     */
    private void iniciarSesion(String email, String password, Boolean remember) {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password, this, remember);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Comprueba si el email es válido
     */
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    /**
    Comprueba si la longitud de la contraseña es apropiada.
     */
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Muestra el mensaje de carga de inicio de sesión.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mStartingView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    /**
     * Carga los datos almacenados en el almacenamiento interno.
     */
    private void cargarPreferencias() {
        SharedPreferences sp = this.getSharedPreferences("Login", MODE_PRIVATE);
        user = sp.getString("User", null);
        password = sp.getString("Pass", null);
    }

    @Override
    public void onBackPressed() {

    }

    /**
     * Actividad de inicio de sesión en segundo plano.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final Context contexto;
        private final Boolean mRemember;
        private String msgError;
        private String name;
        private String lastName;
        private String date;


        UserLoginTask(String email, String password, Context cont, Boolean remember) {
            mEmail = email;
            mPassword = password;
            contexto = cont;
            mRemember = remember;
            msgError = "";
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            Usuarios conexion = new Usuarios();
            JSONObject resultado = conexion.iniciarSesion(mEmail, mPassword);
            try {
                if (Integer.parseInt(resultado.get("errorno").toString()) != 0) {
                    msgError = resultado.get("errorMessage").toString();
                    return false;
                }
                else {
                    name = resultado.get("nombre").toString();
                    lastName = resultado.get("apellidos").toString();
                    date = resultado.get("fecha").toString();
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
                SharedPreferences sp=getSharedPreferences("Login", 0);
                SharedPreferences.Editor Ed=sp.edit();
                Ed.putString("User",mEmail );
                if (mRemember)
                    Ed.putString("Pass",mPassword);
                Ed.putString("name", name);
                Ed.putString("lastName", lastName);
                Ed.putString("date", date);
                Ed.apply();
                Intent intent = new Intent(contexto, MainActivity.class);
                startActivity(intent);
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


