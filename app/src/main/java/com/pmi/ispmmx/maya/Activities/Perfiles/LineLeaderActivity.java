package com.pmi.ispmmx.maya.Activities.Perfiles;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pmi.ispmmx.maya.Activities.LoginActivity;
import com.pmi.ispmmx.maya.Activities.ProfileActivity;
import com.pmi.ispmmx.maya.Adapters.Pages.RiperPagerAdapter;
import com.pmi.ispmmx.maya.DialogFragments.MarcasDialogFragment;
import com.pmi.ispmmx.maya.Fragments.AreaFragment;
import com.pmi.ispmmx.maya.Interfaces.ILineLeaderService;
import com.pmi.ispmmx.maya.Interfaces.IMarcaService;
import com.pmi.ispmmx.maya.Interfaces.IVolumenService;
import com.pmi.ispmmx.maya.Modelos.Entidades.Maquinaria.BussinesUnit;
import com.pmi.ispmmx.maya.Modelos.Entidades.Maquinaria.WorkCenter;
import com.pmi.ispmmx.maya.Modelos.Entidades.Marca;
import com.pmi.ispmmx.maya.Modelos.Entidades.VolumenesDeProduccion;
import com.pmi.ispmmx.maya.R;
import com.pmi.ispmmx.maya.Respuesta.RespuestaServicio;
import com.pmi.ispmmx.maya.Utils.CircleTransform;
import com.pmi.ispmmx.maya.Utils.Config.HostPreference;
import com.pmi.ispmmx.maya.Utils.User.OperadorPreference;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.pmi.ispmmx.maya.Utils.Config.HostPreference.URL_FOTOS_PERSONAS;

