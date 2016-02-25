import java.io.*;
import java.net.*;
import java.util.*;

public class ImageSocketFetch{

	public static void main(String args[]) throws Exception{
		if(args.length < 1){
			System.out.println("Expected Command Line arguments");
		}
		ImageSocketFetch obj = new ImageSocketFetch();
		String hostName = obj.getHostName(args[0]);
		String pagename = obj.getPageName(args[0]);
		System.out.println(hostName);
		System.out.println(pagename);
		String filename = args[0].substring( args[0].lastIndexOf('/')+1, args[0].length() );
		System.out.println(filename);
		String imagename = obj.getHTMLPage(hostName,pagename,filename);
		if (imagename.length() ==0){
			System.out.println("Couldn't find an image on this page");
		}
		String imagePath = pagename.substring(0, pagename.lastIndexOf('/')+1) + imagename;
		System.out.println("HTML Page Fetched");
		System.out.println(imagename);
		System.out.println(imagePath);
		obj.getImage(hostName,imagePath,imagename);
		System.out.println("Done doing everything");
	}
	public String getHostName(String url) throws URISyntaxException{
		URI uri = new URI(url);
		return uri.getHost();
	}
	public String getPageName(String url) throws URISyntaxException{
		URI uri = new URI(url);
		return uri.getPath();
	}
	public void getImage(String host, String imageurl,String filename) throws Exception {
		Socket httpSocket = new Socket(host, 80);
		PrintWriter writer = new PrintWriter(httpSocket.getOutputStream());
		FileOutputStream foutStream = new FileOutputStream(filename);
		String request ="GET " + imageurl +"  HTTP/1.0";
		
		writer.println(request);
		writer.println();
		writer.flush();
		
		InputStream inputStream = httpSocket.getInputStream();
		int count,offset;
		int total_bytes=0;
		byte[] buffer = new byte[2048];
		boolean eohFound = false;
		
		while ((count = inputStream.read(buffer)) != -1){
			total_bytes= total_bytes+count;
			offset = 0;
			if(!eohFound){
        			String string = new String(buffer, 0, count);
        			int indexOfEOH = string.indexOf("\r\n\r\n");
        			if(indexOfEOH != -1) {
            				count = count-indexOfEOH-4;
            				offset = indexOfEOH+4;
            				eohFound = true;
        			} else {
            				count = 0;
       				}
    			}
    			foutStream.write(buffer, offset, count);
  			foutStream.flush();
		}
		
		System.out.println("Read an image of size "+ total_bytes);
		httpSocket.close();
		writer.close();
		foutStream.close(); 
	}
	public String getHTMLPage(String host,String pagename,String filename) throws Exception{
	
		
		Socket clientSocket = new Socket(host,80 );
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))); 
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
		FileWriter fw = new FileWriter(filename);
		
	
		String request ="GET " + pagename+"  HTTP/1.0";
		out.println(request);
		out.println();
		out.flush();
		String inputLine; 
		boolean eoh =false;
		String imagename= "";
		boolean imageFound=false;
		while ((inputLine = in.readLine()) != null) { 
			if(!eoh){
				if(inputLine.length() ==0)
					eoh = true;
				continue;
			}
			fw.write(inputLine);
			fw.write("\n");
		  	int imageIndex= inputLine.indexOf("src=\"");	
		  	if(imageIndex!=-1 && imageFound != true){
		  		imagename = inputLine.substring(inputLine.indexOf("src=\""));
				imagename = imagename.substring("src=\"".length());
				imagename = imagename.substring(0, imagename.indexOf("\""));
		  	}
		} 
		
		System.out.println(imagename);
		out.close();
		in.close();
		fw.close();
		clientSocket.close();
		return imagename;
	}
}
