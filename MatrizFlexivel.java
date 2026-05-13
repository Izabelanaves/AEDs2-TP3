import java.util.Scanner;

class CelulaMatriz {
	int valor;
	CelulaMatriz esq, dir, inf, sup;

	CelulaMatriz() {
		this.valor = 0;
		esq = dir = inf = sup = null;
	}

	CelulaMatriz(int valor) {
		this.valor = valor;
		esq = dir = inf = sup = null;
	}
}

class Matriz {
	CelulaMatriz inicio;
	int linhas, colunas;

	Matriz(int linhas, int colunas) {
		this.linhas = linhas;
		this.colunas = colunas;
		inicio = new CelulaMatriz();
		CelulaMatriz linhaAtual = inicio;
		for (int j = 1; j < colunas; j++) {
			linhaAtual.dir = new CelulaMatriz();
			linhaAtual.dir.esq = linhaAtual;
			linhaAtual = linhaAtual.dir;
		}
		CelulaMatriz linhaAnterior = inicio;
		for (int i = 1; i < linhas; i++) {
			CelulaMatriz novaLinha = new CelulaMatriz();
			novaLinha.sup = linhaAnterior;
			linhaAnterior.inf = novaLinha;
			CelulaMatriz colAtual = novaLinha;
			CelulaMatriz colAcima = linhaAnterior.dir;
			for (int j = 1; j < colunas; j++) {
				colAtual.dir = new CelulaMatriz();
				colAtual.dir.esq = colAtual;
				colAtual = colAtual.dir;
				colAtual.sup = colAcima;
				colAcima.inf = colAtual;
				colAcima = colAcima.dir;
			}
			linhaAnterior = novaLinha;
		}
	}

	void preencher(Scanner sc) {
		CelulaMatriz linhaAtual = inicio;
		for (int i = 0; i < linhas; i++) {
			CelulaMatriz colAtual = linhaAtual;
			for (int j = 0; j < colunas; j++) {
				colAtual.valor = sc.nextInt();
				colAtual = colAtual.dir;
			}
			linhaAtual = linhaAtual.inf;
		}
	}

	String diagonalPrincipal() {
		String s = "";
		CelulaMatriz atual = inicio;
		for (int i = 0; i < linhas && i < colunas; i++) {
			if (i > 0) s += " ";
			s += atual.valor;
			atual = atual.dir;
			if (atual != null) atual = atual.inf;
		}
		return s;
	}

	String diagonalSecundaria() {
		String s = "";
		CelulaMatriz atual = inicio;
		for (int i = 0; i < colunas - 1; i++) atual = atual.dir;
		for (int i = 0; i < linhas && i < colunas; i++) {
			if (i > 0) s += " ";
			s += atual.valor;
			atual = atual.inf;
			if (atual != null) atual = atual.esq;
		}
		return s;
	}

	Matriz soma(Matriz outra) {
		Matriz res = new Matriz(linhas, colunas);
		CelulaMatriz a = inicio, b = outra.inicio, c = res.inicio;
		for (int i = 0; i < linhas; i++) {
			CelulaMatriz ca = a, cb = b, cc = c;
			for (int j = 0; j < colunas; j++) {
				cc.valor = ca.valor + cb.valor;
				ca = ca.dir;
				cb = cb.dir;
				cc = cc.dir;
			}
			a = a.inf;
			b = b.inf;
			c = c.inf;
		}
		return res;
	}

	Matriz multiplicacao(Matriz outra) {
		Matriz res = new Matriz(linhas, outra.colunas);
		CelulaMatriz linhaA = inicio;
		CelulaMatriz linhaC = res.inicio;
		for (int i = 0; i < linhas; i++) {
			CelulaMatriz colC = linhaC;
			for (int j = 0; j < outra.colunas; j++) {
				int soma = 0;
				CelulaMatriz ca = linhaA;
				CelulaMatriz cb = outra.inicio;
				for (int k = 0; k < colunas; k++) {
					CelulaMatriz cbCol = cb;
					for (int l = 0; l < j; l++) cbCol = cbCol.dir;
					soma += ca.valor * cbCol.valor;
					ca = ca.dir;
					cb = cb.inf;
				}
				colC.valor = soma;
				colC = colC.dir;
			}
			linhaA = linhaA.inf;
			linhaC = linhaC.inf;
		}
		return res;
	}

	void mostrar() {
		CelulaMatriz linhaAtual = inicio;
		for (int i = 0; i < linhas; i++) {
			CelulaMatriz colAtual = linhaAtual;
			for (int j = 0; j < colunas; j++) {
				if (j > 0) System.out.print(" ");
				System.out.print(colAtual.valor);
				colAtual = colAtual.dir;
			}
			System.out.println();
			linhaAtual = linhaAtual.inf;
		}
	}
}

public class MatrizFlexivel {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int pares = sc.nextInt();

		for (int p = 0; p < pares; p++) {
			int lin = sc.nextInt();
			int col = sc.nextInt();

			Matriz m1 = new Matriz(lin, col);
			m1.preencher(sc);

			Matriz m2 = new Matriz(lin, col);
			m2.preencher(sc);

			System.out.println(m1.diagonalPrincipal());
			System.out.println(m2.diagonalSecundaria());

			Matriz s = m1.soma(m2);
			s.mostrar();

			Matriz prod = m1.multiplicacao(m2);
			prod.mostrar();
		}
	}
}
