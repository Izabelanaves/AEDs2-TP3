#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define MAX_RESTAURANTES 8192
#define MAX_TIPOS 32
#define MAX_NOME 256
#define MAX_CIDADE 128
#define MAX_LINHA 1024

/* Funcoe auxiliares de forma manual*/

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
	for (i = 0; i < n && src[i] != '\0'; i++) {
		dest[i] = src[i];
	}
	for (; i < n; i++) {
		dest[i] = '\0';
	}
}

void meu_strcat(char *dest, const char *src)
{
	int d = 0, s = 0;
	while (dest[d] != '\0')
		d++;
	while (src[s] != '\0') {
		dest[d++] = src[s++];
	}
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
		for (int i = 0; delim[i] != '\0'; i++) {
			if (*str == delim[i])
				is_delim = 1;
		}
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
		for (int i = 0; delim[i] != '\0'; i++) {
			if (*str == delim[i])
				is_delim = 1;
		}
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

/* Hora */

Hora parseHora(const char *s)
{
	Hora h = { 0, 0 };
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

/* Data */

Data parseData(const char *s)
{
	Data d = { 0, 0, 0 };
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

/* Restaurante */

Restaurante parseRestaurante(const char *s)
{
	Restaurante r = { 0 };
	char linha[MAX_LINHA];
	meu_strncpy(linha, s, sizeof(linha) - 1);
	linha[sizeof(linha) - 1] = '\0';

	char *token;
	char *saveptr;
	int campo = 0;
	char tiposStr[MAX_LINHA] = "";

	token = meu_strtok_r(linha, ",", &saveptr);

	while (token != NULL) {
		switch (campo) {
		case 0:
			r.id = atoi(token);
			break;
		case 1:
			meu_strncpy(r.nome, token, MAX_NOME - 1);
			r.nome[MAX_NOME - 1] = '\0';
			break;
		case 2:
			meu_strncpy(r.cidade, token, MAX_CIDADE - 1);
			r.cidade[MAX_CIDADE - 1] = '\0';
			break;
		case 3:
			r.capacidade = atoi(token);
			break;
		case 4:
			r.avaliacao = atof(token);
			break;
		case 5:
			meu_strncpy(tiposStr, token, sizeof(tiposStr) - 1);
			tiposStr[sizeof(tiposStr) - 1] = '\0';
			parseTiposCozinha(tiposStr, r.tiposCozinha, &r.numTipos);
			break;
		case 6:
			r.faixaPreco = meu_strlen(token);
			break;
		case 7: // horário "HH:MM-HH:MM"
		{
			char horA[16] = { 0 }, horF[16] = { 0 };
			sscanf(token, "%15[^-]", horA);
			char *dash = meu_strchr(token, '-');
			if (dash)
				sscanf(dash + 1, "%15[^,]", horF);
			r.horarioAbertura = parseHora(horA);
			r.horarioFechamento = parseHora(horF);
		} break;
		case 8: // data
			r.dataAbertura = parseData(token);
			break;
		case 9: // aberto
			r.aberto = (strcmp(token, "true") == 0) ? 1 : 0;
			break;
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

	sprintf(buf, "[%d ## %s ## %s ## %d ## %.1f ## [%s] ## %s ## %s-%s ## %s ## %s]", r->id, r->nome, r->cidade, r->capacidade, r->avaliacao, tiposFormatados, precoStr, hA, hF, dStr,r->aberto ? "true" : "false");

	return buf;
}

/* ColecaoRestaurantes */

void lerCsv(ColecaoRestaurantes *colecao, const char *path)
{
	colecao->tamanho = 0;
	FILE *file = fopen(path, "r");

	char linha[MAX_LINHA];
	/* pular cabecalho */
	if (fgets(linha, sizeof(linha), file) == NULL) {
		fclose(file);
		return;
	}

	while (fgets(linha, sizeof(linha), file) && colecao->tamanho < MAX_RESTAURANTES) {
		/* remover newline manualmente */
		for (int i = 0; linha[i] != '\0'; i++) {
			if (linha[i] == '\n' || linha[i] == '\r') {
				linha[i] = '\0';
				break;
			}
		}

		if (meu_strlen(linha) > 0)
			colecao->restaurantes[colecao->tamanho++] = parseRestaurante(linha);
	}

	fclose(file);
}

/* ========================================================================== */

// Contadores
static int num_comparacoes;
static int num_movimentacoes;

static int compara(Restaurante a, Restaurante b)
{
	if (a.avaliacao != b.avaliacao)
		return (a.avaliacao > b.avaliacao) - (a.avaliacao < b.avaliacao);
	return strcmp(a.nome, b.nome);
}

static void swap(Restaurante *a, Restaurante *b) {
	Restaurante tmp = *a;
	*a = *b;
	*b = tmp;
	++num_movimentacoes; // Conta movimentacao
}

static void quicksort(ColecaoRestaurantes *c, int esq, int dir)
{
	if (esq >= dir)
		return;
	Restaurante pivo = c->restaurantes[(esq + dir) / 2];
	int i = esq, j = dir;
	while (i <= j) {
		while (++num_comparacoes && compara(c->restaurantes[i], pivo) < 0) i++;
		while (++num_comparacoes && compara(c->restaurantes[j], pivo) > 0) j--;
		if (i <= j) {
			swap(&c->restaurantes[i], &c->restaurantes[j]);
			i++; j--;
		}
	}
	quicksort(c, esq, j);
	quicksort(c, i, dir);
}

int main(void)
{
	ColecaoRestaurantes *colecao = calloc(1, sizeof(*colecao));
	ColecaoRestaurantes *selecionados = calloc(1, sizeof(*selecionados));
	lerCsv(colecao, "/tmp/restaurantes.csv");

	int idBuscado;
	char buf[2048];

	while (scanf("%d", &idBuscado) == 1) {
		if (idBuscado == -1)
			break;
		for (int i = 0; i < colecao->tamanho; i++) {
			if (colecao->restaurantes[i].id == idBuscado) {
				selecionados->restaurantes[selecionados->tamanho++] = colecao->restaurantes[i];
				i = MAX_RESTAURANTES;
			}
		}
	}

	// Cronometra
	struct timespec time_begin, time_end;
	clock_gettime(CLOCK_MONOTONIC, &time_begin);
	quicksort(selecionados, 0, selecionados->tamanho - 1); // Roda ordenacao
	clock_gettime(CLOCK_MONOTONIC, &time_end);
	double duracao_segundos = time_end.tv_sec - time_begin.tv_sec + ((double)(time_end.tv_nsec - time_begin.tv_sec))/1000000000;

	// Log
	FILE *log_file = fopen("859563_quicksort.txt", "w");
	fprintf(log_file, "%s\t%d\t%d\t%g", "859563", num_comparacoes, num_movimentacoes, duracao_segundos);
	fclose(log_file);

	char printBuf[512];
	for (int i = 0; i < selecionados->tamanho; ++i)
		puts(formatarRestaurante(&selecionados->restaurantes[i], printBuf));

	free(selecionados);
	free(colecao);
	return 0;
}
