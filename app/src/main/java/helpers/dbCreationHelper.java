package helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.os.Environment;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Dani_ on 28/10/2017.
 */

/*Clase encargada de instanciar el objeto que representa la base de datos.
* Hereda de la clase SQLiteOpenHelper que es la clase que posee el comportamiento para la ejecucion de querys
* dbCreationHelper se encarga de crear un objeto SQLiteOpenHelper especializado con el comportamiento de NUESTRA base de datos en particular
* */
public class dbCreationHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;//DATABASE_VERSION
    private static final String DATABASE_NAME = "PICKING_APP.db";
    private static final String SQL_CREATE_CODBARRA = "CREATE TABLE IF NOT EXISTS CodigoBarra (numero INTEGER, nombre TEXT, descripcion TEXT, largoTotal INTEGER, ubicacionCodProd INTEGER, largoCodProd INTEGER, ubicacionCantidad INTEGER, largoCantidad INTEGER, ubicacionPeso INTEGER, largoPeso INTEGER, ubicacionPrecio INTEGER, largoPrecio INTEGER, ubicacionFechaElab INTEGER, largoFechaElab INTEGER, ubicacionFechaVenc INTEGER, largoFechaVenc INTEGER, ubicacionDigitoVer INTEGER, largoDigitoVer INTEGER, ubicacionIdUsuario INTEGER, largoIdUsuario INTEGER, cantidadDecPeso INTEGER, PRIMARY KEY(numero));";
    private static final String SQL_CREATE_COMPROBANTE =  "CREATE TABLE IF NOT EXISTS Comprobante (numeroPick INTEGER, orden INTEGER, observaciones TEXT, puedeUsuario INTEGER, codArt INTEGER);";
    private static final String SQL_CREATE_ITEM = "CREATE TABLE IF NOT EXISTS Item (codigoArticulo TEXT, descripcion TEXT, unidad INTEGER, cantidad REAL, kilos REAL, puedePickear REAL, saldo REAL);";
    private static final String SQL_CREATE_USERCONFIG = "CREATE TABLE IF NOT EXISTS UserConfig (apiURL TEXT);";
    private static final String SQL_CREATE_USUARIO = "CREATE TABLE IF NOT EXISTS Usuario (idUsuario INTEGER, loginUsuario TEXT, estado INTEGER, nombre TEXT, area INTEGER);";
    private static final String SQL_CREATE_STOCK = "CREATE TABLE IF NOT EXISTS Stock(codigoArticulo TEXT, cantidad REAL, unidad INTEGER, kilos REAL, PRIMARY KEY(codigoArticulo));";
    private static final String SQL_CREATE_SERIALES ="CREATE TABLE IF NOT EXISTS Seriales(codigoArticulo TEXT, serial TEXT)";

    public dbCreationHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        getWritableDatabase().execSQL(SQL_CREATE_CODBARRA);
        getWritableDatabase().execSQL(SQL_CREATE_COMPROBANTE);
        getWritableDatabase().execSQL(SQL_CREATE_ITEM);
        getWritableDatabase().execSQL(SQL_CREATE_USERCONFIG);
        getWritableDatabase().execSQL(SQL_CREATE_USUARIO);
        getWritableDatabase().execSQL(SQL_CREATE_STOCK);
        getWritableDatabase().execSQL(SQL_CREATE_SERIALES);
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        /*db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);*/
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}