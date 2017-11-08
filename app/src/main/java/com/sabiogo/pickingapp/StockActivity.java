package com.sabiogo.pickingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data_access.CodigoBarraDAO;
import data_access.SerialDAO;
import data_access.StockDAO;
import data_access.UserConfigDAO;
import entities.CodigoBarra;
import entities.Comprobante;
import entities.Item;
import entities.ItemStock;
import entities.Serial;
import helpers.GsonRequest;
import helpers.WSHelper;

/**
 * Created by Federico on 28/10/2017.
 */

public class StockActivity extends Activity {

    private static final String TAG = "StockActivity";
    public static final String PREFS_NAME = "mPrefs";
    private final String ID_USUARIO = "id_usuario";
    private final String DefaultID = "";
    private final float UNO = 1;
    public StockAdapter stockAdapter;
    List<ItemStock> listadoItemStock;

    //public MyItemStockRecyclerViewAdapter adapter;
    private String id_usuario;

    Button btn_salir, btn_grabar, btn_agregarProducto;
    //FloatingActionButton btn_agregarManual;
    ListView lv_articulos;
    EditText txt_codigo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        btn_grabar = (Button)findViewById(R.id.btn_grabarStock);
        btn_salir  = (Button)findViewById(R.id.btn_salirStock);
        btn_agregarProducto = (Button)findViewById(R.id.btn_agregarProductoStock);
        //btn_agregarManual = (FloatingActionButton)findViewById(R.id.fab_agregarCodBarManualStock);
        lv_articulos = (ListView)findViewById(R.id.lv_itemsStock);
        txt_codigo = (EditText)findViewById(R.id.txt_CodigoStock);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        id_usuario = settings.getString(ID_USUARIO, DefaultID);

        createTextListener();

