#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define MAX_RESTAURANTES 8192
#define MAX_TIPOS 32
#define MAX_NOME 256
#define MAX_CIDADE 128
#define MAX_LINHA 1024

int meu_strlen(const char *s)
{
	int len = 0;
	while (s[len] != '\0')
		len++;
	return len;
}

void meu_strncpy(char *dest, const char *src, int n)
{
	int i;
	for (i = 0; i < n && src[i] != '\0'; i++)
		dest[i] = src[i];
	for (; i < n; i++)
		dest[i] = '\0';
}

void meu_strcat(char *dest, const char *src)
{
	int d = 0, s = 0;
	while (dest[d] != '\0')
		d++;
	while (src[s] != '\0')
		dest[d++] = src[s++];
	dest[d] = '\0';
}

char *meu_strchr(const char *s, char c)
{
	while (*s != '\0') {
		if (*s == c)
			return (char *)s;
		s++;
	}
	if (c == '\0')
		return (char *)s;
	return NULL;
}

char *meu_strtok_r(char *str, const char *delim, char **saveptr)
{
	if (str == NULL)
		str = *saveptr;
	if (*str == '\0') {
		*saveptr = str;
		return NULL;
	}
	while (*str != '\0') {
		int is_delim = 0;
		for (int i = 0; delim[i] != '\0'; i++)
			if (*str == delim[i])
				is_delim = 1;
		if (!is_delim)
			break;
		str++;
	}
	if (*str == '\0') {
		*saveptr = str;
		return NULL;
	}
	char *token = str;
	while (*str != '\0') {
		int is_delim = 0;
		for (int i = 0; delim[i] != '\0'; i++)
			if (*str == delim[i])
				is_delim = 1;
		if (is_delim)
			break;
		str++;
	}
	if (*str != '\0') {
		*str = '\0';
		*saveptr = str + 1;
	} else {
		*saveptr = str;
	}
	return token;
}

typedef struct {
	int hora;
	int minuto;
} Hora;

typedef struct {
	int dia;
	int mes;
	int ano;
} Data;

typedef struct {
	int id;
	char nome[MAX_NOME];
	char cidade[MAX_CIDADE];
	int capacidade;
	double avaliacao;
	char tiposCozinha[MAX_TIPOS][MAX_NOME];
	int numTipos;
	int faixaPreco;
	Hora horarioAbertura;
	Hora horarioFechamento;
	Data dataAbertura;
	int aberto;
} Restaurante;

typedef struct {
	Restaurante restaurantes[MAX_RESTAURANTES];
	int tamanho;
} ColecaoRestaurantes;

Hora parseHora(const char *s)
{
	Hora h = {0, 0};
	int hh, mm;
	if (sscanf(s, "%d:%d", &hh, &mm) == 2) {
		h.hora = hh;
		h.minuto = mm;
	}
	return h;
}

char *formatarHora(const Hora *h, char *buf)
{
	sprintf(buf, "%02d:%02d", h->hora, h->minuto);
	return buf;
}

Data parseData(const char *s)
{
	Data d = {0, 0, 0};
	int ano, mes, dia;
	if (sscanf(s, "%d-%d-%d", &ano, &mes, &dia) == 3) {
		d.dia = dia;
		d.mes = mes;
		d.ano = ano;
	}
	return d;
}

char *formatarData(const Data *d, char *buf)
{
	sprintf(buf, "%02d/%02d/%d", d->dia, d->mes, d->ano);
	return buf;
}

static void parseTiposCozinha(const char *s, char tipos[][MAX_NOME], int *numTipos)
{
	*numTipos = 0;
	char temp[MAX_LINHA];
	meu_strncpy(temp, s, sizeof(temp) - 1);
	temp[sizeof(temp) - 1] = '\0';
	char *saveptr_tipos;
	char *token = meu_strtok_r(temp, ";", &saveptr_tipos);
	while (token != NULL && *numTipos < MAX_TIPOS) {
		meu_strncpy(tipos[*numTipos], token, MAX_NOME - 1);
		tipos[*numTipos][MAX_NOME - 1] = '\0';
		(*numTipos)++;
		token = meu_strtok_r(NULL, ";", &saveptr_tipos);
	}
}

