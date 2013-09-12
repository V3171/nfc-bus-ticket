fffffffffffffffffffffffffff/**
 * 
 */
package uit.nfc.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.widget.Toast;

/**
 * @author XIII
 *
 */
public class NfcUtils {

	public static Tag getTag(Intent intent) {
		Tag tag = null;
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		}
		
		return tag;
	}
	
	public static void enableNdefExchangeMode(Activity activity) {
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0,
											new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		
		nfcAdapter.enableForegroundDispatch(activity, pendingIntent, new IntentFilter[] {tagDetected}, null);
	}
	
	public static void disableNdefExchangeMode(Activity activity) {
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		
		nfcAdapter.disableForegroundDispatch(activity);
	}
	
	public static void enableTagWriteMode(Activity activity) {
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0,
											new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		
		nfcAdapter.enableForegroundDispatch(activity, pendingIntent, new IntentFilter[] {tagDetected}, null);
	}
	
	public static void disableTagWriteMode(Activity activity) {
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		
		nfcAdapter.disableForegroundDispatch(activity);
	}
	
	public static NdefRecord createNdefTextRecord(String text) {
		byte[] textBytes = text.getBytes();
		int textLength = textBytes.length;
		byte[] payload = new byte[textLength];

		System.arraycopy(textBytes, 0, payload, 0, textLength);
		
		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
											new byte[0], payload);
		
		return record;
	}
	
	public static boolean writeTag(Tag tag, NdefMessage message, Context context){
		int messageLength = message.toByteArray().length;
		
		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				
				if (!ndef.isWritable()) {
					toast("Tag is read-only. Failed to write tag!", context);
					return false;
				}
				
				if (ndef.getMaxSize() < messageLength) {
					toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is "
							+ messageLength + " bytes. Failed to write tag!", context);
					return false;
				}
				
				ndef.writeNdefMessage(message);
				ndef.close();
				toast("Wrote message to pre-formatted tag successfully!", context);
				return true;
			} else {
				NdefFormatable ndefFormat = NdefFormatable.get(tag);
				if (ndefFormat != null) {
					ndefFormat.connect();
					ndefFormat.format(message);
					ndefFormat.close();
					toast("Formatted tag and wrote message to tag successfully!", context);
					return true;
				} else {
					toast("Tag doesn't support NDEF!", context);
					return false;
				}
			}
		} catch (Exception ex) {
			toast("Tag not detected or failed to write tag!", context);
			return false;
		}
	}
	
	public static NdefMessage readTag(Intent intent) {
		NdefMessage message = null;
		String action = intent.getAction();
		
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMessage != null) {
				message = (NdefMessage)rawMessage[0];
			} else {
				// Unknown tag type
				byte[] empty = new byte[]{};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
				message = new NdefMessage(new NdefRecord[]{record});
			}
		}
		
		return message;
 	}
	
	public static void toast(String text, Context context) {
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}
}