        btn_salir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                salir();
            }
        });

        /*btn_agregarManual.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                agregarManual();
            }
        });*/

        txt_codigo.setText("12345678911234567891123456");

        btn_agregarProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarArticulo(txt_codigo.getText().toString());
            }
        });

        btn_grabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Comprobante comprobante = armarComprobanteStock();
                if(comprobante != null){
                    grabarComprobanteStock(comprobante);
                }
                else{
                    Toast.makeText(getBaseContext(), "Error en la creación del comprobante", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Una vez seteados los valores de la interfaz, verificamos si existe un conteo de stock pendiente y de ser asi lo mostramos en pantalla
        listadoItemStock = StockDAO.getStockList(getApplicationContext());

        stockAdapter = new StockAdapter(this, R.layout.listview_row,listadoItemStock);
        lv_articulos.setAdapter(stockAdapter);
    }

    private void salir(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StockActivity.this);
        alertDialogBuilder
                .setTitle("Salir")
                .setMessage("¿Esta seguro que desea salir?")
                .setPositiveButton("No", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Si", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        borrarRegistros();
                        Log.d(TAG, "nextActivity: avanzando a la vista opciones");
                        Intent intent = new Intent(getApplicationContext(), OpcionesActivity.class);
                        startActivityForResult(intent,0);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void agregarManual(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
        final EditText input = new EditText(getApplicationContext());
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        alertDialogBuilder
                .setTitle("Ingrese el codigo de barra del articulo")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        if(!input.getText().toString().equals("")){
                            agregarArticulo(input.getText().toString());
                        }
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setView(input);
        alertDialog.show();
    }

    public void createTextListener(){
        txt_codigo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //ESTE METODO SE VA A IMPLEMENTAR UNA VEZ QUE TENGAN EL ANILLO, CUANDO CARGUEN AUTOMATICAMENTE LOS CODIGO DE BARRA. EL USUARIO NO VA A TENER ACCESO DE FORMA MANUAL A ESTE CAMPO, Y SI INGRESA DE FORMA MANUAL ES A TRAVES DE OTRO CAMPO

                if(!txt_codigo.getText().toString().equals("")){
                    //LLAMAR AL METODO QUE COMPRUEBA SI EL CODIGO INGRESADO ES VALIDO O NO (TRUE O FALSE)
                    //IF(CODIGO ES VALIDO) ENTONCES TOAST = CODIGO AGREGADO ELSE CODIGOERRONEO

                    //TXT.CODIGO.SETTEXT("");
                }
            }
        });
    }

    public Boolean agregarArticulo(String serial){
        boolean result;
        Float kilos;
        List<CodigoBarra> listadoCodBarra = CodigoBarraDAO.getCodigosBarra(getApplicationContext());

        if (!serial.equals("")){
            CodigoBarra codigoBarra = verificarCodigoBarra(listadoCodBarra, serial);

            if (codigoBarra != null){
                Integer codArt = codigoBarra.getCodigoArticulo(serial);
                kilos = codigoBarra.getKilos(serial);

                //TODO Modificar tipo
                ItemStock item = new ItemStock(Integer.toString(codArt), UNO, UNO, kilos);

                listadoItemStock = StockDAO.leerItemStock(getApplicationContext(),item);

                Toast.makeText(getBaseContext(), R.string.producto_agregado, Toast.LENGTH_LONG).show();
                //adapter = new MyItemStockRecyclerViewAdapter(listadoItemStock, null);

                stockAdapter = new StockAdapter(this, R.layout.listview_row,listadoItemStock);
                lv_articulos.setAdapter(stockAdapter);

                //rv_articulos.setAdapter(adapter);
                Serial serialNuevo = new Serial(serial);
                SerialDAO.grabarSerial(getApplicationContext(), serialNuevo, item.getCodigoArticulo());
            }
            result = true;
        }
        else{
            Toast.makeText(getBaseContext(), R.string.serial_invalido, Toast.LENGTH_LONG).show();
            result =  false;
        }
        txt_codigo.setText("98765432109876543210123456");
        return result;
    }

    public CodigoBarra verificarCodigoBarra(List<CodigoBarra> listadoCodBarra, String serial){
        for (CodigoBarra codBar : listadoCodBarra) {
            if(serial.length() == codBar.getLargoTotal()){
                return codBar;
            }
        }
        return null;
    }

    private Comprobante armarComprobanteStock() {
        Comprobante comprobante = new Comprobante();
        List<ItemStock> listadoItemsStock;
        List<Item> listadoItem = new ArrayList<>();

        listadoItemsStock = StockDAO.getStockList(getApplicationContext());
        if (listadoItemsStock.size() > 0){
            for (ItemStock itemStock: listadoItemsStock) {
                Item item = new Item();
                item.setCodigoArticulo(itemStock.getCodigoArticulo());
                item.setDescripcion("");
                item.setUnidad((int)itemStock.getUnidad());
                item.setCantidad(itemStock.getCantidad());
                item.setKilos(itemStock.getKilos());
                item.setPuedePickear(0);
                item.setSaldo(0);

                listadoItem.add(item);
            }

            for (Item item: listadoItem) {
                List<Serial> seriales= SerialDAO.getSerialesArticulo(getApplicationContext(), item.getCodigoArticulo());
                item.setSeriales(seriales);
            }

            comprobante.setItems(listadoItem);
            comprobante.setNumeroPick(0);
            comprobante.setObservaciones(getString(R.string.mov_stock));
            comprobante.setOrden(0);
            comprobante.setPuedeUsuario(0);

            return comprobante;
        }else {
            return null;
        }
    }

    public void grabarComprobanteStock(final Comprobante comprobante){
        //VER COMO SE HACE PARA MANDAR UN JSON A LA URL QUE ESTOY METIENDO. EN ESTE CODIGO NO ESTOY CARGANDO EL JSON EN NINGUN MOMENTO
        JSONObject jsonBody;
/*      URL url = new URL("http://" + UserConfigDAO.getUserConfig(getApplicationContext()).getApiUrl() + getString(R.string.api_ingresarStock) + id_usuario);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream is = urlConnection.getInputStream();*/
        try {
            /*jsonBody = new JSONObject();
            jsonBody.put("", comprobante);*/

            String url = "http://" + UserConfigDAO.getUserConfig( StockActivity.this).getApiUrl() + ":3000" + getString(R.string.api_ingresarStock) + id_usuario;

/*            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("comprobante", comprobante.toString());*/

            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");

            GsonRequest request = new GsonRequest(url,comprobante,Comprobante.class,headers, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });

           /* JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, response.toString());
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            jsonObjReq.setTag(TAG);*/

    /* if (queue!= null) {
    queue.cancelAll(TAG);
    } */

            /*StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    })
            {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("comprobante", comprobante.toString());
                    return params;
                }
            };*/

            /*StringRequest stringRequest = new StringRequest(1, "http://" + UserConfigDAO.getUserConfig(getApplicationContext()).getApiUrl() + getString(R.string.api_ingresarStock) + id_usuario,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(response.toString().equals("true")){
                                borrarRegistros();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
                                alertDialogBuilder
                                        .setTitle("Comprobante")
                                        .setMessage("Comprobante guardado con éxito")
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface dialog, int id){
                                                Intent intent = new Intent(getApplicationContext(), OpcionesActivity.class);
                                                startActivityForResult(intent,0);
                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
                    alertDialogBuilder
                            .setTitle("Error")
                            .setMessage(error.toString())
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                }
            });*/

            WSHelper.getInstance(getApplicationContext()).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void borrarRegistros(){
        StockDAO.borrarItemStock(getApplicationContext());
        SerialDAO.borrarSeriales(getApplicationContext());
    }
}
