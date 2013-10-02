import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpiderSearch {
	private final String CRLF = "\r\n";
	private HashMap<String,Link> hostsVisitados;
	private Pattern patternUrlBase;
	private Pattern patternUrl;
	private Pattern patternHref;
	private Pattern patternResponseHttp;
	private Link linkBase;
	
	public SpiderSearch() {
		hostsVisitados = new HashMap<String,Link>();
		patternUrl = Pattern.compile("http://(.*?)(/.*/$|/$|/.*?\\..*?$|/.*|$)");
		patternUrlBase = Pattern.compile("http://(.*?)(/.*/$|/$)");
		patternHref = Pattern.compile("href=\"(.*?)\"");
		patternResponseHttp = Pattern.compile("HTTP/[0-9]\\.[0-9] ([0-9]{3}) ");
	}
	
	public void validarLink(Link link){
		Matcher matcherUrl = patternUrl.matcher(link.getUrl());
		if (matcherUrl.find()) {
			link.setHost(matcherUrl.group(1).toString());
			link.setPath(matcherUrl.group(2).toString().equals("") ? "/" : matcherUrl.group(2).toString());
		} else {
			System.out.println("URL com formato incorreto: " + link.getUrl());
			return;
		}
		search(link);
	}
	
	public void searchLink(String url) {
		linkBase = new Link();
		linkBase.setUrl(url);
		linkBase.setPai("sitebase");
		linkBase.setLinha(0);
		linkBase.setTipoRequisicao("GET");
		Matcher matcherUrlBase = patternUrlBase.matcher(linkBase.getUrl());
		if (matcherUrlBase.find()) {
			linkBase.setHost(matcherUrlBase.group(1).toString());
			linkBase.setPath(matcherUrlBase.group(2).toString());
		}else {
			System.out.println("URL base com formato incorreto!");
			return;
		}
		search(linkBase);
	}
	
	public void search (Link link) {
		Socket sc;
		try {
			if (hostsVisitados.containsKey(link.getUrl())){
				return;
			}
//			System.out.println(link.getUrl());
			boolean isHtml = false;
			hostsVisitados.put(link.getUrl(), link);
			
			sc = new Socket();
			sc.connect(new InetSocketAddress(link.getHost(), 80), 0);
			sc.setSoTimeout(5000);
			OutputStream os = sc.getOutputStream();
			InputStream is = sc.getInputStream();
			String head = link.getTipoRequisicao() + " " + link.getPath() + " HTTP/1.1" + CRLF;
			String hostHttp = "Host:" + link.getHost() + CRLF;
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
					System.out.println(link.getUrl()+" "+matcherResponseHttp.group(1).toString()+" "+link.getPai()+" "+link.getLinha());
				}
				if (resposta.contains("Content-Type: text/html")) {
					isHtml = true;
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
							if (s.startsWith(linkBase.getUrl())) {
								Link linkFilho = new Link();
								linkFilho.setUrl(s);
								linkFilho.setPai(link.getUrl());
								linkFilho.setLinha(i);
								linkFilho.setTipoRequisicao("GET");
								validarLink(linkFilho);
							} else if (s.startsWith("/")) {
								Pattern patternLink = Pattern.compile("(http://.*/)");
								Matcher matcherLink = patternLink.matcher(link.getUrl());
								matcherLink.find();
								Link linkFilho = new Link();
								linkFilho.setUrl(linkBase.getUrl() + s.substring(1, s.length()));
								linkFilho.setPai(link.getUrl());
								linkFilho.setLinha(i);
								linkFilho.setTipoRequisicao("GET");
								validarLink(linkFilho);
							} else if (s.startsWith("./")) {
								Pattern patternLink = Pattern.compile("(http://.*/)");
								Matcher matcherLink = patternLink.matcher(link.getUrl());
								matcherLink.find();
								Link linkFilho = new Link();
								linkFilho.setUrl(matcherLink.group(1).toString()+s.substring(2, s.length()));
								linkFilho.setPai(link.getUrl());
								linkFilho.setLinha(i);
								linkFilho.setTipoRequisicao("GET");
								validarLink(linkFilho);
							} else if (s.startsWith("../")) {
								Pattern patternLink = Pattern.compile("(http://.*/).*?/");
								Matcher matcherLink = patternLink.matcher(link.getUrl());
								matcherLink.find();
								Link linkFilho = new Link();
								linkFilho.setUrl(matcherLink.group(1).toString()+s.substring(3, s.length()));
								linkFilho.setPai(link.getUrl());
								linkFilho.setLinha(i);
								linkFilho.setTipoRequisicao("GET");
								validarLink(linkFilho);
							} else if (s.matches("[^./h#].*")) {
								Pattern patternLink = Pattern.compile("(http://.*/)");
								Matcher matcherLink = patternLink.matcher(link.getUrl());
								matcherLink.find();
								Link linkFilho = new Link();
								linkFilho.setUrl(matcherLink.group(1).toString()+s);
								linkFilho.setPai(link.getUrl());
								linkFilho.setLinha(i);
								linkFilho.setTipoRequisicao("GET");
								validarLink(linkFilho); 
							} else if(s.startsWith("#")) {
								return;
							} else {
								Link linkFilho = new Link();
								linkFilho.setUrl(s);
								linkFilho.setPai(link.getUrl());
								linkFilho.setLinha(i);
								linkFilho.setTipoRequisicao("HEAD");
								validarLink(linkFilho);
							}
						} 
					}
					i++;
				}
				sc.close();
			} else {
				sc.close();
			}
			sc.close();
		} catch (UnknownHostException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}