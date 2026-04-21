public class TesteData{
	public static void main (String[] args){
		Data data = Data.parseData("2001-08-03"); 
		System.out.println(data.formatar());
	}
}
