package uit.nfc.nfcbusticket;

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
	private EditText editTicket;
	private Button buttonWrite;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_ticket);
        
        Bundle b = this.getIntent().getExtras();
        if (b != null) {
        	tag = (Tag)b.getParcelable("TagDetected");
        }
        
        context = this;
        editTicket = (EditText)findViewById(R.id.edit_ticket);
        buttonWrite = (Button)findViewById(R.id.button_write);
        
        buttonWrite.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				String numberTicket = editTicket.getText().toString();
				if (numberTicket.isEmpty() || numberTicket.startsWith("0")) {
					NfcUtils.toast("Please enter number of tickets!", context);
				} else {
					NdefRecord[] records = new NdefRecord[2];
					records[0] = NfcUtils.createNdefTextRecord(PREFIX);
					records[1] = NfcUtils.createNdefTextRecord(numberTicket);
					NdefMessage message = new NdefMessage(records);
					boolean success = NfcUtils.writeTag(tag, message, context);
					
					if (success) {
						String tagId = tag.getId().toString();
						
						AlertDialog dialog = new AlertDialog.Builder(context).create();
						dialog.setTitle("Notice");
						dialog.setMessage("Valid tag! Tag id " + tagId + " is remaining " + numberTicket + " times.");
						
						dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
						
						dialog.show();
					}
				}
			}
		});
    }

    @Override
	protected void onNewIntent(Intent intent) {
		tag = NfcUtils.getTag(intent);
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
