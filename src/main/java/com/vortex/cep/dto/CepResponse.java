package com.vortex.cep.dto;

public record CepResponse(
    String cep,
    String logradouro,
    String complemento,
    String bairro,
    String cidade,
    String uf,
    String estado,
    String ibge) {
  public static CepResponse from(OpenCepResponse resposta) {
    return new CepResponse(
        resposta.cep(),
        resposta.logradouro(),
        resposta.complemento(),
        resposta.bairro(),
        resposta.localidade(),
        resposta.uf(),
        resposta.estado(),
        resposta.ibge());
  }
}
