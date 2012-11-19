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

public class CheckTicketActivity extends Activity {

	private Tag tag;
	private Context context;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_ticket);
        
        context = this;
    }

	@Override
	protected void onNewIntent(Intent intent) {
		tag = NfcUtils.getTag(intent);
    	
		NdefMessage message = NfcUtils.readTag(intent);
		if (message != null) {
			String prefix = new String(message.getRecords()[0].getPayload());
			if (prefix.equals(SellTicketActivity.PREFIX) && (message.getRecords().length > 1)) {
				String numberTicket = new String(message.getRecords()[1].getPayload());
				int remainTicket = Integer.parseInt(numberTicket) - 1;
				if (remainTicket >= 0) {
					NdefRecord[] records = new NdefRecord[2];
					records[0] = NfcUtils.createNdefTextRecord(SellTicketActivity.PREFIX);
					records[1] = NfcUtils.createNdefTextRecord(String.valueOf(remainTicket));
					NdefMessage msg = new NdefMessage(records);
					boolean success = NfcUtils.writeTag(tag, msg, context);
					
					if (success) {
						String tagId = tag.getId().toString();
						
						AlertDialog dialog = new AlertDialog.Builder(this).create();
						dialog.setTitle("Notice");
						dialog.setMessage("Valid tag! Tag id " + tagId + " is remaining " + remainTicket + " times.");
						
						dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
						
						dialog.show();
					}
					
					return;
				}
			}
		}
		
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setTitle("Notice");
		dialog.setMessage("Invalid tag or Tag has expired tickets! Do you want sell ticket for this tag?");
		
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Bundle bundle = new Bundle();
				bundle.putParcelable("TagDetected", tag);
				
				Intent sellTicket = new Intent(CheckTicketActivity.this, SellTicketActivity.class);
				sellTicket.putExtras(bundle);
				startActivity(sellTicket);
				
				finish();
			}
		});
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		
		dialog.show();
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
        getMenuInflater().inflate(R.menu.activity_check_ticket, menu);
        return true;
    }
}
