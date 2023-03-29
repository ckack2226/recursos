package com.example.recursos;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private Activity activity;
    //version android
    private TextView versionAndroid;
    int versionSDK = 0;
    //bateria
    private ProgressBar pbLevelBattery;
    private TextView tvLevelBattery;
    IntentFilter batteryFilter;
    //bluetooth
    private BluetoothAdapter btAdapter;
    BluetoothManager bluetoothManager;
    private boolean flag = false;
    //files
    private EditText nameFile;
    private Archivo archivo;

    private TextView tvConexion;
    ConnectivityManager conexion;


    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Toast.makeText(context, "" + result.getData(), Toast.LENGTH_SHORT).show();
                            int resultado = result.getResultCode();
                            if (resultado == RESULT_OK) {
                                Toast.makeText(context, "Habilitando", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Operacion Cancelada", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        begin();
        batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, batteryFilter);
        nameFile = findViewById(R.id.etNameFile);

    }

    public void guardar(View view) {
        String nombreArchivo = nameFile.getText().toString();
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombreArchivo, Context.MODE_PRIVATE));
            archivo.write(nombreArchivo);
            archivo.flush();
            archivo.close();
            nameFile.setText("");
            Toast.makeText(context, "Se han guardado el archivo", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "No se pudo crear el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void methoFlag() {
        if (!flag) {
            bluetoothManager = getSystemService(BluetoothManager.class);
            btAdapter = bluetoothManager.getAdapter();
            flag = true;
        }
    }

    public void enableBT(View view) {
        methoFlag();
        try {
            if (btAdapter != null) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.activityResultLauncher.launch(enableIntent);
            }
        } catch (Exception e) {
            Log.i("Error BT", "Habilitando el BT" + e);
        }
    }

    public void disableBT(View view) {
        methoFlag();
        try {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter != null && btAdapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                btAdapter.disable();
                Toast.makeText(this, "Bluetooth desactivado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Error BT", "Desactivando BT: " + e.getMessage());
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        String versionSO= Build.VERSION.RELEASE;
        versionSDK = Build.VERSION.SDK_INT;
        versionAndroid.setText("version sistema operativo"+versionSO+"/SDK"+versionSDK);
        checkConnetion();
    }

    private void checkConnetion(){
        conexion = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conexion.getActiveNetworkInfo();
        boolean stateNetwork = networkInfo != null && networkInfo.isConnectedOrConnecting();
        if(stateNetwork) tvConexion.setText("state On");
        else tvConexion.setText("state OFF");
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int levelBattery=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            pbLevelBattery.setProgress(levelBattery);
            tvLevelBattery.setText("Level Battery : "+levelBattery+"%");
        }
    };
    private void begin(){
        this.context = getApplicationContext();
        this.activity = this;
        this.versionAndroid = findViewById(R.id.tvVersionAndroid);
        this.pbLevelBattery =findViewById(R.id.pbLevelBattery);
        this.tvLevelBattery = findViewById(R.id.tvLevelBattery);
        this.nameFile = findViewById(R.id.etNameFile);
        this.tvConexion = findViewById(R.id.tvConexion);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}