import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

class Restaurante {
	private int id;
	private String nome;
	private String cidade;
	private int capacidade;
	private double avaliacao;
	private String[] tiposCozinha;
	private int faixaPreco;
	private Hora horarioAbertura;
	private Hora horarioFechamento;
	private Data dataAbertura;
	private boolean aberto;

	public Restaurante(int id, String nome, String cidade, int capacidade, double avaliacao, String[] tiposCozinha, int faixaPreco, Hora horarioAbertura, Hora horarioFechamento, Data dataAbertura,
					   boolean aberto)
	{
		this.id = id;
		this.nome = nome;
		this.cidade = cidade;
		this.capacidade = capacidade;
		this.avaliacao = avaliacao;
		this.tiposCozinha = tiposCozinha;
		this.faixaPreco = faixaPreco;
		this.horarioAbertura = horarioAbertura;
		this.horarioFechamento = horarioFechamento;
		this.dataAbertura = dataAbertura;
		this.aberto = aberto;
	}

	public int getId()
	{
		return this.id;
	}

	public static Restaurante parseRestaurante(String s)
	{
		try (Scanner sc = new Scanner(s)) {
			sc.useDelimiter(",");

			int id = sc.nextInt();
			String nome = sc.next();
			String cidade = sc.next();
			int capacidade = sc.nextInt();
			double avaliacao = sc.nextDouble();
			String[] tiposCozinha = parseTiposCozinha(sc.next());
			int faixaPreco = sc.next().length();

			
			sc.useDelimiter("-");
			Hora horarioAbertura = Hora.parseHora(sc.next());
			sc.useDelimiter(",");
			Hora horarioFechamento = Hora.parseHora(sc.next());

			Data dataAbertura = Data.parseData(sc.next());
			boolean aberto = sc.next().equals("true");

			return new Restaurante(id, nome, cidade, capacidade, avaliacao, tiposCozinha, faixaPreco, horarioAbertura, horarioFechamento, dataAbertura, aberto);
		}
	}

	private static String[] parseTiposCozinha(String s)
	{
		
		int count = 1;
		for (int i = 0; i < s.length(); ++i)
			if (s.charAt(i) == ';')
				++count;

		String[] tipos = new String[count];
		try (Scanner sc = new Scanner(s)) {
			sc.useDelimiter(";");
			int i = 0;
			while (sc.hasNext())
				tipos[i++] = sc.next();
		}
		return tipos;
	}

	public String formatar()
	{
		String tiposFormatados = "";
		for (int i = 0; i < tiposCozinha.length; i++) {
			tiposFormatados += tiposCozinha[i];
			if (i < tiposCozinha.length - 1)
				tiposFormatados += ",";
		}

		String precoStr = "";
		for (int i = 0; i < faixaPreco; i++)
			precoStr += "$";

		return "[" + id + " ## " + nome + " ## " + cidade + " ## " + capacidade + " ## " + avaliacao + " ## [" + tiposFormatados + "] ## " + precoStr + " ## " + horarioAbertura.formatar() + "-" +
			horarioFechamento.formatar() + " ## " + dataAbertura.formatar() + " ## " + aberto + "]";
	}
}

class ColecaoRestaurantes {
	private Restaurante[] restaurantes;
	private int tamanho;

	public void lerCsv(String path) throws IOException
	{
		restaurantes = new Restaurante[8192];
		tamanho = 0;

		try (Scanner sc = new Scanner(new File(path))) {
			sc.nextLine();
			while (sc.hasNextLine())
				restaurantes[tamanho++] = Restaurante.parseRestaurante(sc.nextLine());
		}
	}

	public int getTamanho()
	{
		return tamanho;
	}

	public Restaurante[] getRestaurantes()
	{
		return restaurantes;
	}
}

class Data {
	private int dia;
	private int mes;
	private int ano;

	public Data(int dia, int mes, int ano)
	{
		this.dia = dia;
		this.mes = mes;
		this.ano = ano;
	}

	public static Data parseData(String s)
	{
		Scanner sc = new Scanner(s);
		sc.useDelimiter("-");
		int ano = sc.nextInt();
		int mes = sc.nextInt();
		int dia = sc.nextInt();
		sc.close();
		return new Data(dia, mes, ano);
	}

	public String formatar()
	{
		String d = (dia < 10 ? "0" : "") + dia;
		String m = (mes < 10 ? "0" : "") + mes;
		return d + "/" + m + "/" + ano;
	}

	public void setAno(int ano)
	{
		this.ano = ano;
	}
	public int getAno()
	{
		return this.ano;
	}
	public void setMes(int mes)
	{
		this.mes = mes;
	}
	public int getMes()
	{
		return this.mes;
	}
	public void setDia(int dia)
	{
		this.dia = dia;
	}
	public int getDia()
	{
		return this.dia;
	}
}

class Hora {
	private int hora;
	private int minuto;

	public Hora(int hora, int minuto)
	{
		this.hora = hora;
		this.minuto = minuto;
	}

	public static Hora parseHora(String s)
	{
		Scanner sc = new Scanner(s);
		sc.useDelimiter("[^0-9]+");
		int hora = sc.nextInt();
		int minuto = sc.nextInt();
		sc.close();
		return new Hora(hora, minuto);
	}

	public String formatar()
	{
		String h = (hora < 10 ? "0" : "") + hora;
		String m = (minuto < 10 ? "0" : "") + minuto;
		return h + ":" + m;
	}

	public int getHora()
	{
		return this.hora;
	}
	public int getMinuto()
	{
		return this.minuto;
	}
	public void setHora(int hora)
	{
		this.hora = hora;
	}
	public void setMinuto(int minuto)
	{
		this.minuto = minuto;
	}
}

public class Modelagem {
	public static void main(String[] args) throws IOException
	{
		ColecaoRestaurantes colecao = new ColecaoRestaurantes();
		colecao.lerCsv("/tmp/restaurantes.csv");

		Scanner sc = new Scanner(System.in);
		while (sc.hasNext()) {
			int idBuscado = sc.nextInt();

			Restaurante[] restaurantes = colecao.getRestaurantes();
			for (int i = 0; i < colecao.getTamanho(); i++) {
				if (restaurantes[i].getId() == idBuscado) {
					System.out.println(restaurantes[i].formatar());
					i = 999999999;
				}
			}
		}
		sc.close();
	}
}
