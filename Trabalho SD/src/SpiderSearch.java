import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpiderSearch {
	private final String CRLF = "\r\n";
	private ArrayList<Link> hostsVisitados;
	private Pattern patternUrl;
	private Pattern patternHref;
	private Pattern patternResponseHttp;
	
	public SpiderSearch() {
		hostsVisitados = new ArrayList<Link>();
		patternUrl = Pattern.compile("http://(.*?)(/.*/$|/$)");
		patternHref = Pattern.compile("href=\"(.*?)\"");
		patternResponseHttp = Pattern.compile("HTTP/[0-9]\\.[0-9] ([0-9]{3}) ");
	}
	
	public void searchLink(String url) {
		Link link = new Link();
		link.setLink(url);
		link.setPai(url);
		link.setLinha(0);
		hostsVisitados.add(link);
		search(link);
	}
	
	public void search (Link link) {
		try {
			hostsVisitados.remove(link);
			String host = "";
			String path = "";
			Matcher matcherUrl = patternUrl.matcher(link.getLink());
			if (matcherUrl.find()) {
				host = matcherUrl.group(1).toString();
				path = matcherUrl.group(2).toString();
				System.out.println(host);
				System.out.println(path);
			} else {
				System.out.println("URL com formato incorreto!");
				return;
			}
			
			Socket sc = new Socket(host, 80);
			OutputStream os = sc.getOutputStream();
			InputStream is = sc.getInputStream();
			String head = "HEAD " + path + " HTTP/1.1" + CRLF;
			String hostHttp = "Host:" + host + CRLF;
			String msg = head + hostHttp + CRLF;
			
			os.write(msg.getBytes());
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String resposta;
			
			int contador = 0;
			while ((resposta = reader.readLine()).length()!=0){
				if (!resposta.contains("200 OK") && contador == 0) {
					Matcher matcherResponseHttp = patternResponseHttp.matcher(resposta);
					matcherResponseHttp.find();
					System.out.println(link.getLink()+" "+matcherResponseHttp.group(1).toString()+" "+link.getPai()+" "+link.getLinha());
				}
				if (resposta.contains("Content-Type: text/html")) {
					System.out.println("É html");
				}
				contador++;
			}
			int i = 1;
			
			while ((resposta= reader.readLine())!=null){			
				Matcher matcher = patternHref.matcher(resposta);
				while (matcher.find()) {
					MatchResult matchResult = matcher.toMatchResult();
					String s = matchResult.group(1).toString();
					if (!s.matches("javascript:.*|mailto:.*|https:.*|ftp:.*|file:.*")) {
						System.out.println(i+" "+ s);						
					} 
				}
				i++;
			}
			sc.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
