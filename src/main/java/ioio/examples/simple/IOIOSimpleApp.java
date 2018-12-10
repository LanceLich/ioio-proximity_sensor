package ioio.examples.simple;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;

public class IOIOSimpleApp extends IOIOActivity {
	private TextView textView_1;
    private TextView textView_2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textView_1 = (TextView) findViewById(R.id.TextView1);
        textView_2 = (TextView) findViewById(R.id.TextView2);
        //textView_3 = (TextView) findViewById(R.id.TextView3);
        //textView_4 = (TextView) findViewById(R.id.TextView4);
	}

	class Looper extends BaseIOIOLooper {
		//private AnalogInput input_;
		private DigitalOutput led_;
		private TwiMaster twi_; // set up I2C


		@Override
		public void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			//input_ = ioio_.openAnalogInput(40);
			twi_ = ioio_.openTwiMaster(1, TwiMaster.Rate.RATE_400KHz, false);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
 //code for VL6180X proximity sensor, have not finished yet.

            byte[] write_request = new byte[] {0x18, 0x01};
			byte[] empty_response = new byte[] {};
			byte[] read_request = new byte[] {0x62};
			byte[] check_request = new byte[] {0x4f};
			byte[] check_status = new byte[2];
			byte[] read_range = new byte[2];
            byte[] clear_request = new byte[] {0x15, 0x07};

			if (twi_.writeRead(0x29,false,write_request,write_request.length,empty_response,empty_response.length)) {// write 0x01 to register 0x18, a single-shot range measurement is performed
				Thread.sleep(100);
				if (twi_.writeRead(0x29, false, check_request, check_request.length, check_status, check_status.length)) { // read status of range measurement from register 0x4f,
					Thread.sleep(100);
					setNumber2(check_status[0]);
					if (twi_.writeRead(0x29, false, read_request, read_request.length, read_range, read_range.length)) { //read range result from register 0x62
						Log.i("LICH", "success");
						setNumber1(read_range[1]); // print the result
						led_.write(false);
						Thread.sleep(1000);
						twi_.writeRead(0x29, false, clear_request, clear_request.length, empty_response, empty_response.length); //clear the interrupt status
					}else Log.i("LICH", " read failed");
				}else Log.i("LICH", " check failed");
			}else Log.i("LICH", " write failed");
            //twi_.writeRead(0x29, false, clear_request, clear_request.length, empty_response, empty_response.length);
            //setNumber(input_.read());
            led_.write(true);
            //setNumber2(0);
			//setNumber1(0);
            Log.i("LICH","nextLoop");

            //setNumber1(input_.read()); //print the range result
            //led_.write(false); //turn on status led
			Thread.sleep(1000);
		}

	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void setNumber1(float f) {
		final String str = String.format("%.2f",f);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_1.setText(str);
			}
		});
	}

    private void setNumber2(float f) {
        final String str = String.format("%.2f",f);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_2.setText(str);
            }
        });
    }


}
