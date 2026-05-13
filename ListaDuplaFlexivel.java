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
	private CelulaDupla primeiro;
	private CelulaDupla ultimo;
	private int tamanho;

	ListaDupla() {
		primeiro = new CelulaDupla(null);
		ultimo = primeiro;
		tamanho = 0;
	}

	void inserirInicio(Restaurante r) {
		CelulaDupla tmp = new CelulaDupla(r);
		tmp.ant = primeiro;
		tmp.prox = primeiro.prox;
		primeiro.prox = tmp;
		if (primeiro == ultimo) ultimo = tmp;
		else tmp.prox.ant = tmp;
		tamanho++;
	}

	void inserirFim(Restaurante r) {
		CelulaDupla tmp = new CelulaDupla(r);
		tmp.ant = ultimo;
		ultimo.prox = tmp;
		ultimo = tmp;
		tamanho++;
	}

	void inserir(Restaurante r, int pos) {
		if (pos < 0 || pos > tamanho) return;
		if (pos == 0) { inserirInicio(r); return; }
		if (pos == tamanho) { inserirFim(r); return; }
		CelulaDupla ant = primeiro;
		for (int i = 0; i < pos; i++) ant = ant.prox;
		CelulaDupla tmp = new CelulaDupla(r);
		tmp.ant = ant;
		tmp.prox = ant.prox;
		ant.prox = tmp;
		tmp.prox.ant = tmp;
		tamanho++;
	}

	Restaurante removerInicio() {
		CelulaDupla tmp = primeiro.prox;
		Restaurante r = tmp.restaurante;
		primeiro.prox = tmp.prox;
		if (tmp == ultimo) ultimo = primeiro;
		else tmp.prox.ant = primeiro;
		tamanho--;
		return r;
	}

	Restaurante removerFim() {
		CelulaDupla tmp = ultimo;
		ultimo = ultimo.ant;
		ultimo.prox = null;
		tamanho--;
		return tmp.restaurante;
	}

	Restaurante remover(int pos) {
		if (pos < 0 || pos >= tamanho) return null;
		if (pos == 0) return removerInicio();
		if (pos == tamanho - 1) return removerFim();
		CelulaDupla tmp = primeiro.prox;
		for (int i = 0; i < pos; i++) tmp = tmp.prox;
		tmp.ant.prox = tmp.prox;
		tmp.prox.ant = tmp.ant;
		tamanho--;
		return tmp.restaurante;
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

public class ListaDuplaFlexivel {
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

		int qtd = sc.nextInt();

		double inicio = System.nanoTime();

		for (int i = 0; i < qtd; i++) {
			String cmd = sc.next();
			if (cmd.charAt(0) == 'I') {
				if (cmd.length() == 2 && cmd.charAt(1) == 'I') {
					int idCmd = sc.nextInt();
					Restaurante r = colecao.buscarPorId(idCmd);
					if (r != null) { lista.inserirInicio(r); ++movimentacoes; }
				} else if (cmd.length() == 2 && cmd.charAt(1) == 'F') {
					int idCmd = sc.nextInt();
					Restaurante r = colecao.buscarPorId(idCmd);
					if (r != null) { lista.inserirFim(r); ++movimentacoes; }
				} else if (cmd.charAt(1) == '*') {
					int pos = sc.nextInt();
					int idCmd = sc.nextInt();
					Restaurante r = colecao.buscarPorId(idCmd);
					if (r != null) { lista.inserir(r, pos); ++movimentacoes; }
				}
			} else if (cmd.charAt(0) == 'R') {
				if (cmd.length() == 2 && cmd.charAt(1) == 'I') {
					Restaurante r = lista.removerInicio();
					System.out.println("(R)" + r.getNome());
					++movimentacoes;
				} else if (cmd.length() == 2 && cmd.charAt(1) == 'F') {
					Restaurante r = lista.removerFim();
					System.out.println("(R)" + r.getNome());
					++movimentacoes;
				} else if (cmd.charAt(1) == '*') {
					int pos = sc.nextInt();
					Restaurante r = lista.remover(pos);
					System.out.println("(R)" + r.getNome());
					++movimentacoes;
				}
			}
		}

		double tempoExecucaoMs = (System.nanoTime() - inicio) / 1_000_000.0;

		try (FileWriter writer = new FileWriter("859563_listaDuplaFlexivel.txt")) {
			writer.write(String.format("859563\t%d\t%g", comparacoes, tempoExecucaoMs));
		}

		lista.mostrar();
	}
}
