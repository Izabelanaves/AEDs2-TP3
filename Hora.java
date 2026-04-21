import java.util.Scanner;
public class Hora{
	private int hora;
	private int minuto;
	public Hora(int hora, int minuto){
		this.hora = hora;
		this.minuto = minuto;
	}
	public static Hora parseHora (String s){
		Scanner sc = new Scanner(s);
		sc. useDelimiter(":");
		int hora = sc.nextInt();
		int minuto = sc.nextInt();
		sc.close();
		return new Hora(hora, minuto);
	}
	public String formatar(){
		return String.format("%02d:%02d", hora, minuto);
	}		

}