public class LineLeaderActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AreaFragment.OnInteractionListener,
        MarcasDialogFragment.Listener {

    private SharedPreferences pref;
    private Retrofit retrofit;

    private Toolbar _toolbar;
    private TabLayout _tabLayout;
    private DrawerLayout _drawer;
    private TextView _navUsername;
    private TextView _navPosition;
    private ViewPager _viewPager;
    private ImageView _imgUsuario;

    private AreaFragment areaFragment;

    private ILineLeaderService lineLeaderService;
    private IMarcaService marcaService;
    private IVolumenService volumenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_leader);
        pref = getSharedPreferences(OperadorPreference.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        elementosUI();
        personalizarHeaderBar();

        inicializarEntidades();
        iniciarRetrofit();
        createServicios();
        createFragments();
        generarTabs();

    }

    private void createServicios() {
        lineLeaderService = retrofit.create(ILineLeaderService.class);
        marcaService = retrofit.create(IMarcaService.class);
        volumenService = retrofit.create(IVolumenService.class);
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    private void inicializarEntidades() {

    }

    private void iniciarRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(HostPreference.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void elementosUI() {
        _toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(_toolbar);

        _drawer = findViewById(R.id.drawer_layout);
        _viewPager = findViewById(R.id.viewPager);
        NavigationView _navigationView = findViewById(R.id.nav_view);
        _navigationView.setNavigationItemSelectedListener(this);
        View _headerView = _navigationView.getHeaderView(0);
        _tabLayout = findViewById(R.id.tabLayout);

        _imgUsuario = _headerView.findViewById(R.id.img_persona);
        _navUsername = _headerView.findViewById(R.id.textViewNombreUser);
        _navPosition = _headerView.findViewById(R.id.textViewPuestoUser);
    }

    private void personalizarHeaderBar() {
        String nombre = pref.getString(OperadorPreference.NOMBRE_PERSONA_SHARED_PREF, "no available");
        String apellido1 = pref.getString(OperadorPreference.APELLIDO1_PERSONA_SHARED_PREF, "no available");
        String apellido2 = pref.getString(OperadorPreference.APELLIDO2_PERSONA_SHARED_PREF, "no available");
        String puesto = pref.getString(OperadorPreference.NOMBRE_PUESTO_SHARED_PREF, "no available");

        Picasso.with(getBaseContext())
                .load(URL_FOTOS_PERSONAS + pref.getInt(OperadorPreference.ID_PERSONA_SHARED_PREF, 0))
                .transform(new CircleTransform())
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .error(R.drawable.ic_error_outline_gray)
                .into(_imgUsuario);


        _imgUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPerfilActivity();
            }
        });

        _navUsername.setText(String.format("%s %s %s.", nombre, apellido1, apellido2));

        _navPosition.setText(puesto);

        menuAnimation();
    }

    private void menuAnimation() {
        ActionBarDrawerToggle _toggle = new ActionBarDrawerToggle(
                this, _drawer, _toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        _drawer.setDrawerListener(_toggle);
        _toggle.syncState();
    }

    private void createFragments() {
        areaFragment = new AreaFragment();
        areaFragment.workCenters = new ArrayList<>();
        areaFragment.iniciarAdapter();

    }

    private void generarTabs() {

        _tabLayout.addTab(_tabLayout.newTab().setText("Indicadores"));

        _tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        RiperPagerAdapter riperPagerAdapter = new RiperPagerAdapter(getSupportFragmentManager(), _tabLayout.getTabCount(), areaFragment);

        _viewPager.setAdapter(riperPagerAdapter);
        _viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(_tabLayout));


        _tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                _viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_actualizar) {
            String url = "https://serverpmi.tr3sco.net/Maya/Download";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.close_sesion) {
            alertLogOut();
        } else if (id == R.id.item_configuracion) {
            //startsettingsActivity();
        } else if (id == R.id.item_acerca_de_maya) {
            //startAboutMayaActivity();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void alertLogOut() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Segurpde que quieres cerrar session?");
        alertDialogBuilder.setPositiveButton("Si",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        remove();
                        logout();


                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        //Showing the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void messageDialog(String message) {
        Snackbar.make(findViewById(R.id.coordinator_line_leader), message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void logout() {


        //Starting login activity
        Intent intent = new Intent(LineLeaderActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);


    }

    private void remove() {
        pref.edit().clear().apply();
    }

    @Override
    public void getWorkCenters() {
        retrofiCallWorkCenters();
    }

    @Override
    public void onBadgeDefectoClick(WorkCenter workCenter, int position) {

    }

    @Override
    public void onBadgeParoClick(WorkCenter workCenter, int position) {

    }

    @Override
    public void onClickWorkCenter(WorkCenter workCenter) {
        retrofiCallMarcas(workCenter);
    }

    @Override
    public void onClickBusinessUnit(BussinesUnit bussinesUnit) {

    }

    @Override
    public boolean onLongClickWorkCenter(WorkCenter workCenter) {
        return false;
    }

    private void startPerfilActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void retrofiCallWorkCenters() {
        lineLeaderService.getWorkCenters(pref.getInt(OperadorPreference.ID_PERSONA_SHARED_PREF, 0)).enqueue(new Callback<RespuestaServicio<List<WorkCenter>>>() {
            @Override
            public void onResponse(@NonNull Call<RespuestaServicio<List<WorkCenter>>> call, @NonNull Response<RespuestaServicio<List<WorkCenter>>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getEjecucionCorrecta()) {
                            areaFragment.workCenters.clear();
                            areaFragment.workCenters.addAll(response.body().getRespuesta());
                            areaFragment.mAdapter.notifyDataSetChanged();
                        } else {
                            messageDialog(response.body().getMensaje());
                        }

                    }
                } else {
                    messageDialog(response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RespuestaServicio<List<WorkCenter>>> call, @NonNull Throwable t) {
                messageDialog("Por favor!! Validatu conexion a internet");
            }
        });
    }

    private void retrofiCallMarcas(final WorkCenter workCenter) {
        marcaService.getMarcas().enqueue(new Callback<List<Marca>>() {
            @Override
            public void onResponse(@NonNull Call<List<Marca>> call, @NonNull Response<List<Marca>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        startBSParosActivos(workCenter, response.body());
                    }
                } else {
                    messageDialog(response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Marca>> call, @NonNull Throwable t) {
                messageDialog("Por favor!! Validatu conexion a internet");
            }
        });
    }

    private void retrofiPostVolumen(VolumenesDeProduccion volumen) {
        volumenService.postVolumen(volumen).enqueue(new Callback<RespuestaServicio<VolumenesDeProduccion>>() {
            @Override
            public void onResponse(@NonNull Call<RespuestaServicio<VolumenesDeProduccion>> call, @NonNull Response<RespuestaServicio<VolumenesDeProduccion>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getEjecucionCorrecta()) {
                            messageDialog("Desperdicio generado correctamente #" + response.body().getRespuesta().getId());
                        } else {
                            messageDialog(response.body().getMensaje());
                        }


                    }
                } else {
                    messageDialog(response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RespuestaServicio<VolumenesDeProduccion>> call, @NonNull Throwable t) {
                messageDialog(t.getMessage());
            }
        });
    }

    private void startBSParosActivos(WorkCenter workCenter, List<Marca> marcas) {
        BottomSheetDialogFragment newFragment = MarcasDialogFragment.newInstance(workCenter, marcas);
        newFragment.show(getSupportFragmentManager(), newFragment.getTag());

    }

    @Override
    public void onMarcaClicked(final WorkCenter workCenter, final Marca marca) {
        AlertDialog.Builder builder;
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_NUMBER_FLAG_SIGNED);


        builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);

        builder
                .setView(input)
                .setTitle("Ingresa el Volumen producido")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        VolumenesDeProduccion volumen = new VolumenesDeProduccion();

                        volumen.setIdMarca(marca.getId());
                        volumen.setIdWorkCenter(workCenter.getId());
                        volumen.setIdPersona(pref.getInt(OperadorPreference.ID_PERSONA_SHARED_PREF, 0));
                        volumen.setCantidad(Double.parseDouble(input.getText().toString()));

                        retrofiPostVolumen(volumen);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();


    }


}