Restaurante parseRestaurante(const char *s)
{
	Restaurante r = {0};
	char linha[MAX_LINHA];
	meu_strncpy(linha, s, sizeof(linha) - 1);
	linha[sizeof(linha) - 1] = '\0';
	char *token, *saveptr;
	int campo = 0;
	char tiposStr[MAX_LINHA] = "";
	token = meu_strtok_r(linha, ",", &saveptr);
	while (token != NULL) {
		switch (campo) {
		case 0: r.id = atoi(token); break;
		case 1: meu_strncpy(r.nome, token, MAX_NOME - 1); r.nome[MAX_NOME - 1] = '\0'; break;
		case 2: meu_strncpy(r.cidade, token, MAX_CIDADE - 1); r.cidade[MAX_CIDADE - 1] = '\0'; break;
		case 3: r.capacidade = atoi(token); break;
		case 4: r.avaliacao = atof(token); break;
		case 5:
			meu_strncpy(tiposStr, token, sizeof(tiposStr) - 1);
			tiposStr[sizeof(tiposStr) - 1] = '\0';
			parseTiposCozinha(tiposStr, r.tiposCozinha, &r.numTipos);
			break;
		case 6: r.faixaPreco = meu_strlen(token); break;
		case 7: {
			char horA[16] = {0}, horF[16] = {0};
			sscanf(token, "%15[^-]", horA);
			char *dash = meu_strchr(token, '-');
			if (dash) sscanf(dash + 1, "%15[^,]", horF);
			r.horarioAbertura = parseHora(horA);
			r.horarioFechamento = parseHora(horF);
		} break;
		case 8: r.dataAbertura = parseData(token); break;
		case 9: r.aberto = (strcmp(token, "true") == 0) ? 1 : 0; break;
		}
		token = meu_strtok_r(NULL, ",", &saveptr);
		campo++;
	}
	return r;
}

char *formatarRestaurante(const Restaurante *r, char *buf)
{
	char tiposFormatados[MAX_LINHA] = "";
	for (int i = 0; i < r->numTipos; i++) {
		meu_strcat(tiposFormatados, r->tiposCozinha[i]);
		if (i < r->numTipos - 1)
			meu_strcat(tiposFormatados, ",");
	}
	char precoStr[32] = "";
	for (int i = 0; i < r->faixaPreco && i < 31; i++)
		precoStr[i] = '$';
	precoStr[r->faixaPreco < 31 ? r->faixaPreco : 31] = '\0';
	char hA[16], hF[16], dStr[32];
	formatarHora(&r->horarioAbertura, hA);
	formatarHora(&r->horarioFechamento, hF);
	formatarData(&r->dataAbertura, dStr);
	sprintf(buf, "[%d ## %s ## %s ## %d ## %.1f ## [%s] ## %s ## %s-%s ## %s ## %s]",
		r->id, r->nome, r->cidade, r->capacidade, r->avaliacao,
		tiposFormatados, precoStr, hA, hF, dStr, r->aberto ? "true" : "false");
	return buf;
}

void lerCsv(ColecaoRestaurantes *colecao, const char *path)
{
	colecao->tamanho = 0;
	FILE *file = fopen(path, "r");
	char linha[MAX_LINHA];
	if (fgets(linha, sizeof(linha), file) == NULL) { fclose(file); return; }
	while (fgets(linha, sizeof(linha), file) && colecao->tamanho < MAX_RESTAURANTES) {
		for (int i = 0; linha[i] != '\0'; i++)
			if (linha[i] == '\n' || linha[i] == '\r') { linha[i] = '\0'; break; }
		if (meu_strlen(linha) > 0)
			colecao->restaurantes[colecao->tamanho++] = parseRestaurante(linha);
	}
	fclose(file);
}

/* ========================================================================== */

static int num_comparacoes;
static int num_movimentacoes;

typedef struct Celula {
	Restaurante restaurante;
	struct Celula *prox;
} Celula;

typedef struct {
	Celula *primeiro;
	Celula *ultimo;
	int tamanho;
} Lista;

Lista *novaLista(void)
{
	Lista *l = calloc(1, sizeof(Lista));
	l->primeiro = calloc(1, sizeof(Celula));
	l->ultimo = l->primeiro;
	l->tamanho = 0;
	return l;
}

void inserirInicio(Lista *l, Restaurante r)
{
	Celula *tmp = calloc(1, sizeof(Celula));
	tmp->restaurante = r;
	tmp->prox = l->primeiro->prox;
	++num_movimentacoes;
	l->primeiro->prox = tmp;
	++num_movimentacoes;
	if (l->primeiro == l->ultimo) {
		l->ultimo = tmp;
		++num_movimentacoes;
	}
	l->tamanho++;
}

void inserirFim(Lista *l, Restaurante r)
{
	l->ultimo->prox = calloc(1, sizeof(Celula));
	++num_movimentacoes;
	l->ultimo = l->ultimo->prox;
	++num_movimentacoes;
	l->ultimo->restaurante = r;
	l->ultimo->prox = NULL;
	l->tamanho++;
}

