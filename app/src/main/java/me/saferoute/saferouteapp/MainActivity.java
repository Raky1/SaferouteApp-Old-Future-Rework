package me.saferoute.saferouteapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import me.saferoute.saferouteapp.Model.Usuario;
import me.saferoute.saferouteapp.Tools.Validacao;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NavigationView.OnCreateContextMenuListener {

    //Login persist
    private Usuario user = null;

    private MapsFragment mapsFragment;
    private FragmentManager fragmentManager;

    //scroll e mapbtn
    private MenuItem navLogar;
    private LinearLayout linearLayout;
    private ImageView btnGps;
    private AutoCompleteTextView txtSearch;

    //Dialogs
    private AlertDialog dialogDicas, dialogPrivacidade, dialogSobre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Verifica conectividade com internet
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isConnected())
            finish();

        //searchs
        txtSearch = (AutoCompleteTextView) findViewById(R.id.txtSearch);
        btnGps = (ImageView) findViewById(R.id.icon_gps);

        //Botões filtro
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutScrollFiltro);
        this.configButton();

        init();
    }

    //configura os botões
    private void configButton() {
        for(int i = 0; i < linearLayout.getChildCount(); i++)
            linearLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(int i = 0; i < linearLayout.getChildCount(); i++)
                        linearLayout.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);

                    view.setBackgroundColor(Color.rgb(100,100,100));
                }
            });
    }

    private void init() {
        //Barra lateral de navegação...
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navLogar = (MenuItem)navigationView.getMenu().findItem(R.id.nav_logar);

        //set fragment Map
        mapsFragment = new MapsFragment();
        mapsFragment.setTxtSearch(txtSearch);
        mapsFragment.setBtnGps(btnGps);

        //change fragment
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, mapsFragment, "MapsFragment");
        fragmentTransaction.commitAllowingStateLoss();

        //****************************************************************************************************************************
        //Configura Dialogs
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        //dialog Dicas de seguraça
        View view = getLayoutInflater().inflate(R.layout.dialog_dicas, null);
        builder.setView(view);
        dialogDicas = builder.create();

        //dialog Politica de Privacidade
        view = getLayoutInflater().inflate(R.layout.dialog_privacidade, null);
        builder.setView(view);
        dialogPrivacidade = builder.create();

        //dialog Sobre
        view = getLayoutInflater().inflate(R.layout.dialog_sobre, null);
        builder.setView(view);
        dialogSobre = builder.create();

        //****************************************************************************************************************************


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //@SuppressWarnings("StatementWithEmptyBody") //acho desnecessário
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_logar) {
            if(item.getTitle().toString() == getResources().getString(R.string.nav_logar))
                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), 901);
            else {
                user = null;
                item.setTitle(getResources().getString(R.string.nav_logar));
            }

        } else if (id == R.id.nav_fui_roubado) {
            if(user != null) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mapsFragment.setMode(1);//mode de captura de coordenadas
            } else
                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), 901);

        } else if (id == R.id.nav_suas_ocorrencias) {
            if(user != null) {
                Intent intent = new Intent(MainActivity.this, ListaActivity.class);
                intent.putExtra("id", user.getId());
                startActivity(intent);
            } else
                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), 901);

        } else if (id == R.id.nav_dicas) {
            dialogDicas.show();

        } else if (id == R.id.nav_privacidade) {
            dialogPrivacidade.show();

        } else if (id == R.id.nav_sobre) {
            dialogSobre.show();

        } else if (id == R.id.nav_personal) {
            mapsFragment.getDeviceLocation();
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void showROcorrencia(double latitude, double longitude) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        Intent intent = new Intent(MainActivity.this, OcorrenciaActivity.class);
        intent.putExtra("id", user.getId());
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        //Login ok
        if (requestCode == 901 && resultCode == RESULT_OK) {
            user = new Usuario();
            user.setId(data.getIntExtra("id", 0));
            user.setEmail(data.getStringExtra("email"));
            user.setSenha(data.getStringExtra("senha"));
            navLogar.setTitle(getResources().getString(R.string.nav_logado));
        }

    }
}
