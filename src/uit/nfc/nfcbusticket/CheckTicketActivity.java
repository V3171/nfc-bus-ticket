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

public class CheckTicketActivity extends Activity {

	private Tag tag;
	private String tagId;
	private int numberTicket;
	private int remainTicket;
	private String prevTime;
	private Context context;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_ticket);
        
        context = this;
        tagId = null;
        numberTicket = 0;
        remainTicket = 0;
        prevTime = null;
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
		tag = NfcUtils.getTag(this.getIntent());
		
		NdefMessage message = NfcUtils.readTag(this.getIntent());
		if (message != null) {
			String prefix = new String(message.getRecords()[0].getPayload());
			if (prefix.equals(SellTicketActivity.PREFIX) && (message.getRecords().length > 3)) {
				tagId = new String(message.getRecords()[1].getPayload());
				numberTicket = Integer.parseInt(new String(message.getRecords()[2].getPayload()));
				remainTicket = numberTicket - 1;
				prevTime = new String(message.getRecords()[3].getPayload());
				if (remainTicket >= 0) {
					handleValidTag();
					return;
				}
			}
		}
		
		handleInvalidTag();
		
		NfcUtils.enableTagWriteMode(this);
	}
	
	private void handleValidTag() {
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setTitle("Notice");
		dialog.setMessage("Tag id " + tagId + " is remaining " + numberTicket + " times.\nLast checked time: " + prevTime + ".\nDo you want check or sell ticket?");
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Check", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
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
					
					AlertDialog OkDialog = new AlertDialog.Builder(context).create();
					OkDialog.setTitle("Notice");
					OkDialog.setMessage("Tag id " + tagId + " is remaining " + remainTicket + " times.\nPrevious checked time: " + prevTime + ".\nLast checked time: " + currentTime + ".\nWelcome you!");
					
					OkDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
					
					OkDialog.show();
				}
			}
			
		});
		
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Sell", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Bundle bundle = new Bundle();
				bundle.putString("TagId", tagId);
				bundle.putInt("NumberTicket", numberTicket);
				
				Intent sellTicket = new Intent(CheckTicketActivity.this, SellTicketActivity.class);
				sellTicket.putExtras(bundle);
				startActivity(sellTicket);
				
				finish();
			}
		});
		
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Done", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {		
				finish();
			}
		});
		
		dialog.show();
	}
	
	private void handleInvalidTag() {
		String notice = "Invalid tag! Do you want sell ticket for this tag?";
		if (tagId != null) {
			notice = "Tag id " + tagId + " has expired tickets! Do you want sell ticket for this tag?";
		}
		
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setTitle("Notice");
		dialog.setMessage(notice);
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Yes", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Bundle bundle = new Bundle();
				bundle.putString("TagId", tagId);
				bundle.putInt("NumberTicket", numberTicket);
				
				Intent sellTicket = new Intent(CheckTicketActivity.this, SellTicketActivity.class);
				sellTicket.putExtras(bundle);
				startActivity(sellTicket);
				
				finish();
			}
		});
		
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "No", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		
		dialog.show();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_check_ticket, menu);
        return true;
    }
}