void inserir(Lista *l, Restaurante r, int pos)
{
	if (pos < 0 || pos > l->tamanho) return;
	Celula *ant = l->primeiro;
	for (int i = 0; i < pos; i++)
		ant = ant->prox;
	Celula *tmp = calloc(1, sizeof(Celula));
	tmp->restaurante = r;
	tmp->prox = ant->prox;
	++num_movimentacoes;
	ant->prox = tmp;
	++num_movimentacoes;
	if (ant == l->ultimo) {
		l->ultimo = tmp;
		++num_movimentacoes;
	}
	l->tamanho++;
}

Restaurante removerInicio(Lista *l)
{
	Celula *tmp = l->primeiro->prox;
	Restaurante r = tmp->restaurante;
	l->primeiro->prox = tmp->prox;
	++num_movimentacoes;
	if (tmp == l->ultimo) {
		l->ultimo = l->primeiro;
		++num_movimentacoes;
	}
	free(tmp);
	l->tamanho--;
	return r;
}

Restaurante removerFim(Lista *l)
{
	Celula *ant = l->primeiro;
	while (ant->prox != l->ultimo)
		ant = ant->prox;
	Restaurante r = l->ultimo->restaurante;
	free(l->ultimo);
	l->ultimo = ant;
	ant->prox = NULL;
	l->tamanho--;
	return r;
}

Restaurante remover(Lista *l, int pos)
{
	if (pos < 0 || pos >= l->tamanho) { Restaurante vazio = {0}; return vazio; }
	Celula *ant = l->primeiro;
	for (int i = 0; i < pos; i++)
		ant = ant->prox;
	Celula *tmp = ant->prox;
	Restaurante r = tmp->restaurante;
	ant->prox = tmp->prox;
	if (tmp == l->ultimo)
		l->ultimo = ant;
	free(tmp);
	l->tamanho--;
	return r;
}

void mostrar(Lista *l)
{
	char buf[512];
	for (Celula *i = l->primeiro->prox; i != NULL; i = i->prox)
		puts(formatarRestaurante(&i->restaurante, buf));
}

Restaurante *buscarPorId(int id, ColecaoRestaurantes *colecao)
{
	for (int i = 0; i < colecao->tamanho; i++) {
		++num_comparacoes;
		if (colecao->restaurantes[i].id == id)
			return &colecao->restaurantes[i];
	}
	return NULL;
}

int main(void)
{
	ColecaoRestaurantes *colecao = calloc(1, sizeof(*colecao));
	lerCsv(colecao, "/tmp/restaurantes.csv");

	Lista *lista = novaLista();

	int idBuscado;
	while (scanf("%d", &idBuscado) == 1) {
		if (idBuscado == -1) break;
		Restaurante *r = buscarPorId(idBuscado, colecao);
		if (r != NULL) inserirFim(lista, *r);
	}

	int qtd;
	scanf("%d", &qtd);

	clock_t inicio = clock();
	char linhaComando[64];

	for (int i = 0; i < qtd; i++) {
		scanf(" %[^\n]", linhaComando);
		char cmd[4];
		int pos, idCmd;
		if (sscanf(linhaComando, "%s %d %d", cmd, &pos, &idCmd) >= 3) {
			Restaurante *r = buscarPorId(idCmd, colecao);
			if (r != NULL) inserir(lista, *r, pos);
		} else if (sscanf(linhaComando, "%s %d", cmd, &idCmd) >= 2) {
			if (cmd[0] == 'I' && cmd[1] == 'I') {
				Restaurante *r = buscarPorId(idCmd, colecao);
				if (r != NULL) inserirInicio(lista, *r);
			} else if (cmd[0] == 'I' && cmd[1] == 'F') {
				Restaurante *r = buscarPorId(idCmd, colecao);
				if (r != NULL) inserirFim(lista, *r);
			} else if (cmd[0] == 'R' && cmd[1] == '*') {
				Restaurante r = remover(lista, idCmd);
				printf("(R)%s\n", r.nome);
			}
		} else {
			if (linhaComando[0] == 'R' && linhaComando[1] == 'I') {
				Restaurante r = removerInicio(lista);
				printf("(R)%s\n", r.nome);
			} else if (linhaComando[0] == 'R' && linhaComando[1] == 'F') {
				Restaurante r = removerFim(lista);
				printf("(R)%s\n", r.nome);
			}
		}
	}

	double tempoExecucaoMs = (double)(clock() - inicio) / CLOCKS_PER_SEC * 1000.0;

	FILE *log_file = fopen("859563_listaFlexivel.txt", "w");
	fprintf(log_file, "%s\t%d\t%g", "859563", num_comparacoes, tempoExecucaoMs);
	fclose(log_file);

	mostrar(lista);

	free(lista->primeiro);
	free(lista);
	free(colecao);
	return 0;
}
