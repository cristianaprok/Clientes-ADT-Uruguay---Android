package com.zendesk.adtapp.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.zendesk.sdk.model.request.CreateRequest;
import com.zendesk.sdk.network.RequestProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

/**
 * Created by rahulramachandra on 24/07/17.
 */

public class CreateTicketActivity extends AppCompatActivity {
    private  String subject;
    private  String desc;
    private  String selectedSub;
    EditText subjectTxt,descTxt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zendesk.adtapp.R.layout.create_ticket_activity);
//        subjectTxt = (EditText) findViewById(R.id.subject);
        descTxt = (EditText) findViewById(com.zendesk.adtapp.R.id.desc);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.show();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(com.zendesk.adtapp.R.drawable.chaticon);


        String colors[] = {"Blanqueo de palabra clave"
                ,"Cambio de contacto"
                              ,"Clave sucursal virtual"
                              ,"Comprobantes de pago"
                              ,"Consultas mudanza"
                              ,"Consultas sobre cobranzas"
                              ,"Consultas sobre facturacion"
                              ,"Consultas tecnicas"
                              ,"Corporativo"
                              ,"Desbloqueo de Clave Sucursal Virtual"
                              ,"Documentacion por cambio de titularidad"
                              ,"Falla de Sistema"
                              ,"Infotecnica"
                              ,"Instalacion defectuosa"
                              ,"Llamado por baja bateria"
                              ,"Llamado por corte de energia"
                              ,"Modificacion de forma de pago"
                              ,"Modificacion de servicio"
                              ,"Otros motivos"
                              ,"Problema descarga de factura"
                              ,"Solicitar contacto"
                              ,"Solicitar factura"
                              ,"Solicitar numero de cliente"};

// Selection of the spinner
        final Spinner spinner = (Spinner) findViewById(com.zendesk.adtapp.R.id.spinner);

// Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, colors);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSub = spinner.getSelectedItem().toString();
//                subjectTxt.setText(selectedSub);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.zendesk.adtapp.R.menu.menu_create_profile, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.zendesk.adtapp.R.id.action_save) {
            if (descTxt.getText().toString().length()>0){
                ProgressDialog.show(this, "Ticket Creating", "");
                RequestProvider requestProvider = ZendeskConfig.INSTANCE.provider().requestProvider();
                CreateRequest request = new CreateRequest();

                request.setSubject(selectedSub);
                request.setDescription(descTxt.getText().toString());
                requestProvider.createRequest(request, new ZendeskCallback<CreateRequest>() {
                    @Override
                    public void onSuccess(CreateRequest createRequest) {
                        // Handle succes
                        Toast.makeText(getApplicationContext(),"Created Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(ErrorResponse errorResponse) {
                        // Handle error
                        Toast.makeText(getApplicationContext(),"Error Occured", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
