import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

class Cartao {
    String partidaId, atleta, tipoCartao;
    Cartao(String partidaId, String atleta, String tipoCartao) {
        this.partidaId = partidaId;
        this.atleta = atleta;
        this.tipoCartao = tipoCartao;
    }
}

class Partida {
    String id, mandante, visitante, vencedor, data;
    int mandantePlacar, visitantePlacar;
    String mandanteEstado, visitanteEstado;

    Partida(String id, String mandante, String visitante, String vencedor, int mandantePlacar, int visitantePlacar, String mandanteEstado, String visitanteEstado, String data) {
        this.id = id;
        this.mandante = mandante;
        this.visitante = visitante;
        this.vencedor = vencedor;
        this.mandantePlacar = mandantePlacar;
        this.visitantePlacar = visitantePlacar;
        this.mandanteEstado = mandanteEstado;
        this.visitanteEstado = visitanteEstado;
        this.data = data;
    }
}

class Gol {
    String partidaId, atleta, tipoGol;
    Gol(String partidaId, String atleta, String tipoGol) {
        this.partidaId = partidaId;
        this.atleta = atleta;
        this.tipoGol = tipoGol;
    }
}

public class CampeonatoBrasileiro {
    public static void main(String[] args) {
        String cartoesFile = "C:/Users/othon/OneDrive/Documentos/GitHub/SantanderCoders2024/Trabalho_Final_Modulo_4/CampeonatoBrasileiro/files/campeonato-brasileiro-cartoes.csv";
        String partidasFile = "C:/Users/othon/OneDrive/Documentos/GitHub/SantanderCoders2024/Trabalho_Final_Modulo_4/CampeonatoBrasileiro/files/campeonato-brasileiro-full.csv";
        String golsFile = "C:/Users/othon/OneDrive/Documentos/GitHub/SantanderCoders2024/Trabalho_Final_Modulo_4/CampeonatoBrasileiro/files/campeonato-brasileiro-gols.csv";

        List<Cartao> cartoes = lerCartoes(cartoesFile);
        List<Partida> partidas = lerPartidas(partidasFile);
        List<Gol> gols = lerGols(golsFile);

        System.out.println("O time que mais venceu jogos no ano 2008: " + timeComMaisVitorias(partidas, 2008));
        System.out.println("O Estado que teve menos jogos dentro do período 2003 e 2022: " + estadosComMenosVitorias(partidas));
        System.out.println("O jogador que mais fez gols: " + jogadorComMaisGols(gols));
        System.out.println("O jogador que mais fez gols de pênaltis: " + jogadorComMaisGolsDePenalti(gols));
        System.out.println("O jogador que mais fez gols contras: " + jogadoresComMaisGolsContra(gols));
        System.out.println("O jogador que mais recebeu cartões amarelos: " + jogadoresComMaisCartoes(cartoes, "Amarelo"));
        System.out.println("O jogador que mais recebeu cartões vermelhos: " + jogadoresComMaisCartoes(cartoes, "Vermelho"));
        System.out.println("O placar da partida com mais gols.: " + partidaComMaisGols(partidas));
    }

    private static List<Cartao> lerCartoes(String filePath) {
        List<Cartao> cartoes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Ignorar o cabeçalho
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");

                // Corrigir a leitura do cartão e atleta
                String partidaId = fields[0].replace("\"", "").trim();
                String atleta = fields[4].replace("\"", "").trim();
                String tipoCartao = fields[3].replace("\"", "").trim();

                Cartao cartao = new Cartao(partidaId, atleta, tipoCartao);
                cartoes.add(cartao);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cartoes;
    }

    private static List<Partida> lerPartidas(String filePath) {
        List<Partida> partidas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Ignorar o cabeçalho
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");

                // Limpar e validar os campos de placar
                String mandantePlacarStr = fields[12].replace("\"", "").trim();
                String visitantePlacarStr = fields[13].replace("\"", "").trim();

                // Verificar se os campos não estão vazios
                int mandantePlacar = mandantePlacarStr.isEmpty() ? 0 : Integer.parseInt(mandantePlacarStr);
                int visitantePlacar = visitantePlacarStr.isEmpty() ? 0 : Integer.parseInt(visitantePlacarStr);

                // Lê a data da terceira coluna
                String data = fields[2].replace("\"", "").trim(); // Corrigido para a terceira coluna

                Partida partida = new Partida(fields[0], fields[4], fields[5], fields[10],
                        mandantePlacar, visitantePlacar, fields[14], fields[15], data); // Passando a data
                partidas.add(partida);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return partidas;
    }


    private static List<Gol> lerGols(String filePath) {
        List<Gol> gols = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Ignorar o cabeçalho
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Regex para lidar com aspas
                Gol gol = new Gol(fields[0].replace("\"", "").trim(), fields[3].replace("\"", "").trim(), fields[5].replace("\"", "").trim());
                gols.add(gol);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gols;
    }

    private static String timeComMaisVitorias(List<Partida> partidas, int ano) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

        Map<String, Long> vitorias = partidas.stream()
                .filter(p -> p.vencedor != null && !p.vencedor.contains("-") && !p.vencedor.isEmpty()) // Filtrando vencedores inválidos
                .filter(p -> {
                    try {
                        LocalDate partidaDate = LocalDate.parse(p.data, formatter);
                        return partidaDate.getYear() == ano; // Filtra apenas partidas do ano de 2008
                    } catch (DateTimeParseException e) {
                        return false; // Ignorar partidas com data inválida
                    }
                })
                .collect(Collectors.groupingBy(p -> p.vencedor, Collectors.counting()));

