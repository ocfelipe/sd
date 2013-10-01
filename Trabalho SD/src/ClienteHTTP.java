import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Cliente HTTP m�nimo.
 * @author Tarcisio
 */
public class ClienteHTTP {

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Socket sc = new Socket("computer.howstuffworks.com", 80);
        OutputStream os = sc.getOutputStream();
        InputStream is = sc.getInputStream();
                
        final String CRLF = "\r\n";
        
		//----------- Envio da requisi��o HTTP
        
	    String get = "GET /c.htm/printable HTTP/1.1" + CRLF;
	    String host = "Host:computer.howstuffworks.com" + CRLF;
	    String msg = get + host + CRLF;
	        
	    os.write(msg.getBytes());
	        
	    //----------  Recebimento da resposta HTTP
	    
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
			
		// Cabe�alho da Resposta HTTP
		while ((line= reader.readLine()).length()!=0){
			System.out.println(line);
		}
		System.out.println();
		int i =1;
		// Conte�do da Resposta HTTP
		while ((line= reader.readLine())!=null){
			System.out.println(i+" "+line);
			i++;			
		}
		System.out.println(" : Ultima Linha");
		
		sc.close();
	}
}