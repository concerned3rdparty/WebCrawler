import java.io.PrintWriter;


public class test
{
	public static void main(String[] args){
	try{
		PrintWriter w = new PrintWriter("tesetFile.txt", "UTF-8");
		w.println("Hello");
		w.close();
		System.out.println("Success");
	}
	catch(Exception c){
		//ignore
	}
	}
}