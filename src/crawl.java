public class crawl {	
	public static void main(String[] args) throws Exception{
		crawler c = new crawler();
		c.setStartUrl("http://pastebin.com/raw.php?i=Vym25bxE");
		c.setGetEmails(true);
		c.setGetProxies(true);
		c.setGetTelNr(true);
		c.start();
		if(c.exportAllToXSL("ilikememes.xls")){
			System.out.println("Export was successful");
		}else{
			System.out.println("Export was not successful");
		}
		c.printToConsole();
	}
}
