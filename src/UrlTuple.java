import java.util.HashMap;

public class UrlTuple {
    private int linksCrawled = 0;
    private int emailsCrawled = 0;
    private int proxiesCrawled = 0;
    private int telnrCrawled = 0;
    private String url = "";
    private String origin;
    // Constructor
    public UrlTuple() {
    }


    /*
     * Getters & Setters
     */


    public int getLinksCrawled() {
        return linksCrawled;
    }

    public void setLinksCrawled(int linksCrawled) {
        this.linksCrawled = linksCrawled;
    }
    public int getEmailsCrawled() {
        return emailsCrawled;
    }

    public void setEmailsCrawled(int emailsCrawled) {
        this.emailsCrawled = emailsCrawled;
    }
    public int getProxiesCrawled(){
    	return this.proxiesCrawled;
    }
    public void setProxiesCrawled(int proxiesCrawled){
    	this.proxiesCrawled = proxiesCrawled;
    }
    public int getTelnrCrawled(){
    	return this.telnrCrawled;
    }
    public void setTelnrCrawled(int telnrCrawled){
    	this.telnrCrawled = telnrCrawled;
    }
    public String getOrigin(){
    	return this.origin;
    }
    public void setOrigin(String origin){
    	this.origin = origin;
    }
    public void setVal(String url) {
        this.url = url;
    }
    public String getVal() {
        return this.url;
    }
    public static UrlTuple hashmapByIndex(int index, HashMap<String, UrlTuple> urls){
		int iterate = 0;
		for(String url : urls.keySet()){
			if(iterate == index){
				return urls.get(url);
			}
			iterate++;
		}
		return null;
	}
}