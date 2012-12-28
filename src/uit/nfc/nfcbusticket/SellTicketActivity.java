package uit.nfc.nfcbusticket;

import java.text.SimpleDateFormat;
import java.util.Date;

import uit.nfc.utils.NfcUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SellTicketActivity extends Activity {

	public static final String PREFIX = "NBT";
	
	private Tag tag;
	private Context context;
	private EditText editId;
	private EditText editRemainTicket;
	private EditText editTicket;
	private Button buttonWrite;
	
	private NdefMessage message;
	private String tagId;
	private String numberTicket;
	private int remainTicket;
	private String currentTime;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_ticket);
        
        context = this;
        editId = (EditText)findViewById(R.id.edit_id);
        editRemainTicket = (EditText)findViewById(R.id.edit_remain_ticket);
        editTicket = (EditText)findViewById(R.id.edit_ticket);
        buttonWrite = (Button)findViewById(R.id.button_write);
        
        tagId = null;
        numberTicket = null;
        remainTicket = 0;
        currentTime = null;
        
        Bundle b = this.getIntent().getExtras();
        if (b != null) {
        	String tagId = b.getString("TagId");
        	remainTicket = b.getInt("NumberTicket");
        	if (tagId != null) {
        		editId.setText(tagId);
        		editId.setEnabled(false);
        	}
        }
        
        editRemainTicket.setText(String.valueOf(remainTicket));
        editRemainTicket.setEnabled(false);
        
        buttonWrite.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				tagId = editId.getText().toString();
				numberTicket = editTicket.getText().toString();
				
				if (tagId.isEmpty() || numberTicket.isEmpty() || numberTicket.startsWith("0")) {
					NfcUtils.toast("Please enter Tag Id or Number of tickets!", context);
				} else {
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					currentTime = dateFormat.format(new Date());
					numberTicket = String.valueOf(remainTicket + Integer.parseInt(numberTicket));
					
					NdefRecord[] records = new NdefRecord[4];
					records[0] = NfcUtils.createNdefTextRecord(PREFIX);
					records[1] = NfcUtils.createNdefTextRecord(tagId);
					records[2] = NfcUtils.createNdefTextRecord(numberTicket);
					records[3] = NfcUtils.createNdefTextRecord(currentTime);
					message = new NdefMessage(records);
					
					NfcUtils.toast("Please touch tag to write!", v.getContext());
					NfcUtils.enableTagWriteMode((Activity)v.getContext());
				}
			}
		});
    }

    @Override
	protected void onNewIntent(Intent intent) {
    	tag = NfcUtils.getTag(intent);
    	
    	boolean success = NfcUtils.writeTag(tag, message, context);
		if (success) {
			
			AlertDialog dialog = new AlertDialog.Builder(context).create();
			dialog.setTitle("Notice");
			dialog.setMessage("Tag id " + tagId + " is remaining " + numberTicket + " times.\nTime of sell: " + currentTime);
			
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			dialog.show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		NfcUtils.disableTagWriteMode(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		NfcUtils.enableTagWriteMode(this);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sell_ticket, menu);
        return true;
    }
}
