import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Cliente HTTP mínimo.
 * @author Tarcisio
 */
public class ClienteHTTP {

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Socket sc = new Socket("www.ufs.br", 80);
        OutputStream os = sc.getOutputStream();
        InputStream is = sc.getInputStream();
                
        final String CRLF = "\r\n";
        
		//----------- Envio da requisição HTTP
        
	    String get = "GET / HTTP/1.1" + CRLF;
	    String host = "Host:www.ufs.br" + CRLF;
	    String msg = get + host + CRLF;
	        
	    os.write(msg.getBytes());
	        
	    //----------  Recebimento da resposta HTTP
	    
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
			
		// Cabeçalho da Resposta HTTP
		while ((line= reader.readLine()).length()!=0){
			System.out.println(line);
		}
		System.out.println();
		int i =1;
		// Conteúdo da Resposta HTTP
		while ((line= reader.readLine())!=null){
			System.out.println(i+" "+line);
			i++;
		}
		sc.close();
	}
}