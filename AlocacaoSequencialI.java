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

    public Restaurante(int id, String nome, String cidade, int capacidade, double avaliacao, String[] tiposCozinha, int faixaPreco, Hora horarioAbertura, Hora horarioFechamento, Data dataAbertura, boolean aberto) {
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

    public int getId() { return this.id; }
    public String getNome() { return this.nome; }

    public static Restaurante parseRestaurante(String s) {
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

    private static String[] parseTiposCozinha(String s) {
        int count = 1;
        for (int i = 0; i < s.length(); ++i) if (s.charAt(i) == ';') ++count;
        String[] tipos = new String[count];
        try (Scanner sc = new Scanner(s)) {
            sc.useDelimiter(";");
            int i = 0;
            while (sc.hasNext()) tipos[i++] = sc.next();
        }
        return tipos;
    }

    public String formatar() {
        String tiposFormatados = "";
        for (int i = 0; i < tiposCozinha.length; i++) {
            tiposFormatados += tiposCozinha[i];
            if (i < tiposCozinha.length - 1) tiposFormatados += ",";
        }
        String precoStr = "";
        for (int i = 0; i < faixaPreco; i++) precoStr += "$";
        return String.format("[%d ## %s ## %s ## %d ## %s ## [%s] ## %s ## %s-%s ## %s ## %b]", id, nome, cidade, capacidade, String.valueOf(avaliacao), tiposFormatados, precoStr, horarioAbertura.formatar(), horarioFechamento.formatar(), dataAbertura.formatar(), aberto);
    }
}

class ColecaoRestaurantes {
    private Restaurante[] restaurantes;
    private int tamanho;

    public void lerCsv(String path) throws IOException {
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

    public int getTamanho() { return tamanho; }
    public Restaurante[] getRestaurantes() { return restaurantes; }
}

class Data {
    private int dia, mes, ano;
    public Data(int dia, int mes, int ano) { this.dia = dia; this.mes = mes; this.ano = ano; }
    public static Data parseData(String s) {
        Scanner sc = new Scanner(s);
        sc.useDelimiter("-");
        int ano = sc.nextInt();
        int mes = sc.nextInt();
        int dia = sc.nextInt();
        sc.close();
        return new Data(dia, mes, ano);
    }
    public String formatar() { return String.format("%02d/%02d/%d", dia, mes, ano); }
}

class Hora {
    private int hora, minuto;
    public Hora(int hora, int minuto) { this.hora = hora; this.minuto = minuto; }
    public static Hora parseHora(String s) {
        Scanner sc = new Scanner(s);
        sc.useDelimiter("[^0-9]+");
        int hora = sc.nextInt();
        int minuto = sc.nextInt();
        sc.close();
        return new Hora(hora, minuto);
    }
    public String formatar() { return String.format("%02d:%02d", hora, minuto); }
}

class Lista {
    private Restaurante[] array;
    private int n;

    public Lista(int tamanho) {
        array = new Restaurante[tamanho];
        n = 0;
    }

    public void inserirInicio(Restaurante restaurante) {
        if (n >= array.length) return;
        for (int i = n; i > 0; i--) {
            array[i] = array[i - 1];
            AlocacaoSequencialI.movimentacoes++;
        }
        array[0] = restaurante;
        n++;
    }

    public void inserir(Restaurante restaurante, int posicao) {
        if (n >= array.length || posicao < 0 || posicao > n) return;
        for (int i = n; i > posicao; i--) {
            array[i] = array[i - 1];
            AlocacaoSequencialI.movimentacoes++; 
        }
        array[posicao] = restaurante;
        n++;
    }

    public void inserirFim(Restaurante restaurante) {
        if (n >= array.length) return;
        array[n++] = restaurante;
    }

    public Restaurante removerInicio() {
        if (n == 0) return null;
        Restaurante resp = array[0];
        n--;
        for (int i = 0; i < n; i++) {
            array[i] = array[i + 1];
            AlocacaoSequencialI.movimentacoes++; 
        }
        return resp;
    }

    public Restaurante remover(int posicao) {
        if (n == 0 || posicao < 0 || posicao >= n) return null;
        Restaurante resp = array[posicao];
        n--;
        for (int i = posicao; i < n; i++) {
            array[i] = array[i + 1];
            AlocacaoSequencialI.movimentacoes++; 
        }
        return resp;
    }

    public Restaurante removerFim() {
        if (n == 0) return null;
        return array[--n];
    }

    public void mostrar() {
        for (int i = 0; i < n; i++) {
            System.out.println(array[i].formatar());
        }
    }
}

public class AlocacaoSequencialI {
    public static int comparacoes = 0;
    public static int movimentacoes = 0;

    public static void main(String[] args) throws IOException {
        ColecaoRestaurantes colecao = new ColecaoRestaurantes();
        colecao.lerCsv("/tmp/restaurantes.csv");

        Scanner sc = new Scanner(System.in);
        Lista lista = new Lista(8192);

        while (sc.hasNext()) {
            String entrada = sc.next();
            if (entrada.equals("-1")) break;
            int idBuscado = Integer.parseInt(entrada);
            Restaurante r = buscarPorId(idBuscado, colecao);
            if (r != null) lista.inserirFim(r);
        }

        double inicio = System.nanoTime();

        if (sc.hasNextInt()) {
            int qtdComandos = sc.nextInt();
            for (int i = 0; i < qtdComandos; i++) {
                String comando = sc.next();
                if (comando.equals("II")) {
                    Restaurante r = buscarPorId(sc.nextInt(), colecao);
                    if (r != null) lista.inserirInicio(r);
                } else if (comando.equals("I*")) {
                    int pos = sc.nextInt();
                    Restaurante r = buscarPorId(sc.nextInt(), colecao);
                    if (r != null) lista.inserir(r, pos);
                } else if (comando.equals("IF")) {
                    Restaurante r = buscarPorId(sc.nextInt(), colecao);
                    if (r != null) lista.inserirFim(r);
                } else if (comando.equals("RI")) {
                    Restaurante r = lista.removerInicio();
                    if (r != null) System.out.println("(R)" + r.getNome());
                } else if (comando.equals("R*")) {
                    Restaurante r = lista.remover(sc.nextInt());
                    if (r != null) System.out.println("(R)" + r.getNome());
                } else if (comando.equals("RF")) {
                    Restaurante r = lista.removerFim();
                    if (r != null) System.out.println("(R)" + r.getNome());
                }
            }
        }

        double tempoExecucaoMs = (System.nanoTime() - inicio) / 1_000_000.0;
        sc.close();

        try (FileWriter writer = new FileWriter("859563_alocacaoSequencial.txt")) {
            writer.write(String.format("859563\t%d\t%d\t%g", comparacoes, movimentacoes, tempoExecucaoMs));
        }

        lista.mostrar();
    }

    static Restaurante buscarPorId(int id, ColecaoRestaurantes colecao) {
        Restaurante[] todos = colecao.getRestaurantes();
        for (int i = 0; i < colecao.getTamanho(); i++) {
            comparacoes++;
            if (todos[i].getId() == id) return todos[i];
        }
        return null;
    }
}
