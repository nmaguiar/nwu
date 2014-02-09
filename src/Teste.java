import java.io.IOException;
import java.util.Properties;

import com.nwu.httpd.HTTPd;
import com.nwu.httpd.responses.EchoResponse;
import com.nwu.httpd.responses.FileResponse;
import com.nwu.httpd.responses.StatusResponse;
import com.nwu.log.Log;

public class Teste {
	
	public static void main(String args[]) {
		try {
			Properties fileProps = new Properties();
			fileProps.put("publichtml", "./html");
			
			Properties testFileProps = new Properties();
			testFileProps.put("publichtml", "d://a");
			
			HTTPd.setDefaultResponse("/");
			
			// Register responses
			HTTPd.registerURIResponse("/Echo", EchoResponse.class, null);
			HTTPd.registerURIResponse("/Status", StatusResponse.class, null);
			HTTPd.registerURIResponse("/abc", FileResponse.class, fileProps);
			HTTPd.registerURIResponse("/", FileResponse.class, fileProps);
			//HTTPd.registerURIResponse("/aguiar", FileResponse.class, testFileProps);
			//HTTPd.registerURIResponse("/php", JavaServletResponse.class, null);
			
			// Start server
			HTTPd httpd = new HTTPd(new Log("httpd_17878", Log.Type.DEBUG), 17878);

			while(true) {
				Thread.sleep(1000);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
