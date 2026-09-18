package com.desafio.designpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Aplicação Spring Boot simplificada em arquivo único para demonstração
 * de Padrões de Projeto (Singleton, Strategy e Facade) no consumo da API de CEP.
 */
@EnableFeignClients
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// ==========================================
// 1. DTO / MODELO DE DADOS (Record)
// ==========================================
record EnderecoDto(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf
) {}

// ==========================================
// 2. PADRÃO STRATEGY (Interface de Consulta)
// ==========================================
interface CepStrategy {
    EnderecoDto buscarEndereco(String cep);
}

// Estratégia A: Consumo via OpenFeign (Declarativo)
@FeignClient(name = "viacep", url = "https://viacep.com.br/ws")
interface ViaCepFeignClient {
    @GetMapping("/{cep}/json/")
    EnderecoDto consultarCep(@PathVariable("cep") String cep);
}

@Service("feignStrategy")
class FeignCepStrategy implements CepStrategy {

    private final ViaCepFeignClient viaCepFeignClient;

    public FeignCepStrategy(ViaCepFeignClient viaCepFeignClient) {
        this.viaCepFeignClient = viaCepFeignClient;
    }

    @Override
    public EnderecoDto buscarEndereco(String cep) {
        return viaCepFeignClient.consultarCep(cep);
    }
}

// Estratégia B: Consumo via HttpClient nativo (Fallback/Alternativa)
@Service("nativeStrategy")
class NativeCepStrategy implements CepStrategy {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public EnderecoDto buscarEndereco(String cep) {
        String cleanCep = cep.replaceAll("\\D", "");
        String url = "https://viacep.com.br/ws/" + cleanCep + "/json/";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            return new EnderecoDto(
                    cleanCep,
                    extrairCampo(body, "logradouro"),
                    extrairCampo(body, "complemento"),
                    extrairCampo(body, "bairro"),
                    extrairCampo(body, "localidade"),
                    extrairCampo(body, "uf")
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro na consulta nativa de CEP", e);
        }
    }

    private String extrairCampo(String json, String chave) {
        String busca = "\"" + chave + "\": \"";
        int inicio = json.indexOf(busca);
        if (inicio == -1) return "";
        inicio += busca.length();
        int fim = json.indexOf("\"", inicio);
        return json.substring(inicio, fim);
    }
}

// ==========================================
// 3. PADRÃO FACADE (Serviço Unificado)
// ==========================================
@Service
class CepFacadeService {

    private final CepStrategy feignStrategy;
    private final CepStrategy nativeStrategy;

    // Injeção dos Beans Singletons pelo Spring
    public CepFacadeService(
            @org.springframework.beans.factory.annotation.Qualifier("feignStrategy") CepStrategy feignStrategy,
            @org.springframework.beans.factory.annotation.Qualifier("nativeStrategy") CepStrategy nativeStrategy
    ) {
        this.feignStrategy = feignStrategy;
        this.nativeStrategy = nativeStrategy;
    }

    public EnderecoDto consultar(String cep, String estrategia) {
        String cepLimpo = cep.replaceAll("\\D", "");

        if (cepLimpo.length() != 8) {
            throw new IllegalArgumentException("O CEP deve conter exatamente 8 dígitos.");
        }

        // Escolha dinamica de estrategia baseada na requisicao
        if ("native".equalsIgnoreCase(estrategia)) {
            return nativeStrategy.buscarEndereco(cepLimpo);
        }

        return feignStrategy.buscarEndereco(cepLimpo);
    }
}

// ==========================================
// 4. REST CONTROLLER (Interface HTTP)
// ==========================================
@RestController
@RequestMapping("/api/cep")
class CepController {

    private final CepFacadeService cepFacadeService;

    public CepController(CepFacadeService cepFacadeService) {
        this.cepFacadeService = cepFacadeService;
    }

    @GetMapping("/{cep}")
    public ResponseEntity<EnderecoDto> buscarCep(
            @PathVariable String cep,
            @RequestParam(defaultValue = "feign") String provider) {

        EnderecoDto endereco = cepFacadeService.consultar(cep, provider);
        return ResponseEntity.ok(endereco);
    }
}