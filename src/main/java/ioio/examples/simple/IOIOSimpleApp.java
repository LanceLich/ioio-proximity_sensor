package ioio.examples.simple;

import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;

public class IOIOSimpleApp extends IOIOActivity {
	private TextView textView_1; //textview to print range data
    public static final byte VL6180X_addr = 0x29; //default address for VL6180X

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		textView_1 = (TextView) findViewById(R.id.TextView1);
	}

	class Looper extends BaseIOIOLooper {
		private TwiMaster twi_; // set up I2C

		@Override
		public void setup() throws ConnectionLostException,InterruptedException {
			twi_ = ioio_.openTwiMaster(1, TwiMaster.Rate.RATE_400KHz, false);

            // load settings!

            // private settings from page 24 of app note
            write_request(twi_,(short)0x0207, (byte)0x01);
            write_request(twi_,(short)0x0208, (byte)0x01);
            write_request(twi_,(short)0x0096, (byte)0x00);
            write_request(twi_,(short)0x0097, (byte)0xfd);
            write_request(twi_,(short)0x00e3, (byte)0x00);
            write_request(twi_,(short)0x00e4, (byte)0x04);
            write_request(twi_,(short)0x00e5, (byte)0x02);
            write_request(twi_,(short)0x00e6, (byte)0x01);
            write_request(twi_,(short)0x00e7, (byte)0x03);
            write_request(twi_,(short)0x00f5, (byte)0x02);
            write_request(twi_,(short)0x00d9, (byte)0x05);
            write_request(twi_,(short)0x00db, (byte)0xce);
            write_request(twi_,(short)0x00dc, (byte)0x03);
            write_request(twi_,(short)0x00dd, (byte)0xf8);
            write_request(twi_,(short)0x009f, (byte)0x00);
            write_request(twi_,(short)0x00a3, (byte)0x3c);
            write_request(twi_,(short)0x00b7, (byte)0x00);
            write_request(twi_,(short)0x00bb, (byte)0x3c);
            write_request(twi_,(short)0x00b2, (byte)0x09);
            write_request(twi_,(short)0x00ca, (byte)0x09);
            write_request(twi_,(short)0x0198, (byte)0x01);
            write_request(twi_,(short)0x01b0, (byte)0x17);
            write_request(twi_,(short)0x01ad, (byte)0x00);
            write_request(twi_,(short)0x00ff, (byte)0x05);
            write_request(twi_,(short)0x0100, (byte)0x05);
            write_request(twi_,(short)0x0199, (byte)0x05);
            write_request(twi_,(short)0x01a6, (byte)0x1b);
            write_request(twi_,(short)0x01ac, (byte)0x3e);
            write_request(twi_,(short)0x01a7, (byte)0x1f);
            write_request(twi_,(short)0x0030, (byte)0x00);

            // Recommended : Public registers - See data sheet for more detail
            write_request(twi_,(short)0x0011, (byte)0x10);       // Enables polling for 'New Sample ready'
            // when measurement completes
            write_request(twi_,(short)0x010a, (byte)0x30);       // Set the averaging sample period
            // (compromise between lower noise and
            // increased execution time)
            write_request(twi_,(short)0x003f, (byte)0x46);       // Sets the light and dark gain (upper
            // nibble). Dark gain should not be
            // changed.
            write_request(twi_,(short)0x0031, (byte)0xFF);       // sets the # of range measurements after
            // which auto calibration of system is
            // performed
            write_request(twi_,(short)0x0040, (byte)0x63);       // Set ALS integration time to 100ms
            write_request(twi_,(short)0x002e, (byte)0x01);       // perform a single temperature calibration
            // of the ranging sensor

            // Optional: Public registers - See data sheet for more detail
            write_request(twi_,(short)0x001b, (byte)0x09);       // Set default ranging inter-measurement
            // period to 100ms
            write_request(twi_,(short)0x003e, (byte)0x31);       // Set default ALS inter-measurement period
            // to 500ms
            write_request(twi_,(short)0x0014, (byte)0x24);       // Configures interrupt on 'New Sample
            // Ready threshold event'

		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
            if (!(((read_request(twi_,(short)0x4d)) & 0x01) == 0)) { // check if the sensor ready for the next operation
                write_request(twi_, (short)0x18, (byte) 0x01); // write 0x01 to register 0x18 to start a single-shot range measurement
                if (!(((read_request(twi_,(short)0x4f)) & 0x04) == 0)) { // check if the measurement is complete
                    setNumber1(read_request(twi_, (short) 0x62)); // read the range and print it on the screen
                    write_request(twi_,(short)0x15,(byte)0x07); // clear the interrupt status
                } else Log.i("LICH", "data not ready");
            }else Log.i("LICH", "sensor read not ready");
			Thread.sleep(50);
		}

	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void setNumber1(byte b) {
		final String str = String.valueOf(b);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_1.setText(str);
			}
		});
	}

    private void write_request(TwiMaster twi, short reg, byte data) throws ConnectionLostException,InterruptedException{
        byte[] write = new byte[] {(byte)(reg >> 8),(byte)reg,data}; // write 1 byte data to 2 bytes address of register
        byte[] empty_response = new byte[] {};
	    if (!twi.writeRead(VL6180X_addr, false, write, write.length,empty_response,0)) {
            //Thread.sleep(100);
            Log.i("LICH", " write failed");
        }
    }

    private byte read_request(TwiMaster twi, short reg) throws ConnectionLostException,InterruptedException{
        byte[] read = new byte[] {(byte)(reg >> 8),(byte)reg}; // read 1 byte data from 2 bytes address of register
        byte[] response = new byte[1];
        if (!twi.writeRead(VL6180X_addr, false, read, read.length,response,1)) {
            //Thread.sleep(100);
            Log.i("LICH", " read failed");
            return 0;
        }
        else return response[0];
    }

}
