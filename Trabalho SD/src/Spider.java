public class Spider {
	public static void main(String[] args) {
		SpiderSearch ss = new SpiderSearch();
		ss.searchLink("http://www.ufs.br/");
		//ss.searchLink("http://www.ic.unicamp.br/~beatriz/");
		ss.viewHosts();
		//ss.searchLink("http://www.sigaa.ufs.br/sigaa/public/home.jsf");
	}
}