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

static int num_comparacoes;

typedef struct No {
	Restaurante restaurante;
	struct No *esq;
	struct No *dir;
} No;

No *novoNo(Restaurante r)
{
	No *no = calloc(1, sizeof(No));
	no->restaurante = r;
	no->esq = no->dir = NULL;
	return no;
}

No *inserir(No *raiz, Restaurante r)
{
	if (raiz == NULL) return novoNo(r);
	if (strcmp(r.nome, raiz->restaurante.nome) < 0)
		raiz->esq = inserir(raiz->esq, r);
	else
		raiz->dir = inserir(raiz->dir, r);
	return raiz;
}

void pesquisar(No *raiz, const char *nome)
{
	printf("raiz");
	No *atual = raiz;
	while (atual != NULL) {
		++num_comparacoes;
		int cmp = strcmp(nome, atual->restaurante.nome);
		if (cmp == 0) {
			printf(" SIM\n");
			return;
		}
		if (cmp < 0) {
			printf(" esq");
			atual = atual->esq;
		} else {
			printf(" dir");
			atual = atual->dir;
		}
	}
	printf(" NAO\n");
}

void caminharCentral(No *raiz)
{
	char buf[512];
	if (raiz == NULL) return;
	caminharCentral(raiz->esq);
	puts(formatarRestaurante(&raiz->restaurante, buf));
	caminharCentral(raiz->dir);
}

Restaurante *buscarPorId(int id, ColecaoRestaurantes *colecao)
{
	for (int i = 0; i < colecao->tamanho; i++) {
		if (colecao->restaurantes[i].id == id)
			return &colecao->restaurantes[i];
	}
	return NULL;
}

int main(void)
{
	ColecaoRestaurantes *colecao = calloc(1, sizeof(*colecao));
	lerCsv(colecao, "/tmp/restaurantes.csv");

	No *raiz = NULL;

	int idBuscado;
	while (scanf("%d", &idBuscado) == 1) {
		if (idBuscado == -1) break;
		Restaurante *r = buscarPorId(idBuscado, colecao);
		if (r != NULL) raiz = inserir(raiz, *r);
	}

	clock_t inicio = clock();
	char nomeLinha[MAX_NOME];

	while (scanf(" %[^\n]", nomeLinha) == 1) {
		if (strcmp(nomeLinha, "FIM") == 0) break;
		pesquisar(raiz, nomeLinha);
	}

	double tempoExecucaoMs = (double)(clock() - inicio) / CLOCKS_PER_SEC * 1000.0;

	FILE *log_file = fopen("859563_arvoreBinaria.txt", "w");
	fprintf(log_file, "%s\t%d\t%g", "859563", num_comparacoes, tempoExecucaoMs);
	fclose(log_file);

	caminharCentral(raiz);

	free(colecao);
	return 0;
}
