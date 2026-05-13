import java.io.File;
import java.io.FileWriter; 
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

	public String getNome()
	{
		return this.nome;
	}

	public String getCidade()
	{
		return this.cidade;
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

		return String.format("[%d ## %s ## %s ## %d ## %s ## [%s] ## %s ## %s-%s ## %s ## %b]", id, nome, cidade, capacidade, String.valueOf(avaliacao), tiposFormatados, precoStr, horarioAbertura.formatar(), horarioFechamento.formatar(), dataAbertura.formatar(), aberto);
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

public class OrdenacaoMergesort {
	
	static int comparacoes = 0;
	static int movimentacoes = 0;

	public static void main(String[] args) throws IOException
	{
		ColecaoRestaurantes colecao = new ColecaoRestaurantes();
		colecao.lerCsv("/tmp/restaurantes.csv");

		Scanner sc = new Scanner(System.in);
		Restaurante[] arrayBusca = new Restaurante[8192];
		int n = 0;

		while (sc.hasNext()) {
			String entrada = sc.next();
			if (entrada.compareTo("-1") != 0) {
				int idBuscado = Integer.parseInt(entrada);
				Restaurante[] todos = colecao.getRestaurantes();

				for (int i = 0; i < colecao.getTamanho(); i++) {
					if (todos[i].getId() == idBuscado) {
						arrayBusca[n++] = todos[i];
						
						i = colecao.getTamanho();
					}
				}
			}
		}
		sc.close();

		
		double inicio = System.nanoTime();
		mergesort(arrayBusca, n); 
		double tempoExecucaoMs = (System.nanoTime() - inicio) / 1_000_000.0;

		
		try (FileWriter writer = new FileWriter("859563_mergesort.txt")) {
			writer.write(String.format("859563\t%d\t%d\t%g", comparacoes, movimentacoes, tempoExecucaoMs));
		}

		for (int i = 0; i < n; i++)
			System.out.println(arrayBusca[i].formatar());
	}

	static void merge(Restaurante[] vec, int esq, int meio, int dir)
	{
		int n1 = meio - esq, n2 = dir - meio;
		Restaurante[] L = new Restaurante[n1];
		Restaurante[] R = new Restaurante[n2];

		for (int i = 0; i < n1; i++) { L[i] = vec[esq + i]; ++OrdenacaoMergesort.movimentacoes; }
		for (int i = 0; i < n2; i++) { R[i] = vec[meio + i]; ++OrdenacaoMergesort.movimentacoes; }

		int i = 0, j = 0, k = esq;
		while (i < n1 && j < n2) {
			++OrdenacaoMergesort.comparacoes; 
			int cmp = L[i].getCidade().compareTo(R[j].getCidade());
			if (cmp == 0) cmp = L[i].getNome().compareTo(R[j].getNome());
			if (cmp <= 0) { vec[k++] = L[i++]; }
			else          { vec[k++] = R[j++]; }
			++OrdenacaoMergesort.movimentacoes;
		}
		while (i < n1) { vec[k++] = L[i++]; ++OrdenacaoMergesort.movimentacoes; }
		while (j < n2) { vec[k++] = R[j++]; ++OrdenacaoMergesort.movimentacoes; }
	}

	static void mergesort(Restaurante[] vec, int n)
	{
		for (int tam = 1; tam < n; tam *= 2) {
			for (int esq = 0; esq < n - tam; esq += 2 * tam) {
				int meio = esq + tam;
				int dir  = Math.min(esq + 2 * tam, n);
				merge(vec, esq, meio, dir);
			}
		}
	}
}
