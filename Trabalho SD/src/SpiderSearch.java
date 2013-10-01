import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpiderSearch {
	private final String CRLF = "\r\n";
	private HashMap<String,Link> hostsVisitados;
	private Pattern patternUrl;
	private Pattern patternHref;
	private Pattern patternResponseHttp;
	private Link linkBase;
	
	public SpiderSearch() {
		hostsVisitados = new HashMap<String,Link>();
		patternUrl = Pattern.compile("http://(.*?)(/.*/$|/$|/.*?\\..*?$)");
		patternHref = Pattern.compile("href=\"(.*?)\"");
		patternResponseHttp = Pattern.compile("HTTP/[0-9]\\.[0-9] ([0-9]{3}) ");
	}
	
	public void viewHosts() {
		//System.out.println("Imprimindo HashMap...............");
		for (Link link : hostsVisitados.values()) {
		    //System.out.println(link.getLink());
		}
	}
	
	public void searchLink(String url) {
		linkBase = new Link();
		linkBase.setLink(url);
		linkBase.setPai(url);
		linkBase.setLinha(0);
		search(linkBase);
	}
	
	public void search (Link link) {
		try {
			if (hostsVisitados.containsKey(link.getLink())){
				return;
			}
			System.out.println(link.getLink());
			String host = "";
			String path = "";
			boolean isHtml = false;
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
			hostsVisitados.put(link.getLink(), link);
			
			Socket sc = new Socket(host, 80);
			OutputStream os = sc.getOutputStream();
			InputStream is = sc.getInputStream();
			String head = "GET " + path + " HTTP/1.1" + CRLF;
			String hostHttp = "Host:" + host + CRLF;
			String msg = head + hostHttp + CRLF;
			
			os.write(msg.getBytes());
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String resposta;
			
			//Cabeçalho HTTP
			int contador = 0;
			while ((resposta = reader.readLine()).length()!=0){
				if (!resposta.contains("200 OK") && contador == 0) {
					Matcher matcherResponseHttp = patternResponseHttp.matcher(resposta);
					matcherResponseHttp.find();
					System.out.println(link.getLink()+" "+matcherResponseHttp.group(1).toString()+" "+link.getPai()+" "+link.getLinha());
				}
				if (resposta.contains("Content-Type: text/html")) {
					isHtml = true;
//					System.out.println("É html");
				}
				contador++;
			}
			
			//Conteúdo de Resposta HTTP
			int i = 1;
			if (isHtml) {
				while ((resposta= reader.readLine())!=null){
					Matcher matcher = patternHref.matcher(resposta);
					while (matcher.find()) {
						MatchResult matchResult = matcher.toMatchResult();
						String s = matchResult.group(1).toString();
						if (!s.matches("javascript:.*|mailto:.*|https:.*|ftp:.*|file:.*")) {
//							System.out.println(i+" "+ s);						
							if (s.startsWith(linkBase.getLink())) {
								Link linkFilho = new Link();
								linkFilho.setLink(s);
								linkFilho.setPai(link.getLink());
								linkFilho.setLinha(i);
								search(linkFilho);
							} else if (s.startsWith("./")) {
								Pattern patternLink = Pattern.compile("(http://.*/)");
								Matcher matcherLink = patternLink.matcher(link.getLink());
								matcherLink.find();
								Link linkFilho = new Link();
								linkFilho.setLink(matcherLink.group(1).toString()+s.substring(2, s.length()));
								linkFilho.setPai(link.getLink());
								linkFilho.setLinha(i);
								search(linkFilho);
							}
						} 
					}
					i++;
				}
			}
			else {
				sc.close();
			}
			sc.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
