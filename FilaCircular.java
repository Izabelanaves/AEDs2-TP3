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
    public Data getDataAbertura() { return this.dataAbertura; }

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
    public int getAno() { return this.ano; }
    
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


class Fila {
    private Restaurante[] array;
    private int primeiro;
    private int ultimo;

    public Fila(int tamanho) {
        array = new Restaurante[tamanho + 1];
        primeiro = ultimo = 0;
    }

    public void inserir(Restaurante restaurante) {
        if (((ultimo + 1) % array.length) == primeiro) {
            Restaurante removido = remover();
            if (removido != null) {
                System.out.println("(R)" + removido.getNome());
            }
        }
        
        array[ultimo] = restaurante;
        ultimo = (ultimo + 1) % array.length;
        FilaCircular.movimentacoes++;
    }

    public Restaurante remover() {
        if (primeiro == ultimo) return null;
        Restaurante resp = array[primeiro];
        primeiro = (primeiro + 1) % array.length;
        FilaCircular.movimentacoes++;
        return resp;
    }

    public int getMediaAnoAbertura() {
        if (primeiro == ultimo) return 0;
        
        int soma = 0;
        int count = 0;
        int i = primeiro;
        
        while (i != ultimo) {
            soma += array[i].getDataAbertura().getAno();
            count++;
            i = (i + 1) % array.length;
        }
        return Math.round((float) soma / count);
    }

    public void mostrar() {
        int i = primeiro;
        while (i != ultimo) {
            System.out.println(array[i].formatar());
            i = (i + 1) % array.length;
        }
    }
}

public class FilaCircular {
    public static int comparacoes = 0;
    public static int movimentacoes = 0;

    public static void main(String[] args) throws IOException {
        ColecaoRestaurantes colecao = new ColecaoRestaurantes();
        colecao.lerCsv("/tmp/restaurantes.csv");

        Scanner sc = new Scanner(System.in);
        Fila fila = new Fila(5);

        while (sc.hasNext()) {
            String entrada = sc.next();
            if (entrada.equals("-1")) break;
            
            int idBuscado = Integer.parseInt(entrada);
            Restaurante r = buscarPorId(idBuscado, colecao);
            if (r != null) {
                fila.inserir(r);
                System.out.println("(I)" + fila.getMediaAnoAbertura());
            }
        }

        double inicio = System.nanoTime();

        if (sc.hasNextInt()) {
            int qtdComandos = sc.nextInt();
            for (int i = 0; i < qtdComandos; i++) {
                String comando = sc.next();
                
                if (comando.equals("I")) {
                    Restaurante r = buscarPorId(sc.nextInt(), colecao);
                    if (r != null) {
                        fila.inserir(r);
                        System.out.println("(I)" + fila.getMediaAnoAbertura());
                    }
                } else if (comando.equals("R")) {
                    Restaurante r = fila.remover();
                    if (r != null) System.out.println("(R)" + r.getNome());
                }
            }
        }

        double tempoExecucaoMs = (System.nanoTime() - inicio) / 1_000_000.0;
        sc.close();

        try (FileWriter writer = new FileWriter("859563_fila.txt")) {
            writer.write(String.format("859563\t%d\t%d\t%g", comparacoes, movimentacoes, tempoExecucaoMs));
        }
        fila.mostrar();
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
