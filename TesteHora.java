public class TesteHora{
	public static void main(String[] args){
		Hora hora = Hora.parseHora("12:07");
		System.out.println(hora.formatar());
	}
}

