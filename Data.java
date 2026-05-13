import java.util.Scanner;

public class Data{
	
	private int dia;
	private int mes;
	private int ano;
	
	public Data(int dia, int mes, int ano){
		
		this.dia = dia;
		this.mes = mes;
		this.ano = ano;
	}
		

	public static Data parseData (String s){
			Scanner sc = new Scanner (s);
			sc.useDelimiter("-");
			int ano = sc.nextInt();
			int mes = sc.nextInt();
			int dia = sc.nextInt();
			sc.close();
			return new Data (dia, mes, ano);	

		}
	public String formatar(){
		return String.format( "%02d/%02d/%04d", dia, mes, ano);
	}

}	