        // Verifica se o mapa de vitórias está vazio
        if (vitorias.isEmpty()) {
            return "Nenhum time venceu em 2008.";
        }

        // Obtém a quantidade máxima de vitórias
        long maxVitorias = vitorias.values().stream().max(Long::compare).orElse(0L);

        // Encontra os times que têm a quantidade máxima de vitórias
        List<String> timesComMaxVitorias = vitorias.entrySet().stream()
                .filter(entry -> entry.getValue() == maxVitorias)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Formata a saída
        return String.join(", ", timesComMaxVitorias);
    }

    private static List<String> estadosComMenosVitorias(List<Partida> partidas) {
        // Filtrar partidas entre 2003 e 2022
        Map<String, Long> estadosVitorias = partidas.stream()
                .filter(p -> {
                    try {
                        LocalDate partidaDate = LocalDate.parse(p.data, DateTimeFormatter.ofPattern("d/M/yyyy"));
                        return partidaDate.getYear() >= 2003 && partidaDate.getYear() <= 2022;
                    } catch (DateTimeParseException e) {
                        return false; // Ignorar partidas com data inválida
                    }
                })
                .filter(p -> p.vencedor != null && !p.vencedor.contains("-") && !p.vencedor.isEmpty())
                .collect(Collectors.groupingBy(p -> {
                    // Retorna o estado do vencedor
                    return p.mandanteEstado.equals(p.vencedor) ? p.mandanteEstado : p.visitanteEstado;
                }, Collectors.counting()));

        // Verifica se o mapa de vitórias está vazio
        if (estadosVitorias.isEmpty()) {
            return List.of("Nenhum estado venceu entre 2003 e 2022.");
        }

        // Obtém o mínimo de vitórias
        long minVitorias = estadosVitorias.values().stream().min(Long::compare).orElse(0L);

        // Obtém a lista de estados com o mínimo de vitórias
        List<String> estadosComMenosVitorias = estadosVitorias.entrySet().stream()
                .filter(entry -> entry.getValue() == minVitorias)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return estadosComMenosVitorias.isEmpty() ? List.of("Nenhum estado encontrado.") : estadosComMenosVitorias;
    }

    private static String jogadorComMaisGols(List<Gol> gols) {
        Map<String, Long> jogadoresGols = gols.stream()
                .collect(Collectors.groupingBy(g -> g.atleta, Collectors.counting()));

        return jogadoresGols.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }

    private static String jogadorComMaisGolsDePenalti(List<Gol> gols) {
        Map<String, Long> jogadoresGolsDePenalti = gols.stream()
                .filter(g -> g.tipoGol.equalsIgnoreCase("Penalty")) // Usar equalsIgnoreCase para evitar problemas de case
                .collect(Collectors.groupingBy(g -> g.atleta, Collectors.counting()));

        // Verifica se o mapa de gols de pênalti está vazio
        if (jogadoresGolsDePenalti.isEmpty()) {
            return "Nenhum gol de pênalti registrado.";
        }

        // Obtém o jogador com mais gols de pênalti
        return jogadoresGolsDePenalti.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey) // Extrai o jogador com mais gols de pênalti
                .orElse("Nenhum jogador encontrado");
    }


    private static List<String> jogadoresComMaisGolsContra(List<Gol> gols) {
        Map<String, Long> jogadoresGolsContra = gols.stream()
                .filter(g -> g.tipoGol.equalsIgnoreCase("Gol Contra"))
                .collect(Collectors.groupingBy(g -> g.atleta, Collectors.counting()));

        // Verifica se o mapa de gols contra está vazio
        if (jogadoresGolsContra.isEmpty()) {
            return List.of("Nenhum gol contra registrado.");
        }

        long maxGols = jogadoresGolsContra.values().stream()
                .max(Long::compare).orElse(0L);

        // Obtém a lista de jogadores com o máximo de gols contra
        return jogadoresGolsContra.entrySet().stream()
                .filter(entry -> entry.getValue().equals(maxGols))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    private static String jogadoresComMaisCartoes(List<Cartao> cartoes, String corCartao) {
        // Mapeia a contagem de cartões para a cor especificada
        Map<String, Long> jogadoresCartoes = cartoes.stream()
                .filter(c -> c.tipoCartao.equalsIgnoreCase(corCartao))
                .collect(Collectors.groupingBy(c -> c.atleta, Collectors.counting()));

        // Verifica se a contagem está vazia
        if (jogadoresCartoes.isEmpty()) {
            return "Nenhum cartão " + corCartao + " registrado.";
        }

        // Determina o máximo de cartões da cor especificada
        long maxCartoes = jogadoresCartoes.values().stream()
                .max(Long::compare).orElse(0L);

        // Filtra jogadores que têm a contagem máxima
        List<String> jogadoresComMaxCartoes = jogadoresCartoes.entrySet().stream()
                .filter(entry -> entry.getValue().equals(maxCartoes))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Formata a saída
        return "Jogador(es) que mais recebeu cartões " + corCartao + ": " + String.join(", ", jogadoresComMaxCartoes);
    }


    private static String partidaComMaisGols(List<Partida> partidas) {
        return partidas.stream()
                .max(Comparator.comparingInt(p -> p.mandantePlacar + p.visitantePlacar))
                .get().id;
    }
}
