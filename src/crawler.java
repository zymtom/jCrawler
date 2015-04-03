import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class crawler {
	private static HashMap<String, UrlTuple> urls = new HashMap<String, UrlTuple>();
	private HashMap<String, UrlTuple> emails = new HashMap<String, UrlTuple>();
	private HashMap<String, UrlTuple> proxies = new HashMap<String, UrlTuple>();
	private HashMap<String, UrlTuple> telnr = new HashMap<String, UrlTuple>();
	private boolean getEmails = false;
	private boolean baseDomainOnly = false;
	private boolean strictDomainCrawl = false;
	private String baseDomain;
	private String StartUrl;
	private String mysqluser, mysqlpassword, mysqldb, mysqlhost;
	private boolean getProxies = false;
	private boolean getTelNr = false;
	
	public crawler(){} //Constructor
	public void start() throws Exception{ //Starting
		urls.put(getStartUrl(), new UrlTuple());
		urls.get(getStartUrl()).setVal(this.StartUrl);
		urls.get(getStartUrl()).setOrigin("StartURL");
		int index = 0;
		while(index < urls.keySet().size()){
			int tries = 0;
			boolean breaks = true;
			while(true){
				try{
					if(UrlTuple.hashmapByIndex(index, urls) != null && tries != 4){
						String url = UrlTuple.hashmapByIndex(index, urls).getVal();
						Document html = getHTTP(url);
						if(html != null){
							getLinks(html, url);
							if(getEmails()){
								Matcher m = getEmails(html);
								if(m != null){
									while(m.find()) {
										String emails = m.group().replaceAll("[\\n\\r]+", "");
										if(this.emails.get(emails) == null){
											urls.get(url).setEmailsCrawled(urls.get(url).getEmailsCrawled()+1);
											this.emails.put(emails, new UrlTuple());
											this.emails.get(emails).setVal(emails);
											this.emails.get(emails).setOrigin(url);
											System.out.println(emails);
										}
							    	}
								}
							}
							if(getProxies()){
								Matcher m = getProxies(html);
								if(m != null){
									while(m.find()) {
										String proxies = m.group().replaceAll("[\\n\\r]+", "");
										if(this.proxies.get(proxies) == null && validIP(proxies)){
											this.proxies.put(proxies, new UrlTuple());
											this.proxies.get(proxies).setVal(proxies);
											this.proxies.get(proxies).setOrigin(url);
											urls.get(url).setProxiesCrawled(urls.get(url).getProxiesCrawled()+1);
											System.out.println(proxies);
										}
						    		}
								}
							}
							if(getTelNr()){
								Matcher m = getTelNr(html);
								if(m != null){
									while(m.find()){
										String telnr = m.group().replaceAll("[\\n\\r]+", "");
										if(this.telnr.get(telnr) == null){
											urls.get(url).setTelnrCrawled(urls.get(url).getTelnrCrawled()+1);
											this.telnr.put(telnr, new UrlTuple());
											this.telnr.get(telnr).setVal(telnr);
											this.telnr.get(telnr).setOrigin(url);
											System.out.println(telnr);
										}
									}
								}
							}
						}
					}
					index++;
					breaks = false;
					break;
				}catch (Exception e){
				    if(e instanceof org.jsoup.HttpStatusException){
				    	tries++;
				    } else if (e instanceof org.jsoup.UnsupportedMimeTypeException){
				    	urls.remove(UrlTuple.hashmapByIndex(index, urls).getVal());
						break;
				    } else {
				    	tries++;
				    }
				}finally{
					if(tries == 4 && breaks){
						index++;
						break;
					}
				}
				
			}
		}
		System.out.println("---\nDone with crawling.");
	}
	//HTTP
	public static Document getHTTP(String url) throws Exception{
		if(isValidURL(url)){
			Connection.Response response = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.0) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.46 Safari/536.5")
                    .timeout(100000)
                    .ignoreHttpErrors(true).execute();
			return response.parse();
		}else{
			return null;
		}
	}
	//Domain methods
	public void setStartUrl(String StartUrl){ //Set the startUrl
		this.StartUrl = StartUrl;
	}
	public String getStartUrl(){
		return this.StartUrl;
	}
	public void baseDomainOnly(boolean baseDomain){ //Set the base domain if you want it to only crawl from the base url
		this.baseDomainOnly = baseDomain;
	}
	public void setBaseDomain(String baseDomain){
		this.baseDomain = baseDomain;
	}
	public String getBaseDomain(){
		return this.baseDomain;
	}
	public boolean isBaseDomain(String Url){ //Insert more accurate code
		if(getStrictDomainCrawl()){
			if(getBaseDomain(Url) == getBaseDomain()){
				return true;
			}else{
				return false;
			}
		}else{
			if(getHost(Url).contains(getBaseDomain())){
				return true;
			}else{
				return false;
			}
		}
	}
	public void setStrictDomainCrawl(boolean strictDomainCrawl){
		this.strictDomainCrawl = strictDomainCrawl;
	}
	public boolean getStrictDomainCrawl(){
		return this.strictDomainCrawl;
	}
	public static String getHost(String url){
	    if(url == null || url.length() == 0)
	        return "";

	    int doubleslash = url.indexOf("//");
	    if(doubleslash == -1)
	        doubleslash = 0;
	    else
	        doubleslash += 2;

	    int end = url.indexOf('/', doubleslash);
	    end = end >= 0 ? end : url.length();

	    int port = url.indexOf(':', doubleslash);
	    end = (port > 0 && port < end) ? port : end;

	    return url.substring(doubleslash, end);
	}
	public static String getBaseDomain(String url) {
	    String host = getHost(url);

	    int startIndex = 0;
	    int nextIndex = host.indexOf('.');
	    int lastIndex = host.lastIndexOf('.');
	    while (nextIndex < lastIndex) {
	        startIndex = nextIndex + 1;
	        nextIndex = host.indexOf('.', startIndex);
	    }
	    if (startIndex > 0) {
	        return host.substring(startIndex);
	    } else {
	        return host;
	    }
	}
	public static boolean isValidURL(String url){
		String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(schemes);
		if (urlValidator.isValid(url)) {
		   return true;
		} else {
		   return false;
		}
	}
	//Get email methods
	public boolean getEmails(){
		return this.getEmails;
	}
	public Matcher getEmails(Document html){
		return Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(html.html());
	}
	public void setGetEmails(boolean getEmails){
		this.getEmails = getEmails;
	}
	public void setGetProxies(boolean getProxies){
		this.getProxies = getProxies;
	}
	public boolean getProxies(){
		return this.getProxies;
	}
	public Matcher getProxies(Document html){
		return Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5]:\\d{2,5})").matcher(html.html());
	}
	public void setGetTelNr(boolean getTelNr){
		this.getTelNr = getTelNr;
	}
	public boolean getTelNr(){
		return this.getTelNr;
	}
	public Matcher getTelNr(Document html){
		return Pattern.compile("\\+?(?:46)?\\s?0?\\d+-?\\ ?\\d{2,3}\\ ?\\d{2,3}\\ ?\\d{2,3}").matcher(html.html());
	}
	
	//Link methods
	public void getLinks(Document html, String origin){
		Elements links = html.select("a");
		for(Element e : links){
			String url = e.attr("href");
			if(isValidURL(url) && urls.get(url) == null){
				if(this.baseDomainOnly){
					if(isBaseDomain(url)){
						urls.put(url, new UrlTuple());
						urls.get(url).setVal(url);
						urls.get(url).setLinksCrawled(urls.get(url).getLinksCrawled()+1);
						//System.out.print(url+"\n");
					}
				}else{
					urls.put(url, new UrlTuple());
					urls.get(url).setVal(url);
					urls.get(url).setLinksCrawled(urls.get(url).getLinksCrawled()+1);
					urls.get(url).setOrigin(origin);
					//System.out.print(url+"\n");
				}
			}
		}
	}
	
	
	public void setMysql(String User, String Password, String database, String host){
		this.mysqluser = User;
		this.mysqlpassword = Password;
		this.mysqldb = database;
		this.mysqlhost = host;
	}
	public boolean exportLinksToMysql(String table, HashMap<Integer, String> columns, HashMap<Integer, HashMap<Integer, String>> values){
		int index = 0;
		while(index < values.keySet().size()){
			if(columns.size() != values.get(index).keySet().size()){
				return false;
			}
		}
		
		MySQLAccess conn = new MySQLAccess();
		conn.setConnectionsDetails(this.mysqldb, this.mysqlhost, this.mysqluser, this.mysqlpassword);
		conn.connect();
		String columnstring = "";
		for(Integer key : columns.keySet()){
			if(key == 0){
				columnstring = columns.get(key);
			}else{
				columnstring = columnstring+", "+columns.get(key);
			}
		}
		for(Integer key: values.keySet()){
			String vals = "";
			for(Integer keys : values.get(key).keySet()){
				if(key == 0){
					vals = "'"+values.get(key).get(keys)+"'";
				}else{
					vals = vals+", '"+values.get(key).get(keys)+"'";
				}
			}
			conn.queryInsert("INSERT INTO " + table + " ("+columnstring+") VALUES ('"+vals+"')");
			index++;
		}
		return true;
	}
	public boolean exportAllToXSL(String docName){
		try{
			String filename=docName;
			HSSFWorkbook workbook=new HSSFWorkbook();
			
			HSSFSheet sheet =  workbook.createSheet("urls");  
			HSSFRow rowhead=   sheet.createRow((short)0);
			rowhead.createCell(0).setCellValue("No.");
			rowhead.createCell(1).setCellValue("URL");
			rowhead.createCell(2).setCellValue("Source");
			rowhead.createCell(3).setCellValue("Emails Crawled");
			rowhead.createCell(3).setCellValue("Proxies Crawled");
			rowhead.createCell(3).setCellValue("Telephonenumbers Crawled");
			
			int index = 0;
			while(index<urls.keySet().size()){
				HSSFRow row=   sheet.createRow((short)index+1);
				String key = UrlTuple.hashmapByIndex(index, urls).getVal();
				row.createCell(0).setCellValue(index+1);
				row.createCell(1).setCellValue(key);
				row.createCell(2).setCellValue(urls.get(key).getOrigin());
				row.createCell(3).setCellValue(urls.get(key).getEmailsCrawled());
				row.createCell(4).setCellValue(urls.get(key).getProxiesCrawled());
				row.createCell(5).setCellValue(urls.get(key).getTelnrCrawled());
				index++;
			}
			if(getEmails()){
				HSSFSheet sheet1 =  workbook.createSheet("emails");  
				HSSFRow rowhead1=   sheet1.createRow((short)0);
				rowhead1.createCell(0).setCellValue("No.");
				rowhead1.createCell(1).setCellValue("Email");
				rowhead1.createCell(2).setCellValue("Source");
				index = 0;
				while(index< this.emails.keySet().size()){
					HSSFRow row =   sheet1.createRow((short)index+1);
					String key = UrlTuple.hashmapByIndex(index, emails).getVal();
					row.createCell(0).setCellValue(index+1);
					row.createCell(1).setCellValue(key);
					row.createCell(2).setCellValue(emails.get(key).getOrigin());
					index++;
				}
			}
			if(getProxies()){
				HSSFSheet sheet2 =  workbook.createSheet("proxies");  
				HSSFRow rowhead2=   sheet2.createRow((short)0);
				rowhead2.createCell(0).setCellValue("No.");
				rowhead2.createCell(1).setCellValue("Proxy");
				rowhead2.createCell(2).setCellValue("Source");
				
				index = 0;
				while(index< this.proxies.keySet().size()){
					HSSFRow row=   sheet2.createRow((short)index+1);
					String key = UrlTuple.hashmapByIndex(index, proxies).getVal();
					row.createCell(0).setCellValue(index+1);
					row.createCell(1).setCellValue(key);
					row.createCell(2).setCellValue(proxies.get(key).getOrigin());
					index++;
				}
			}
			if(getTelNr()){
				HSSFSheet sheet3 =  workbook.createSheet("Telephonenumbers");  
				HSSFRow rowhead3=   sheet3.createRow((short)0);
				rowhead3.createCell(0).setCellValue("No.");
				rowhead3.createCell(1).setCellValue("TelephoneNumbers");
				rowhead3.createCell(2).setCellValue("Source");
				index = 0;
				while(index< this.telnr.keySet().size()){
					HSSFRow row=   sheet3.createRow((short)index+1);
					String key = UrlTuple.hashmapByIndex(index, telnr).getVal();
					row.createCell(0).setCellValue(index+1);
					row.createCell(1).setCellValue(key);
					row.createCell(2).setCellValue(telnr.get(key).getOrigin());
					index++;
				}
			}
			FileOutputStream fileOut =  new FileOutputStream(filename);
			
			workbook.write(fileOut);
			fileOut.close();
			workbook.close();
			return true;
			
		} catch ( Exception ex ) {
			System.out.println(ex);
			return false;
		}
	}
	public void printToConsole(){
		for(String email:emails.keySet()){
			System.out.println(email+"|"+emails.get(email).getOrigin());
		}
		for(String email:telnr.keySet()){
			System.out.println(email);
		}
		for(String email:proxies.keySet()){
			System.out.println(email);
		}
	}
	public boolean validIP (String ip) {
		InetAddressValidator ipvalidator = new InetAddressValidator();
		if(ipvalidator.isValid(ip)){
			return true;
		}else{
			return false;
		}
	}
}

