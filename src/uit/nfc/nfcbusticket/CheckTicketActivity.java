package uit.nfc.nfcbusticket;

import java.text.SimpleDateFormat;
import java.util.Date;

import uit.nfc.utils.NfcUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Menu;

public class CheckTicketActivity extends Activity {

	private Tag tag;
	private String tagId;
	private Context context;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_ticket);
        
        context = this;
        tagId = null;
    }

	@Override
	protected void onNewIntent(Intent intent) {
		tag = NfcUtils.getTag(intent);
		
		NdefMessage message = NfcUtils.readTag(intent);
		if (message != null) {
			String prefix = new String(message.getRecords()[0].getPayload());
			if (prefix.equals(SellTicketActivity.PREFIX) && (message.getRecords().length > 3)) {
				tagId = new String(message.getRecords()[1].getPayload());
				String numberTicket = new String(message.getRecords()[2].getPayload());
				int remainTicket = Integer.parseInt(numberTicket) - 1;
				String prevTime = new String(message.getRecords()[3].getPayload());
				if (remainTicket >= 0) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					String currentTime = dateFormat.format(new Date());
					
					NdefRecord[] records = new NdefRecord[4];
					records[0] = NfcUtils.createNdefTextRecord(SellTicketActivity.PREFIX);
					records[1] = NfcUtils.createNdefTextRecord(tagId);
					records[2] = NfcUtils.createNdefTextRecord(String.valueOf(remainTicket));
					records[3] = NfcUtils.createNdefTextRecord(currentTime);
					NdefMessage msg = new NdefMessage(records);
					boolean success = NfcUtils.writeTag(tag, msg, context);
					
					if (success) {
						
						AlertDialog dialog = new AlertDialog.Builder(this).create();
						dialog.setTitle("Notice");
						dialog.setMessage("Valid tag! Tag id " + tagId + " is remaining " + remainTicket + " times.\nPrevious checked time: " + prevTime + ".\nLast checked time:" + currentTime);
						
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
		
		String notice = "Invalid tag! Do you want sell ticket for this tag?";
		if (tagId != null) {
			notice = "Tag id " + tagId + "has expired tickets! Do you want sell ticket for this tag?";
		}
		
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setTitle("Notice");
		dialog.setMessage(notice);
		
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Bundle bundle = new Bundle();
				bundle.putParcelable("TagDetected", tag);
				bundle.putString("TagId", tagId);
				
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
