import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

class CelulaDupla {
	Restaurante restaurante;
	CelulaDupla ant;
	CelulaDupla prox;

	CelulaDupla(Restaurante r) {
		this.restaurante = r;
		this.ant = null;
		this.prox = null;
	}
}

class ListaDupla {
	CelulaDupla primeiro;
	CelulaDupla ultimo;
	int tamanho;

	ListaDupla() {
		primeiro = new CelulaDupla(null);
		ultimo = primeiro;
		tamanho = 0;
	}

	void inserirFim(Restaurante r) {
		CelulaDupla tmp = new CelulaDupla(r);
		tmp.ant = ultimo;
		ultimo.prox = tmp;
		ultimo = tmp;
		tamanho++;
	}

	void mostrar() {
		for (CelulaDupla i = primeiro.prox; i != null; i = i.prox)
			System.out.println(i.restaurante.formatar());
	}
}

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

	public String getNome()
	{
		return this.nome;
	}

	public double getAvaliacao()
	{
		return this.avaliacao;
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

			boolean aberto = (sc.next().compareTo("true") == 0);

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

		return String.format("[%d ## %s ## %s ## %d ## %s ## [%s] ## %s ## %s-%s ## %s ## %b]", id, nome, cidade, capacidade, String.valueOf(avaliacao), tiposFormatados, precoStr,
							 horarioAbertura.formatar(), horarioFechamento.formatar(), dataAbertura.formatar(), aberto);
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
			while (sc.hasNextLine()) {
				String linha = sc.nextLine();
				if (linha.length() > 0) {
					restaurantes[tamanho++] = Restaurante.parseRestaurante(linha);
				}
			}
		}
	}

	public int getTamanho()
	{
		return tamanho;
	}

	public Restaurante buscarPorId(int id)
	{
		for (int i = 0; i < tamanho; i++) {
			if (restaurantes[i].getId() == id)
				return restaurantes[i];
		}
		return null;
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
		return String.format("%02d/%02d/%d", dia, mes, ano);
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
		return String.format("%02d:%02d", hora, minuto);
	}
}

public class OrdenacaoQuicksortListaDuplaFlexivel {
	static int comparacoes = 0;
	static int movimentacoes = 0;

	public static void main(String[] args) throws IOException
	{
		ColecaoRestaurantes colecao = new ColecaoRestaurantes();
		colecao.lerCsv("/tmp/restaurantes.csv");

		Scanner sc = new Scanner(System.in);
		ListaDupla lista = new ListaDupla();

		while (sc.hasNextInt()) {
			int idBuscado = sc.nextInt();
			if (idBuscado == -1) break;
			Restaurante r = colecao.buscarPorId(idBuscado);
			if (r != null) lista.inserirFim(r);
		}

		double inicio = System.nanoTime();

		quicksort(lista.primeiro.prox, lista.ultimo);

		double tempoExecucaoMs = (System.nanoTime() - inicio) / 1_000_000.0;

		try (FileWriter writer = new FileWriter("859563_quicksortListaDuplaFlexivel.txt")) {
			writer.write(String.format("859563\t%d\t%d\t%g", comparacoes, movimentacoes, tempoExecucaoMs));
		}

		lista.mostrar();
	}

	static int compara(Restaurante a, Restaurante b)
	{
		if (a.getAvaliacao() > b.getAvaliacao())
			return 1;
		if (a.getAvaliacao() < b.getAvaliacao())
			return -1;
		return a.getNome().compareTo(b.getNome());
	}

	static void quicksort(CelulaDupla esq, CelulaDupla dir)
	{
		if (esq == null || dir == null || esq == dir || esq.ant == dir)
			return;

		CelulaDupla i = esq, j = dir;
		Restaurante pivo = esq.restaurante;

		while (i != j) {
			while (i != j && ++comparacoes > 0 && compara(j.restaurante, pivo) > 0)
				j = j.ant;
			while (i != j && ++comparacoes > 0 && compara(i.restaurante, pivo) <= 0)
				i = i.prox;
			if (i != j) {
				Restaurante tmp = i.restaurante;
				i.restaurante = j.restaurante;
				j.restaurante = tmp;
				++movimentacoes;
			}
		}

		if (i != esq) {
			Restaurante tmp = esq.restaurante;
			esq.restaurante = i.restaurante;
			i.restaurante = tmp;
			++movimentacoes;
		}

		quicksort(esq, i.ant);
		quicksort(i.prox, dir);
	}
}
